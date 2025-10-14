use serde::{Deserialize, Serialize};

use crate::EncryptedData;

#[derive(Serialize, Deserialize, Debug)]
pub struct ServerHelloPayload {
    pub ephemeral_pubkey: [u8; 32],

    pub message: EncryptedData,
}

#[derive(Serialize, Deserialize, Debug)]
pub enum ServerRejectReasons {
    UnregisteredPublicKey,

    Unknown,
}

impl From<&str> for ServerRejectReasons {
    fn from(value: &str) -> Self {
        match value {
            "UnregisteredPublicKey" => Self::UnregisteredPublicKey,
            _ => Self::Unknown,
        }
    }
}
