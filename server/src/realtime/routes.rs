use axum::Router;
use axum::body::Bytes;
use axum::extract::WebSocketUpgrade;
use axum::extract::ws::Message;
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
use common::events::server;
use common::get_ephemeral_keypair;
use common::get_nonce;
use common::get_shared_key;
use common::get_static_keypair;
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
    shared_secret: SharedSecret,
    identity_proof: [u8; 32],
}

// fn get_sdserver() {
//     let (sec, r#pub) = get_ephemeral_keypair();
//     sec.
// }

/**
 * sends encrypted payload to client
 */
async fn send_msg(ws: &Sender<Message>, shared_secret: &SharedSecret, ev: Events) -> bool {
    let key = get_shared_key(shared_secret, salts::EVENT, info::SERVER_EVENT_SV_TO_CL);

    let data = encrypt_data(&rmp_serde::to_vec(&ev).unwrap(), &key, &[]);

    let bytes = Bytes::from(rmp_serde::to_vec(&data).unwrap());

    ws.send(Message::Binary(bytes)).await.is_err()
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

                if let Ok(data) = rmp_serde::from_slice::<server::UnsafeClientHello>(&bytes) {
                    let payload = data.0;
                    let c_epk = PublicKey::from(payload.ephemeral_pubkey);
                    let c_ipk = PublicKey::from(payload.identity_pubkey);

                    let (s_esk, s_epk) = get_static_keypair();

                    // Server::EphemeralSecretKey + Client::IdentityPublicKey
                    // ! THE ONLY USE OF EISK IS TO DECRYPT MESSAGE IN HANDSHAKE
                    let ephemeral_identity_shared_secret = s_esk.diffie_hellman(&c_ipk);

                    let identity_proof = get_nonce::<32>();

                    let shared_secret = s_esk.diffie_hellman(&c_epk);

                    sockdat = Some(SocketData {
                        client: SDClient(c_epk, c_ipk),
                        server: SDServer(s_epk),
                        shared_secret,
                        identity_proof,
                    });

                    // Preparing Challenge for Client
                    let message = encrypt_data(
                        &identity_proof,
                        &get_shared_key(
                            &ephemeral_identity_shared_secret,
                            salts::HANDSHAKE,
                            info::SERVER_HANDSHAKE_SV_TO_CL,
                        ),
                        &rmp_serde::to_vec(&payload).unwrap(),
                    );

                    let server_hello =
                        ServerHelloPayload { ephemeral_pubkey: s_epk.to_bytes(), message };

                    if tx_msg
                        .send(Message::Binary(Bytes::from(
                            rmp_serde::to_vec(&UnsafeSeverHello(server_hello)).unwrap(),
                        )))
                        .await
                        .is_err()
                    {
                        return;
                    }
                } else if let Ok(data) = rmp_serde::from_slice::<EncryptedData>(&bytes)
                    && let Some(ref sockdat) = sockdat
                {
                    // Decrypting the data using a key with proper information
                    // key used in decrypting an event sent by client to server
                    match decrypt_data(
                        data,
                        &get_shared_key(
                            &sockdat.shared_secret,
                            salts::EVENT,
                            info::CLIENT_EVENT_CL_TO_SV,
                        ),
                        &[],
                    )
                    .map(|dat| rmp_serde::from_slice::<server::Events>(&dat))
                    {
                        Ok(Ok(events)) => match events {
                            server::Events::Connect(payload) => {
                                if payload.proof != sockdat.identity_proof {
                                    return;
                                }
                                // Accept
                                if send_msg(
                                    &tx_msg,
                                    &sockdat.shared_secret,
                                    client::Events::Accept(),
                                )
                                .await
                                {
                                    return;
                                }
                            },
                        },
                        _ => return,
                    }
                } else {
                    // someone spamming i suppose
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
