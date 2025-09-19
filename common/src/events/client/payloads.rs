use serde::{Deserialize, Serialize};

use crate::EncryptedData;

#[derive(Serialize, Deserialize, Debug)]
pub struct ServerHelloPayload {
    pub ephemeral_pubkey: [u8; 32],

    pub message: EncryptedData,
}
