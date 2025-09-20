use std::error::Error;
use std::fs;
use std::io::ErrorKind;

use bincode::Decode;
use bincode::Encode;
use bincode::config::Configuration;
use common::EncryptedData;
use common::PublicKey;
use common::SharedSecret;
use common::StaticSecret;
use common::decrypt_data;
use common::derive_public_key;
use common::encrypt_data;
use common::events::client;
use common::events::client::UnsafeSeverHello;
use common::events::client::UnsafeSeverReject;
use common::events::server::Events;
use common::events::server::UnsafeClientHello;
use common::events::server::payloads::ClientHelloPayload;
use common::events::server::payloads::ConnectPayload;
use common::get_ephemeral_keypair;
use common::get_secret_key;
use common::get_shared_key;
use common::shared::info;
use common::shared::salts;
use futures_util::SinkExt;
use futures_util::StreamExt;
use tokio::net::TcpStream;
use tokio_tungstenite as ws;
use tokio_tungstenite::MaybeTlsStream;
use tokio_tungstenite::WebSocketStream;
use tokio_tungstenite::tungstenite::Bytes;
use tokio_tungstenite::tungstenite::Message;
use tokio_tungstenite::tungstenite::http::StatusCode;

// Creates `.user` in current dir to store the keys

// #[derive(Encode, Decode)]
struct User {
    public: PublicKey,
    secret: StaticSecret,
}

#[derive(Encode, Decode)]
/**
 * (PubKey, SecKey)
 */
struct UserFile([u8; 32], [u8; 32]);

fn b2h(bytes: &[u8]) -> String {
    bytes
        .iter()
        .map(|b| format!("{:02x}", b)) // Format each byte as two lowercase hex digits
        .collect::<String>() // Collect the formatted strings into a single String
}

fn gen_user_credentials() -> User {
    let secret = get_secret_key();
    let public = derive_public_key(&secret);

    User { secret, public }
}

fn store_user(user: User) -> User {
    let userbin = bincode::encode_to_vec(
        &UserFile(user.public.to_bytes(), user.secret.to_bytes()),
        bincode::config::standard(),
    )
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
        },
        Ok(file) => {
            match bincode::decode_from_slice::<UserFile, Configuration>(
                &file,
                bincode::config::standard(),
            ) {
                Err(_) => (store_user(gen_user_credentials()), true),
                Ok(user) => (
                    User {
                        public: PublicKey::from(user.0.0),
                        secret: StaticSecret::from(user.0.1),
                    },
                    false,
                ),
            }
        },
    }
}

/**
 * sends payload encrypted
 */
async fn send_msg(
    ws: &mut WebSocketStream<MaybeTlsStream<TcpStream>>, shared_secret: &SharedSecret, ev: Events,
) -> bool {
    let key = get_shared_key(shared_secret, salts::EVENT, info::CLIENT_EVENT_CL_TO_SV);

    // Why empty ad? idk
    let data = encrypt_data(&rmp_serde::to_vec(&ev).unwrap(), &key, &[]);

    let bytes = Bytes::from(rmp_serde::to_vec(&data).unwrap());
    ws.send(Message::Binary(bytes)).await.is_err()
}

// struct ConnectionState {
//     id_proof: [u8; 32],
// }

#[tokio::main]
async fn main() -> Result<(), Box<dyn Error>> {
    print!("\x1Bc"); // Clear Terminal

    let (user, is_new) = get_user_file();

    if !is_new {
        println!("Welcome Back!");
    }
    println!("Secret [ {} ]", b2h(user.secret.as_bytes()));
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

    let hello = ClientHelloPayload {
        ephemeral_pubkey: epk.to_bytes(),
        identity_pubkey: user.public.to_bytes(),
        // timestamp: std::time::SystemTime::now()
        //     .duration_since(std::time::UNIX_EPOCH)
        //     .unwrap()
        //     .as_secs() as i64,
    };

    if ws
        .send(Message::Binary(Bytes::from(rmp_serde::to_vec(&UnsafeClientHello(hello)).unwrap())))
        .await
        .is_err()
    {
        println!("[-] Failed to Send ClientHello Message")
    } else {
        println!("[+] Sent ClientHello Message")
    }

    // let mut constate = None;
    let mut shared_secret = None;

    while let Some(packet) = ws.next().await {
        match packet {
            Ok(Message::Binary(bytes)) => {
                if let Ok(data) = rmp_serde::from_slice::<UnsafeSeverHello>(&bytes) {
                    let payload = data.0;

                    let server_pubkey = PublicKey::from(payload.ephemeral_pubkey);

                    let ad = rmp_serde::to_vec(&hello).unwrap();

                    // ! THE ONLY USE OF EISK IS TO DECRYPT MESSAGE IN HANDSHAKE
                    let ephemeral_identity_shared_secret =
                        user.secret.diffie_hellman(&server_pubkey);

                    // payload.message
                    // assuming the ad is correct, which it must be,
                    // assuming its 32 byte which it must be
                    let data: [u8; 32] = decrypt_data(
                        payload.message,
                        // Temporary Because the info is Server->Client
                        // Decrypts Server's Response, wont sent
                        &get_shared_key(
                            &ephemeral_identity_shared_secret,
                            salts::HANDSHAKE,
                            info::SERVER_HANDSHAKE_SV_TO_CL,
                        ),
                        &ad,
                    )
                    .unwrap()
                    .try_into()
                    .unwrap();

                    // let mut secret_guard = shared_secret.lock().await;
                    if let Some(esk) = esk.take() {
                        let secret = esk.diffie_hellman(&server_pubkey);
                        shared_secret = Some(secret)
                        // *secret_guard = Some(secret);
                    }

                    if send_msg(
                        &mut ws,
                        shared_secret.as_ref().unwrap(),
                        Events::Connect(ConnectPayload { proof: data }),
                    )
                    .await
                    {
                        println!("[-] Failed to send Connect Payload");
                    }

                    // constate = Some(ConnectionState {
                    //     // server: RTServer(server_pubkey),
                    //     id_proof: data,
                    // });
                } else if let Ok(UnsafeSeverReject(reason)) =
                    rmp_serde::from_slice::<UnsafeSeverReject>(&bytes)
                {
                    println!("[-] Connection got rejected with reason : {:?}", reason);
                } else if let Ok(data) = rmp_serde::from_slice::<EncryptedData>(&bytes)
                    // && let Some(ref constate) = constate
                    && let Some(ref shared_secret) = shared_secret
                {
                    // Decrypting the data using a key with proper information
                    // key used in decrypting an event sent by server to client
                    match decrypt_data(
                        data,
                        &get_shared_key(shared_secret, salts::EVENT, info::SERVER_EVENT_SV_TO_CL),
                        &[],
                    )
                    .map(|dat| rmp_serde::from_slice::<client::Events>(&dat))
                    {
                        Ok(Ok(events)) => match events {
                            client::Events::Accept() => {
                                println!("SERVER ACCEPTED THE CONNECTION HURRAYY!!")
                            },
                        },
                        _ => return Err("".into()),
                    }
                } else {
                    panic!("[-] Server Sent Unknown or Unexpected Message");
                }
            },
            Ok(Message::Close(frame)) => {
                println!("[!] Connection dropped with reason : {:#?}", frame)
            },
            Ok(msg) => {
                println!("[?] Received Unknown Packet : {}", msg)
            },
            Err(err) => {
                println!("[-] RealTime Error : {}", err)
            },
        }
    }

    Ok(())
}
