use common::EncryptedData;
use common::EphemeralSecret;
use common::PublicKey;
use common::decrypt_data;
use common::events::client;
use common::events::client::UnsafeSeverHello;
use common::events::client::UnsafeSeverReject;
use common::events::client::payloads::ServerRejectReasons;
use common::events::server::Events;
use common::events::server::payloads::ClientHelloPayload;
use common::events::server::payloads::ConnectPayload;
use common::get_shared_key;
use common::shared::info;
use common::shared::salts;
use tokio_tungstenite::tungstenite::Error;
use tokio_tungstenite::tungstenite::Message;

use crate::SHARED_SECRET;
use crate::USER;
use crate::WebSocket;
use crate::send_msg;

pub async fn listener(
    packet: Result<Message, Error>, ws: &mut WebSocket,
    (esk, epk): (&mut Option<EphemeralSecret>, PublicKey),
) -> Result<(), Box<dyn std::error::Error>> {
    let user = USER.get().unwrap();
    match packet {
        Ok(Message::Binary(bytes)) => {
            if let Ok(data) = rmp_serde::from_slice::<UnsafeSeverHello>(&bytes) {
                let payload = data.0;

                let server_pubkey = PublicKey::from(payload.ephemeral_pubkey);

                let ad = rmp_serde::to_vec(&ClientHelloPayload {
                    ephemeral_pubkey: epk.to_bytes(),
                    identity_pubkey: USER.get().unwrap().public.to_bytes(),
                })
                .unwrap();

                // ! THE ONLY USE OF EISK IS TO DECRYPT MESSAGE IN HANDSHAKE
                let ephemeral_identity_shared_secret = user.secret.diffie_hellman(&server_pubkey);

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
                    SHARED_SECRET.set(secret).ok();
                }

                if send_msg(ws, Events::Connect(ConnectPayload { proof: data })).await {
                    println!("[-] Failed to send Connect Payload");
                }
            } else if let Ok(UnsafeSeverReject(reason)) =
                rmp_serde::from_slice::<UnsafeSeverReject>(&bytes)
            {
                println!("[-] Connection got rejected with reason : {:?}", reason);
            } else if let Ok(data) = rmp_serde::from_slice::<EncryptedData>(&bytes)
                    // && let Some(ref constate) = constate
                    && let Some(shared_secret) = SHARED_SECRET.get()
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
            if let Some(frame) = frame {
                let reason: ServerRejectReasons = frame.reason.as_str().into();

                match reason {
                    ServerRejectReasons::UnregisteredPublicKey => {
                        println!("[!] Public Key is Unregistered!");
                        if user.register().await {
                            println!("[+] Registered");
                        }
                    },
                    ServerRejectReasons::Unknown => {},
                }
            } else {
                println!("[!] Connection dropped with reason : {:#?}", frame)
            }
        },
        Ok(msg) => {
            println!("[?] Received Unknown Packet : {}", msg)
        },
        Err(err) => {
            println!("[-] RealTime Error : {}", err)
        },
    }

    Ok(())
}
