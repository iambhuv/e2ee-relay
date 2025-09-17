use axum::{
    extract::{
        ws::{
            Message,
            WebSocket,
        }, WebSocketUpgrade
    }, response::{IntoResponse, Response}, routing::any, Router
};
use common::events::server;
use futures::{
    SinkExt,
    StreamExt,
};

pub fn routes() -> Router {
    println!("Listening to /");

    Router::new().route("/socket", any(handler))
}

async fn handler(ws: WebSocketUpgrade) -> impl IntoResponse {
    ws.on_upgrade(handle_socket)
}

async fn handle_socket(socket: WebSocket) {
    let (mut tx_ws, mut rx) = socket.split();
    let (tx_msg, mut rx_msg) = tokio::sync::mpsc::channel::<Message>(64);

    // Spawn forwarder: from mpsc to websocket
    tokio::spawn(async move {
        while let Some(msg) = rx_msg.recv().await {
            if tx_ws.send(msg).await.is_err() {
                break;
            }
        }
    });

    while let Some(msg) = rx.next().await {
        match msg {
            Ok(Message::Binary(bytes)) => {
                println!("[+] Message: {:?}", bytes);
                if let Ok(data) = rmp_serde::from_slice::<server::Events>(&bytes) {
                    match data {
                        server::Events::ClientHello(payload) => {
                            println!("GOT CLIENT HELLO EVENT : {:?}", payload)
                        },
                    }
                } else {
                    return;
                }
            },
            Ok(Message::Close(_)) => {
                break;
            },
            Err(_) => {
                break;
            },
            _ => {},
        }
    }
}
