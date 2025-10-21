use common::PublicKey;
use common::events::server::UnsafeClientHello;
use common::events::server::payloads::ClientHelloPayload;
use futures_util::SinkExt;
use tokio_tungstenite::tungstenite::Bytes;
use tokio_tungstenite::tungstenite::Message;

use crate::USER;
use crate::WebSocket;

pub async fn send_hello(ws: &mut WebSocket, epk: PublicKey) {
    if ws
        .send(Message::Binary(Bytes::from(
            serde_cbor::to_vec(&UnsafeClientHello(ClientHelloPayload {
                epk: epk.to_bytes(),
                ipk: USER.get().unwrap().public.to_bytes(),
            }))
            .unwrap(),
        )))
        .await
        .is_err()
    {
        println!("[-] Failed to Send ClientHello Message")
    } else {
        println!("[+] Sent ClientHello Message")
    }
}
