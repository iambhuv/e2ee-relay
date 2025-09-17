use serde::{Deserialize, Serialize};

use serde_big_array::BigArray;

#[derive(Serialize, Deserialize, Debug)]
pub struct ServerHelloPayload {
    pub ephemeral_pubkey: [u8; 32],

    #[serde(with = "BigArray")]
    pub message: [u8; 90],
}
