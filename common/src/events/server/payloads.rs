use serde::{Deserialize, Serialize};

use crate::events::Captcha;

#[derive(Serialize, Deserialize, Debug, Clone, Copy)]
pub struct ClientHelloPayload {
    #[serde(with = "serde_bytes")]
    pub ipk: [u8; 32],
    #[serde(with = "serde_bytes")]
    pub epk: [u8; 32],
    // whats the point of timestamp if there is no signature?
    // which there cant be because its not straight forward authentication
    // pub timestamp: i64
}

#[derive(Serialize, Deserialize, Debug)]
/**
 * Proof is unencrypted but the whole event is supposed to be encrypted somehow
 */
pub struct ConnectPayload {
    #[serde(with = "serde_bytes")]
    pub proof: [u8; 32],
    #[serde(skip_serializing_if = "Option::is_none")]
    pub captcha: Option<Captcha>,
}
