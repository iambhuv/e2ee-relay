// use std::vec;
use chacha20poly1305::{
    AeadCore, ChaCha20Poly1305, Error, Key, KeyInit,
    aead::{AeadMutInPlace, OsRng, rand_core::RngCore},
};

use hkdf::Hkdf;
use serde::{Deserialize, Serialize};
use sha2::Sha256;

pub use x25519_dalek::{EphemeralSecret, PublicKey, SharedSecret, StaticSecret};

// pub mod constants;
mod constants;
pub mod events;
pub mod utils;

pub use constants::quic;
pub use constants::shared;

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct Bytes(#[serde(with = "serde_bytes")] pub Vec<u8>);

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct KeyBytes(#[serde(with = "serde_bytes")] pub [u8; 32]);

//extern mod EphemeralSecret;

pub fn add(left: u64, right: u64) -> u64 {
    left + right
}

pub fn get_secret_key() -> StaticSecret {
    StaticSecret::random_from_rng(&mut OsRng)
}

pub fn derive_public_key(key: &StaticSecret) -> PublicKey {
    PublicKey::from(key)
}

pub fn get_ephemeral_keypair() -> (EphemeralSecret, PublicKey) {
    let esk = EphemeralSecret::random_from_rng(&mut OsRng);
    let epk = PublicKey::from(&esk);

    (esk, epk)
}

pub fn get_static_keypair() -> (StaticSecret, PublicKey) {
    let esk = StaticSecret::random_from_rng(&mut OsRng);
    let epk = PublicKey::from(&esk);

    (esk, epk)
}

pub fn get_nonce<const N: usize>() -> [u8; N] {
    let mut nonce = [0u8; N];
    OsRng.fill_bytes(&mut nonce);
    nonce
}

/// SharedSecret
pub fn get_shared_key(shared_secret: &[u8; 32], salt: &str, info: &str) -> [u8; 32] {
    let key = Hkdf::<Sha256>::new(Some(salt.as_bytes()), shared_secret);
    let mut okm = [0u8; 32];
    let _ = key.expand(info.as_bytes(), &mut okm);

    okm
}

#[derive(Serialize, Deserialize, Debug)]
pub struct EncryptedData {
    #[serde(with = "serde_bytes")]
    pub nonce: Vec<u8>,
    #[serde(with = "serde_bytes")]
    pub cipher: Vec<u8>,
}

/// 
/// data -> any data ofc
/// 
/// key -> derived using [`get_shared_key`]
/// 
/// ad -> Authentication Data, supposedly prevents MITM/Relay Attacks
/// 
pub fn encrypt_data(data: &[u8], key: &[u8; 32], ad: &[u8]) -> EncryptedData {
    #[allow(deprecated)]
    let mut chacha20 = ChaCha20Poly1305::new(Key::from_slice(key));
    let nonce = ChaCha20Poly1305::generate_nonce(&mut OsRng);

    let mut cipher = Vec::from(data);

    // Assuming that error is negligible
    chacha20.encrypt_in_place(&nonce, ad, &mut cipher).ok();

    EncryptedData {
        nonce: nonce.to_vec(),
        cipher,
    }
}

pub fn decrypt_data(data: EncryptedData, key: &[u8; 32], ad: &[u8]) -> Result<Vec<u8>, Error> {
    #[allow(deprecated)]
    let mut chacha20 = ChaCha20Poly1305::new(Key::from_slice(key));

    let mut buffer = Vec::from(data.cipher);

    chacha20
        .decrypt_in_place(data.nonce.as_slice().into(), &ad, &mut buffer)
        .map(|_| buffer)
}

pub fn frame_packet(packet: &[u8]) -> Vec<u8> {
    let size: [u8; 4] = (packet.len() as u32).to_be_bytes();
    [&size, packet].concat()
}
