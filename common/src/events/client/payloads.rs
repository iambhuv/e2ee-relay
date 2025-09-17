use serde::{Deserialize, Serialize};

use serde_big_array::BigArray;

#[derive(Serialize, Deserialize)]
pub struct ServerAcceptPayload {
    pub ephemeral_pubkey: [u8; 32],

    #[serde(with = "BigArray")]
    pub challenge: [u8; 64],
}
