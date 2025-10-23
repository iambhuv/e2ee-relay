use serde::{Deserialize, Serialize};

use crate::{events::Captcha, EncryptedData};


#[derive(Serialize, Deserialize, Debug)]
pub struct ServerHelloPayload {
    #[serde(with = "serde_bytes")]
    pub epk: [u8; 32],
    pub msg: EncryptedData,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub captcha: Option<Captcha>,
}

#[derive(Serialize, Deserialize, Debug)]
pub enum ServerRejectReasons {
    // UnregisteredPublicKey,
    MissingCaptcha,
    InvalidCaptcha,

    InvalidIdentityProof,

    Unknown,
}

impl From<&str> for ServerRejectReasons {
    fn from(value: &str) -> Self {
        match value {
            "MissingCaptcha" => Self::MissingCaptcha,
            "InvalidCaptcha" => Self::InvalidCaptcha,
            "InvalidIdentityProof" => Self::InvalidIdentityProof,
            _ => Self::Unknown,
        }
    }
}
