use std::error::Error;
use std::net::ToSocketAddrs;

use common::SharedSecret;
use common::encrypt_data;
use common::events::server::Events;
use common::events::server::UnsafeClientHello;
use common::events::server::payloads::ClientHelloPayload;
use common::get_ephemeral_keypair;
use common::get_shared_key;
use common::shared::info;
use common::shared::salts;
use quinn::Endpoint;
use quinn::SendStream;
use tokio::io::AsyncReadExt;
use tokio::io::AsyncWriteExt;
use tokio::net::TcpStream;
use tokio::sync::OnceCell;
use tokio_tungstenite::MaybeTlsStream;
use tokio_tungstenite::WebSocketStream;

use crate::quic::get_client_config;
use crate::realtime::listener;
use crate::utils::b2h;
use crate::utils::get_user_file;
use crate::utils::mask_key;
use crate::utils::user::User;

pub mod quic;
pub mod realtime;
pub mod utils;

/**
 * sends payload encrypted
 */
async fn send_msg(tx: &mut SendStream, ev: Events) -> bool {
    let shared_secret = SHARED_SECRET.get().unwrap();

    let key = get_shared_key(shared_secret.as_bytes(), salts::EVENT, info::CLIENT_EVENT_CL_TO_SV);

    println!("CBOR of {:?} : {}", ev, hex::encode(serde_cbor::to_vec(&ev).unwrap()));

    // Why empty ad? idk
    let data = encrypt_data(&serde_cbor::to_vec(&ev).unwrap(), &key, &[]);

    let bytes = serde_cbor::to_vec(&data).unwrap();

    tx.write_all(&frame_packet(&bytes)).await.ok();
    tx.flush().await.is_err()
}

// struct ConnectionState {
//     id_proof: [u8; 32],
// }

static SHARED_SECRET: OnceCell<SharedSecret> = OnceCell::const_new();
static USER: OnceCell<User> = OnceCell::const_new();

pub type WebSocket = WebSocketStream<MaybeTlsStream<TcpStream>>;

fn frame_packet(packet: &[u8]) -> Vec<u8> {
    let size: [u8; 4] = (packet.len() as u32).to_be_bytes();
    [&size, packet].concat()
}

#[tokio::main]
async fn main() -> Result<(), Box<dyn Error>> {
    print!("\x1Bc"); // Clear Terminal

    let is_new = get_user_file();

    if !is_new {
        println!("Welcome Back!");
    }

    let user = USER.get().unwrap();

    println!("Secret [ {} ]", mask_key(&b2h(user.secret.as_bytes())));
    println!("Public [ {} ]", b2h(user.public.as_bytes()));

    let client_config = get_client_config()?;

    let mut endpoint = Endpoint::client("0.0.0.0:0".parse()?)?;
    endpoint.set_default_client_config(client_config);

    println!("[!] Connecting to Server...");

    let addr = ("127.0.0.1", 4433).to_socket_addrs()?.next().unwrap();

    let connection = endpoint
        .connect(addr, "localhost")? // "localhost" must match cert
        .await?;

    println!("[+] Connected!");

    println!("[!] Waiting for Secure Channel!");

    let (mut send, mut recv) = connection.open_bi().await?;

    let (esk, epk) = get_ephemeral_keypair();

    let mut esk = Some(esk);

    let hello = serde_cbor::to_vec(&UnsafeClientHello(ClientHelloPayload {
        epk: epk.to_bytes(),
        ipk: USER.get().unwrap().public.to_bytes(),
    }))
    .unwrap();

    send.write_all(&frame_packet(&hello)).await?;

    println!("[!] Sent Hello Packet! {}", hex::encode(hello));

    tokio::spawn(async move {
        while let Ok(packet_size) = recv.read_u32().await {
            let mut packet = vec![0u8; packet_size as usize];

            if recv.read_exact(&mut packet).await.is_err() {
                break;
            }

            if let Err(er) = listener(packet, &mut send, (&mut esk, epk)).await {
                eprintln!("{}", er);
            }
        }
    });

    endpoint.wait_idle().await;

    println!("[+] Connection closed");

    Ok(())
}
