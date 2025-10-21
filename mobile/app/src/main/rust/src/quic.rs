use std::net::SocketAddr;
use std::sync::Arc;

use common::frame_packet;
use jni::objects::{JObject, JValue};
use jni::{
    JNIEnv,
    objects::{JByteArray, JClass, JString},
    sys::{jint, jlong},
};
use quinn::ConnectionError;
use quinn::{
    ClientConfig, Connection, Endpoint,
    rustls::{self, RootCertStore, pki_types::CertificateDer},
};
use tokio::io::AsyncReadExt;
use tokio::join;
use tokio::runtime::Runtime;

use crate::insecure::SkipServerVerification;

fn get_client_config(roots: RootCertStore) -> Result<ClientConfig, Box<dyn std::error::Error>> {
    // Create client config that accepts self-signed certs (INSECURE - dev only!)z
    let mut client_crypto = if roots.is_empty() {
        rustls::ClientConfig::builder()
            .dangerous()
            .with_custom_certificate_verifier(SkipServerVerification::new())
            .with_no_client_auth()
    } else {
        rustls::ClientConfig::builder()
            .with_root_certificates(roots)
            .with_no_client_auth()
    };

    // Set ALPN to match your server
    client_crypto.alpn_protocols = common::quic::ALPN_PROTOCALL
        .iter()
        .map(|p| (*p).into())
        .collect(); // Match your server's ALPN

    let client_config = ClientConfig::new(Arc::new(
        quinn::crypto::rustls::QuicClientConfig::try_from(client_crypto)?,
    ));

    Ok(client_config)
}

fn get_endpoint(client_config: ClientConfig) -> Endpoint {
    let mut endpoint = Endpoint::client("0.0.0.0:0".parse().unwrap()).unwrap();
    endpoint.set_default_client_config(client_config);

    endpoint
}

async fn get_connection(
    host: String,
    port: i32,
    endpoint: &Endpoint,
) -> Result<Connection, ConnectionError> {
    let addr: SocketAddr = format!("{}:{}", host, port).parse().unwrap();

    // Todo: Mustn't be unexpected
    endpoint
        .connect(addr, "localhost")
        .expect("Unexpected")
        .await
}

// type Channel<T> = (tokio::sync::mpsc::Sender<T>, tokio::sync::mpsc::Receiver<T>);
struct Channel<T> {
    tx: tokio::sync::broadcast::Sender<T>,
}

struct QuicClientState {
    // runtime: Runtime,
    connection: Option<Connection>,
    channel: Channel<Vec<u8>>,
    #[allow(unused)]
    endpoint: Option<Endpoint>,
}

use once_cell::sync::Lazy;

// Global runtime
static RUNTIME: Lazy<Runtime> =
    Lazy::new(|| Runtime::new().expect("Failed to create Tokio runtime"));

// use and;

use log::{error, info};
// use android_logger::{Config,FilterBuilder};

#[unsafe(no_mangle)]
pub extern "C" fn Java_com_promtuz_rust_Core_initLogger<'local>(
    _env: JNIEnv<'local>,
    _class: JClass<'local>,
) {
    android_logger::init_once(
        android_logger::Config::default()
            .with_max_level(log::LevelFilter::Debug)
            .with_tag("RustCore"),
    );
}

