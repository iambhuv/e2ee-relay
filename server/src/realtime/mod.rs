mod listener;

use common::PublicKey;
use common::SharedSecret;
use common::encrypt_data;
use common::events::client::Events;
use common::events::client::UnsafeSeverReject;
use common::events::client::payloads::ServerRejectReasons;
use common::get_shared_key;
use common::shared::info;
use common::shared::salts;
pub use listener::listener;
use tokio::sync::mpsc::Sender;

use crate::user::User;

pub type MessageSender = Sender<Vec<u8>>;

/**
 * Client::(EpK, IpK)
 */
pub struct SDClient(pub PublicKey, pub PublicKey);
/**
 * Server::(EsK, EpK)
 */
pub struct SDServer(pub PublicKey);

pub struct SocketData {
    pub user: User,
    pub client: SDClient,
    pub server: SDServer,
    pub shared_secret: SharedSecret,
    pub identity_proof: [u8; 32],
}

/**
 * sends encrypted payload to client
 */
pub async fn send_msg(ws: &MessageSender, shared_secret: &SharedSecret, ev: Events) -> bool {
    let key = get_shared_key(shared_secret.as_bytes(), salts::EVENT, info::SERVER_EVENT_SV_TO_CL);

    println!("CBOR of {:?} : {}", ev, hex::encode(&serde_cbor::to_vec(&ev).unwrap()));

    let data = encrypt_data(&serde_cbor::to_vec(&ev).unwrap(), &key, &[]);

    let bytes = serde_cbor::to_vec(&data).unwrap();
    ws.send(bytes).await.is_err()
}

/**
 * sends encrypted payload to client
 */
pub async fn handshake_reject(ws: &MessageSender, reason: ServerRejectReasons) {
    let rej = UnsafeSeverReject(reason);
    let bytes = serde_cbor::to_vec(&rej).unwrap();
    _ = ws.send(bytes).await;
}
