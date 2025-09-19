use axum::Router;
use axum::body::Bytes;
use axum::extract::WebSocketUpgrade;
use axum::extract::ws::Message;
use axum::extract::ws::WebSocket;
use axum::response::IntoResponse;
use axum::routing::any;
use common::PublicKey;
use common::encrypt_data;
use common::events::client::Events;
use common::events::client::payloads::ServerHelloPayload;
use common::events::server;
use common::get_ephemeral_keypair;
use common::get_nonce;
use common::get_shared_key;
use common::shared::info;
use common::shared::salts;
use futures::SinkExt;
use futures::StreamExt;
use tokio::sync::mpsc::Sender;

pub fn routes() -> Router {
    println!("Listening to /");

    Router::new().route("/socket", any(handler))
}

async fn handler(ws: WebSocketUpgrade) -> impl IntoResponse {
    ws.on_upgrade(handle_socket)
}

// |== NEED SEPERATE FILE ==|

/**
 * Client::(EpK, IpK)
 */
struct SDClient(PublicKey, PublicKey);
/**
 * Server::(EsK, EpK)
 */
struct SDServer(PublicKey);

struct SocketData {
    client: SDClient,
    server: SDServer,
    shared: [u8; 32],
    identity_proof: [u8; 32],
}

// fn get_sdserver() {
//     let (sec, r#pub) = get_ephemeral_keypair();
//     sec.
// }

async fn send_msg(ws: &Sender<Message>, ev: Events) -> bool {
    ws.send(Message::Binary(Bytes::from(rmp_serde::to_vec(&ev).unwrap()))).await.is_err()
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

    let mut sockdat: Option<SocketData> = None;

    while let Some(msg) = rx.next().await {
        // TODO: Gotta Separate em
        match msg {
            Ok(Message::Binary(bytes)) => {
                println!("[+] Message: {:?}", bytes);
                if let Ok(data) = rmp_serde::from_slice::<server::Events>(&bytes) {
                    match data {
                        server::Events::ClientHello(payload) => {
                            let c_epk = PublicKey::from(payload.epeheral_pubkey);
                            let c_ipk = PublicKey::from(payload.identity_pubkey);

                            let (s_esk, s_epk) = get_ephemeral_keypair();

                            // Server::EphemeralSecretKey + Client::IdentityPublicKey
                            let shared_secret = s_esk.diffie_hellman(&c_ipk);

                            let shared = get_shared_key(
                                shared_secret,
                                salts::HANDSHAKE,
                                info::SERVER_RESPONSE_TO_CLIENTHELLO,
                            );

                            let identity_proof = get_nonce::<32>();

                            sockdat = Some(SocketData {
                                client: SDClient(c_epk, c_ipk),
                                server: SDServer(s_epk),
                                shared,
                                identity_proof,
                            });

                            // Preparing Challenge for Client
                            let message = encrypt_data(
                                &identity_proof,
                                &shared,
                                &rmp_serde::to_vec(&payload).unwrap(),
                            );

                            let server_hello =
                                ServerHelloPayload { ephemeral_pubkey: s_epk.to_bytes(), message };

                            if send_msg(&tx_msg, Events::SeverHello(server_hello)).await {
                                return;
                            }
                        },
                        server::Events::Connect(payload) => todo!(),
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
