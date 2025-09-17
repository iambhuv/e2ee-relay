/*
  Server Events
*/
use serde::{Deserialize, Serialize};

use crate::events::EventTrait;

pub mod payloads;

// #[derive(Serialize, Deserialize)]
// pub struct ServerEventPayload(Events, Vec<u8>);

#[derive(Serialize, Deserialize)]
#[serde(rename_all = "SCREAMING_SNAKE_CASE")]
pub enum Events {
    /**
     * Supposedly First Event received by the Server
     *
     * (identity_pubkey, ephemeral_pubkey, nonce, ts, sig)
     */
    ClientHello(super::server::payloads::ClientHelloPayload),
}

impl EventTrait for Events {}
