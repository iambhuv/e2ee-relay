use std::error::Error;
use std::net::IpAddr;
use std::net::Ipv4Addr;
use std::net::SocketAddr;
use std::sync::Arc;
use std::time::Duration;

use axum::Router;
use common::quic::ALPN_PROTOCALL;
use quinn::Endpoint;
use quinn::ServerConfig;
use quinn::TransportConfig;
use quinn::crypto::rustls::QuicServerConfig;
use quinn::rustls;
use quinn::rustls::pki_types::PrivateKeyDer;
use rcgen::CertifiedKey;
use tokio::io::AsyncReadExt;
use tokio::sync::Mutex;

use crate::realtime;
use crate::realtime::SocketData;
use crate::utils::config::AppConfig;

pub async fn serve(config: &AppConfig, router: Router) -> Result<(), std::io::Error> {
    let listener = tokio::net::TcpListener::bind(SocketAddr::new(
        IpAddr::V4(Ipv4Addr::new(0, 0, 0, 0)),
        config.server.port,
    ))
    .await
    .unwrap();

    println!("[+] Listening at http://0.0.0.0:{}", config.server.port);

    axum::serve(listener, router.into_make_service_with_connect_info::<SocketAddr>()).await
}

fn frame_packet(packet: &[u8]) -> Vec<u8> {
    let size: [u8; 4] = (packet.len() as u32).to_be_bytes();
    [&size, packet].concat()
}

pub async fn serve_quic(config: &AppConfig) -> Result<(), Box<dyn Error>> {
    let CertifiedKey { cert, signing_key } =
        rcgen::generate_simple_self_signed(vec!["localhost".to_string()])?;

    let mut server_crypto =
        rustls::ServerConfig::builder().with_no_client_auth().with_single_cert(
            vec![cert.der().clone()],
            PrivateKeyDer::try_from(signing_key.serialize_der())?,
        )?;

    server_crypto.alpn_protocols = ALPN_PROTOCALL.iter().map(|&x| x.into()).collect();

    let mut transport_config = TransportConfig::default();
    transport_config.max_idle_timeout(Some(Duration::from_hours(1).try_into()?));

    let transport_config = Arc::new(transport_config);
    let mut server_config =
        ServerConfig::with_crypto(Arc::new(QuicServerConfig::try_from(server_crypto)?));
    server_config.transport = transport_config;

    let endpoint = Endpoint::server(
        server_config,
        SocketAddr::new(IpAddr::V4(Ipv4Addr::new(0, 0, 0, 0)), config.quic.port),
    )?;

    println!("[!] Listening to QUIC on {:?}", endpoint.local_addr());

    while let Some(connecting) = endpoint.accept().await {
        let connection = match connecting.await {
            Ok(con) => con,
            Err(_) => continue,
        };

        println!("[!] Got a Connection from {}", connection.remote_address());

        tokio::spawn(async move {
            let sockdat: Arc<Mutex<Option<SocketData>>> = Arc::new(Mutex::new(None));

            loop {
                match connection.accept_bi().await {
                    Ok((mut send, mut recv)) => {
                        let sockdat = sockdat.clone();
                        let (tx_msg, mut rx_msg) = tokio::sync::mpsc::channel::<Vec<u8>>(64);
                        let tx_handle = tokio::spawn(async move {
                            while let Some(msg) = rx_msg.recv().await {
                                let _ = send.write_all(&frame_packet(&msg)).await;
                            }
                        });
                        let tx_msg = tx_msg.clone();
                        let rx_handle = tokio::spawn(async move {
                            loop {
                                let packet_size = match recv.read_u32().await {
                                    Ok(size) => size,
                                    Err(_) => break,
                                };
                                let mut packet = vec![0u8; packet_size as usize];
                                if let Err(err) = recv.read_exact(&mut packet).await {
                                    println!("Read failed : {}", err); // temp
                                    break;
                                }
                                realtime::listener(packet, &sockdat, &tx_msg).await
                            }
                        });

                        _ = tokio::join!(tx_handle, rx_handle);
                    },
                    Err(conn_err) => {
                        println!(
                            "[-] Connection {} Dropped : {}",
                            connection.remote_address(),
                            conn_err
                        );

                        break;
                    },
                }
            }
        });
    }

    Ok(())
}
