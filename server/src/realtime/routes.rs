use axum::{
    Router,
    extract::{
        WebSocketUpgrade,
        ws::{
            Message,
            WebSocket,
        },
    },
    response::{
        IntoResponse,
        Response,
    },
    routing::any,
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

// |== NEED SEPERATE FILE ==|

type EpK = [u8; 32];
type EsK = [u8; 32];
type IpK = [u8; 32];

struct SDClient(EpK, IpK);
struct SDServer(EpK, EsK);

struct SocketData {
    client: SDClient,
    server: SDServer
}

// |== NEED SEPERATE FILE ==|

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
                            // Ignoring EPK
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
