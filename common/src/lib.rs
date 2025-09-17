use chacha20poly1305::{
    Nonce,
    aead::{OsRng, rand_core::RngCore},
};
use x25519_dalek::{EphemeralSecret, PublicKey, StaticSecret};

pub mod events;
pub mod utils;

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

pub fn get_nonce<const N: usize>() -> [u8; N] {
    let mut nonce = [0u8; N];
    OsRng.fill_bytes(&mut nonce);
    nonce
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn it_works() {
        let result = add(2, 2);
        assert_eq!(result, 4);
    }
}
