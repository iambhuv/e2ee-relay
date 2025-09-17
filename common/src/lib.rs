use chacha20poly1305::aead::OsRng;
use x25519_dalek::{PublicKey, StaticSecret};

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

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn it_works() {
        let result = add(2, 2);
        assert_eq!(result, 4);
    }
}
