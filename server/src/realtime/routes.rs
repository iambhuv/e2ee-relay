use async_nats::PublishMessage;
use axum::Router;
use axum::body::Bytes;
use axum::extract::WebSocketUpgrade;
use axum::extract::ws::CloseFrame;
use axum::extract::ws::Message;
use axum::extract::ws::Utf8Bytes;
use axum::extract::ws::WebSocket;
use axum::response::IntoResponse;
use axum::routing::any;
use common::EncryptedData;
use common::PublicKey;
use common::SharedSecret;
use common::decrypt_data;
use common::encrypt_data;
use common::events::client;
use common::events::client::Events;
use common::events::client::UnsafeSeverHello;
use common::events::client::payloads::ServerHelloPayload;
use common::events::client::payloads::ServerRejectReasons;
use common::events::server;
use common::get_nonce;
use common::get_shared_key;
use common::get_static_keypair;
use common::shared::info;
use common::shared::salts;
use futures::SinkExt;
use futures::StreamExt;
use tokio::sync::mpsc::Sender;

use crate::MQ_POOL;
use crate::realtime::SocketData;
use crate::realtime::listener::listener;
use crate::user::User;

pub fn routes() -> Router {
    Router::new().route("/socket", any(handler))
}

async fn handler(ws: WebSocketUpgrade) -> impl IntoResponse {
    // ws.on_upgrade(handle_socket)
}

pub type MessageSender = Sender<Vec<u8>>;

// |== NEED SEPERATE FILE ==|

// async fn handle_socket(socket: WebSocket) {
//     let (mut tx_ws, mut rx) = socket.split();
//     let (tx_msg, mut rx_msg) = tokio::sync::mpsc::channel::<Vec<u8>>(64);

//     // Spawn forwarder: from mpsc to websocket
//     tokio::spawn(async move {
//         while let Some(msg) = rx_msg.recv().await {
//             if tx_ws.send(Message::Binary(Bytes::from(msg))).await.is_err() {
//                 break;
//             }
//         }
//     });

//     let mut sockdat: Option<SocketData> = None;

//     while let Some(msg) = rx.next().await {
//         match msg {
//             Ok(Message::Binary(bytes)) => {
//                 println!("[+] Message: {:?}", bytes);

//                 listener(bytes.to_vec(), &sockdat, &tx_msg).await
//             },
//             Ok(Message::Close(_)) => {
//                 break;
//             },
//             Err(_) => {
//                 break;
//             },
//             _ => {},
//         }
//     }
// }
