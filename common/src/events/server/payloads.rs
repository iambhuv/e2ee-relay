use serde::{Deserialize, Serialize};

#[derive(Serialize, Deserialize, Debug)]
pub struct ClientHelloPayload {
    pub identity_pubkey: [u8; 32],
    pub epeheral_pubkey: [u8; 32],
    // whats the point of timestamp if there is no signature?
    // which there cant be because its not straight forward authentication
    // pub timestamp: i64
}