#[unsafe(no_mangle)]
pub extern "C" fn Java_com_promtuz_rust_Core_quicConnect<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    host: JString<'local>,
    port: jint,
    cert_der: JByteArray<'local>,
) -> jlong /* pointer */ {
    info!("quicConnect called");

    let result = std::panic::catch_unwind(move || {
        let host: String = env.get_string(&host).expect("No host string!").into();
        let cert_bytes = if cert_der.is_null() {
            None
        } else {
            Some(env.convert_byte_array(cert_der).expect("No cert!"))
        };
        info!("Got Host String {}", host);

        // // block_on doesn't require Send
        let (endpoint, connection) = RUNTIME.block_on(async {
            info!("1. Inside Runtime Block On");

            let mut roots = RootCertStore::empty();
            if let Some(cert) = cert_bytes {
                let cert = CertificateDer::from(cert);
                roots.add(cert).expect("Failed to add cert");
            }

            info!("2. Roots? : {:?}", roots);

            let client_config = get_client_config(roots).expect("Failed to get client config");

            info!("3. Client Config : {:?}", client_config);

            let endpoint = get_endpoint(client_config);

            info!("4. Endpoint : {:?}", endpoint);

            let connection = get_connection(host, port, &endpoint).await;

            info!("5. Connection : {:?}", connection);

            (endpoint, connection)
        });

        let (tx_msg, _) = tokio::sync::broadcast::channel::<Vec<u8>>(64);

        match connection {
            Ok(conn) => {
                let state = Box::new(QuicClientState {
                    channel: Channel { tx: tx_msg },
                    connection: Some(conn),
                    endpoint: Some(endpoint),
                });
                info!("Got State {:?}", state.channel.tx);
                Box::into_raw(state) as jlong
            }
            Err(err) => {
                error!("QUIC Connection Failed : {err}");

                0
            }
        }
    });

    match result {
        Ok(ptr) => {
            info!("Did not panic, pointer -> {}", ptr);
            ptr
        }
        Err(e) => {
            info!("Panic caught in JNI: {:?}", e);
            0 // Return null pointer
        }
    }
}

#[unsafe(no_mangle)]
pub extern "C" fn Java_com_promtuz_rust_Core_quicSend<'local>(
    env: JNIEnv<'local>,
    _class: JClass<'local>,
    quic_client_state_ptr: jlong,
    byte_array: JByteArray,
) -> jint {
    let state_ptr = quic_client_state_ptr as *mut QuicClientState;
    let state = unsafe { &mut *state_ptr };

    let packet = env
        .convert_byte_array(byte_array)
        .expect("Invalid Byte Array");

    state.channel.tx.send(packet).is_err() as jint
}

// Kotlin will connect and store the connection state pointer
// kotlinx.coroutines.channels.Channel
//

#[unsafe(no_mangle)]
pub extern "C" fn Java_com_promtuz_rust_Core_quicListen<'local>(
    env: JNIEnv<'local>,
    _class: JClass<'local>,
    quic_client_state_ptr: jlong,
    callback: JObject, // : (jbyteArray) -> ()
) -> jint {
    let state_ptr = quic_client_state_ptr as *mut QuicClientState;
    let state = unsafe { &mut *state_ptr };

    // lets not forget, the handshake is needed aswell, so
    // quicConnect, quicHandshake, quicListen, and ofc quicSend
    // quicConnect will also create an internal mpsc channel, stored WITH the state
    // calling quicSend will apparently send data ON that channel
    // the quicListen can ALSO listen to that channel's rx and proxy the bytes to current tx
    let jvm = Arc::new(env.get_java_vm().expect("Can't get the JavaVM"));
    let callback_global = Arc::new(env.new_global_ref(callback).unwrap());

    if let Some(connection) = &state.connection {
        RUNTIME.block_on(async {
            while let Ok((mut tx, mut rx)) = connection.accept_bi().await {
                let mut rx_channel = state.channel.tx.subscribe();

                let forward_task = tokio::spawn(async move {
                    while let Ok(msg) = rx_channel.recv().await {
                        _ = tx.write_all(&frame_packet(&msg)).await;
                    }
                });

                let jvm = jvm.clone();
                let callback_global = callback_global.clone();
                let read_task = tokio::spawn(async move {
                    loop {
                        let jvm = jvm.clone();
                        let packet_size = match rx.read_u32().await {
                            Ok(size) => size,
                            Err(_) => break, // connection closed or error
                        };

                        let mut packet = vec![0u8; packet_size as usize];

                        if rx.read_exact(&mut packet).await.is_err() {
                            break;
                        }

                        let mut env = jvm.attach_current_thread().unwrap();
                        let jbytearray = env.byte_array_from_slice(&packet).unwrap();

                        env.call_method(
                            callback_global.as_obj(),
                            "invoke",
                            "([B)V",
                            &[JValue::Object(&JObject::from(jbytearray))],
                        )
                        .expect("Failed to invoke callback");

                        // tx_packet(&mut env, &callback, &packet);
                    }
                });

                _ = join!(forward_task, read_task);
            }
        })
    } else {
        return 1;
    }

    0 // fine
}
