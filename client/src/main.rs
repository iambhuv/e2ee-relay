use std::error::Error;
use std::fs;
use std::io::ErrorKind;
use std::io::Write;
use std::io::stdin;
use std::io::stdout;

use bincode::Decode;
use bincode::Encode;
use bincode::config::Configuration;
use common::EncryptedData;
use common::PublicKey;
use common::SharedSecret;
use common::StaticSecret;
use common::decrypt_data;
use common::encrypt_data;
use common::events::client;
use common::events::client::UnsafeSeverHello;
use common::events::client::UnsafeSeverReject;
use common::events::client::payloads::ServerRejectReasons;
use common::events::server::Events;
use common::events::server::UnsafeClientHello;
use common::events::server::payloads::ClientHelloPayload;
use common::events::server::payloads::ConnectPayload;
use common::get_ephemeral_keypair;
use common::get_shared_key;
use common::shared::info;
use common::shared::salts;
use futures_util::SinkExt;
use futures_util::StreamExt;
use tokio::net::TcpStream;
use tokio::sync::OnceCell;
use tokio::try_join;
use tokio_tungstenite as ws;
use tokio_tungstenite::MaybeTlsStream;
use tokio_tungstenite::WebSocketStream;
use tokio_tungstenite::tungstenite::Bytes;
use tokio_tungstenite::tungstenite::Message;
use tokio_tungstenite::tungstenite::http::StatusCode;

use crate::realtime::handshake::send_hello;
use crate::realtime::listener;
use crate::utils::b2h;
use crate::utils::get_user_file;
use crate::utils::mask_key;
use crate::utils::user::User;

pub mod realtime;
pub mod utils;

/**
 * sends payload encrypted
 */
async fn send_msg(ws: &mut WebSocketStream<MaybeTlsStream<TcpStream>>, ev: Events) -> bool {
    let shared_secret = SHARED_SECRET.get().unwrap();

    let key = get_shared_key(shared_secret, salts::EVENT, info::CLIENT_EVENT_CL_TO_SV);

    // Why empty ad? idk
    let data = encrypt_data(&rmp_serde::to_vec(&ev).unwrap(), &key, &[]);

    let bytes = Bytes::from(rmp_serde::to_vec(&data).unwrap());
    ws.send(Message::Binary(bytes)).await.is_err()
}

// struct ConnectionState {
//     id_proof: [u8; 32],
// }

static SHARED_SECRET: OnceCell<SharedSecret> = OnceCell::const_new();
static USER: OnceCell<User> = OnceCell::const_new();

pub type WebSocket = WebSocketStream<MaybeTlsStream<TcpStream>>;

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

    // Connecting to Relay Server
    // For Realtime ofc
    // No RESTApi btw

    let (mut ws, resp) = ws::connect_async("ws://0.0.0.0:1729/socket").await?;

    if resp.status() == StatusCode::SWITCHING_PROTOCOLS {
        println!("[+] Connected to RealTime Relay Server!");
    }

    println!("[!] Waiting for Secure Channel!");

    /*
     - Connection Steps

     1. Sending ClientHello
    */

    let (esk, epk) = get_ephemeral_keypair();

    let mut esk = Some(esk);

    send_hello(&mut ws, epk).await;

    let handle = tokio::spawn(async move {
        while let Some(packet) = ws.next().await {
            if let Err(er) = listener(packet, &mut ws, (&mut esk, epk)).await {
                eprintln!("{}", er);
            }
        }
    });

    let mut app_running = true;

    while app_running {
        let mut input = String::new();
        print!("[>] ");
        stdout().flush().ok();
        stdin().read_line(&mut input).expect("[-] IO Error");

        println!("[!] NICE NICE NICE : {}", input)
    }

    try_join!(handle).ok();

    Ok(())
}
