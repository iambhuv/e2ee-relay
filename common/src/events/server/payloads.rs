use serde::{Deserialize, Serialize};

// use serde_big_array::BigArray;

// #[derive(Serialize, Deserialize, Debug)]
// pub struct ClientHelloPayload {
//     pub identity_pubkey: [u8; 32],
//     pub epeheral_pubkey: [u8; 32],
//     pub nonce: [u8; 16],
//     pub timestamp: i64,
    
//     #[serde(with = "BigArray")]
//     pub signature: [u8; 64],
// }

#[derive(Serialize, Deserialize, Debug)]
pub struct ClientHelloPayload {
    pub identity_pubkey: [u8; 32],
    pub epeheral_pubkey: [u8; 32],
    pub timestamp: i64
}