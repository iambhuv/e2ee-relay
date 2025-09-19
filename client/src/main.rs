use std::{
    error::Error,
    fs,
    io::{ErrorKind, Read, Write},
    time::SystemTime,
};

use bincode::{Decode, Encode, config::Configuration};
use common::{
    derive_public_key,
    events::{
        self, client,
        server::{self, Events, payloads::ClientHelloPayload},
    },
    get_ephemeral_keypair, get_nonce, get_secret_key,
};

use futures_util::{SinkExt, StreamExt, sink::Send};

use tokio::net::TcpStream;
use tokio_tungstenite::{
    self as ws, MaybeTlsStream, WebSocketStream,
    tungstenite::{Bytes, Message, WebSocket, http::StatusCode},
};

// Creates `.user` in current dir to store the keys

#[derive(Encode, Decode)]
struct User {
    public: [u8; 32],
    secret: [u8; 32],
}

fn b2h(bytes: &[u8]) -> String {
    bytes
        .iter()
        .map(|b| format!("{:02x}", b)) // Format each byte as two lowercase hex digits
        .collect::<String>() // Collect the formatted strings into a single String
}

fn gen_user_credentials() -> User {
    let secret = get_secret_key();
    let public = derive_public_key(&secret);

    User {
        secret: secret.to_bytes(),
        public: public.to_bytes(),
    }
}

fn store_user(user: User) -> User {
    let userbin = bincode::encode_to_vec(&user, bincode::config::standard())
        .expect("Unexpected Bincode Error");
    fs::write(".user", &userbin).expect("Failed to write .user File");
    user
}

fn get_user_file() -> (User, bool) {
    match fs::read(".user") {
        Err(err) => {
            if err.kind() == ErrorKind::NotFound {
                (store_user(gen_user_credentials()), true)
            } else {
                panic!("Unxpected IO Error")
            }
        }
        Ok(file) => {
            match bincode::decode_from_slice::<User, Configuration>(
                &file,
                bincode::config::standard(),
            ) {
                Err(_) => (store_user(gen_user_credentials()), true),
                Ok(user) => (user.0, false),
            }
        }
    }
}

async fn send_msg(ws: &mut WebSocketStream<MaybeTlsStream<TcpStream>>, ev: Events) -> bool {
    ws.send(Message::Binary(Bytes::from(
        rmp_serde::to_vec(&ev).unwrap(),
    )))
    .await
    .is_err()
}

#[tokio::main]
async fn main() -> Result<(), Box<dyn Error>> {
    print!("\x1Bc"); // Clear Terminal

    let (user, is_new) = get_user_file();

    if !is_new {
        println!("Welcome Back!");
    }
    println!("Secret [ {} ]", b2h(&user.secret));
    println!("Public [ {} ]", b2h(&user.public));

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

    let hello = ClientHelloPayload {
        epeheral_pubkey: epk.to_bytes(),
        identity_pubkey: user.public,
        // timestamp: std::time::SystemTime::now()
        //     .duration_since(std::time::UNIX_EPOCH)
        //     .unwrap()
        //     .as_secs() as i64,
    };

    if send_msg(&mut ws, Events::ClientHello(hello)).await {
        println!("[-] Failed to Send ClientHello Message")
    } else {
        println!("[+] Sent ClientHello Message")
    }

    while let Some(packet) = ws.next().await {
        match packet {
            Ok(Message::Binary(bytes)) => {
                let message = rmp_serde::from_slice::<client::Events>(&bytes)
                    .expect("[-] Server Sent Unknown Message");

                match message {
                    client::Events::SeverHello(payload) => {
                        println!("[/] GOT SERVER'S RESPONSE HOLY??? : {:?}", payload)
                    }
                }
            }
            Ok(msg) => {
                println!("[?] Received Unknown Packet : {}", msg)
            }
            Err(err) => {
                println!("[-] RealTime Error : {}", err)
            }
        }
    }

    Ok(())
}
