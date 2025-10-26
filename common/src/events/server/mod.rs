/*
  Server Events
*/
use serde::{Deserialize, Serialize};

use crate::events::EventTrait;

pub mod payloads;

#[derive(Serialize, Deserialize, Debug)]
pub struct UnsafeClientHello(pub super::server::payloads::ClientHelloPayload);
// #[serde(rename_all = "SCREAMING_SNAKE_CASE")]
// pub enum UnsafeEvents {
//     /**
//      * Supposedly First Event received by the Server
//      *
//      * (identity_pubkey, ephemeral_pubkey, nonce, ts, sig)
//      */
//     ClientHello(super::server::payloads::ClientHelloPayload),
// }

#[derive(Serialize, Deserialize, Debug)]
#[serde(rename_all = "SCREAMING_SNAKE_CASE")]
pub enum Events {
    Connect(super::server::payloads::ConnectPayload),

    // What Events?
}

impl EventTrait for Events {}
