use serde::{Deserialize, Serialize};

use crate::EncryptedData;

#[derive(Serialize, Deserialize, Debug)]
pub struct ServerHelloPayload {
    #[serde(with = "serde_bytes")]
    pub epk: [u8; 32],
    pub msg: EncryptedData,
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
