use std::fs;
use std::io::ErrorKind;

use bincode::config::Configuration;
use common::PublicKey;
use common::StaticSecret;
use common::derive_public_key;
use common::get_secret_key;

use crate::USER;
use crate::utils::user::User;
use crate::utils::user::UserFile;

pub mod user;

pub fn b2h(bytes: &[u8]) -> String {
    bytes
        .iter()
        .map(|b| format!("{:02x}", b)) // Format each byte as two lowercase hex digits
        .collect::<String>() // Collect the formatted strings into a single String
}

pub fn gen_user_credentials() -> User {
    let secret = get_secret_key();
    let public = derive_public_key(&secret);

    User { secret, public }
}

pub fn store_user(user: User) -> User {
    let userbin = bincode::encode_to_vec(
        UserFile(user.public.to_bytes(), user.secret.to_bytes()),
        bincode::config::standard(),
    )
    .expect("Unexpected Bincode Error");
    fs::write(".user", &userbin).expect("Failed to write .user File");
    user
}

pub fn get_user_file() -> bool {
    let (user, is_new) = match fs::read(".user") {
        Err(err) => {
            if err.kind() == ErrorKind::NotFound {
                (store_user(gen_user_credentials()), true)
            } else {
                panic!("Unxpected IO Error")
            }
        },
        Ok(file) => {
            match bincode::decode_from_slice::<UserFile, Configuration>(
                &file,
                bincode::config::standard(),
            ) {
                Err(_) => (store_user(gen_user_credentials()), true),
                Ok(user) => (
                    User {
                        public: PublicKey::from(user.0.0),
                        secret: StaticSecret::from(user.0.1),
                    },
                    false,
                ),
            }
        },
    };

    _ = USER.set(user);

    is_new
}

pub fn symmetric_split_range(n: usize, i: usize) -> std::ops::Range<usize> {
    return ((n / 2) - (i / 2))..((n / 2) + (i / 2));
}

pub fn mask_key(key: &String) -> String {
    let mask_range = symmetric_split_range(key.len(), key.len() - (key.len() / 4));
    let mask_len = mask_range.len();
    let mut str = String::from(key);

    str.replace_range(mask_range, &"*".repeat(mask_len));

    str
}
