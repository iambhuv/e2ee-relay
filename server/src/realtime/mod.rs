mod routes;
mod listener;

pub use listener::listener;

use common::{encrypt_data, events::client::Events, get_shared_key, shared::{info, salts}, PublicKey, SharedSecret};
pub use routes::routes;

use crate::{realtime::routes::MessageSender, user::User};

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

    let data = encrypt_data(&serde_cbor::to_vec(&ev).unwrap(), &key, &[]);

    let bytes = serde_cbor::to_vec(&data).unwrap();
    ws.send(bytes).await.is_err()
}