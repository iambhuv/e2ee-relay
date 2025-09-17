use std::{fs, io::ErrorKind};

use bincode::{Decode, Encode, config::Configuration};
use common::{derive_public_key, get_secret_key};

// Creates `.user` in current dir to store the keys

#[derive(Encode, Decode)]
struct User {
    public: [u8; 32],
    secret: [u8; 32],
}

fn b2h(bytes: &[u8]) -> String {
    bytes
        .iter()
        .map(|b| format!("{:02x}", b)) // Format each byte as two lowercase hex digits
        .collect::<String>() // Collect the formatted strings into a single String
}

fn gen_user_credentials() -> User {
    let secret = get_secret_key();
    let public = derive_public_key(&secret);

    User {
        secret: secret.to_bytes(),
        public: public.to_bytes(),
    }
}

fn store_user(user: User) -> User {
    let userbin = bincode::encode_to_vec(&user, bincode::config::standard())
        .expect("Unexpected Bincode Error");
    fs::write(".user", &userbin).expect("Failed to write .user File");
    user
}

fn get_user_file() -> (User, bool) {
    match fs::read(".user") {
        Err(err) => {
            if err.kind() == ErrorKind::NotFound {
                (store_user(gen_user_credentials()), true)
            } else {
                panic!("Unxpected IO Error")
            }
        }
        Ok(file) => {
            match bincode::decode_from_slice::<User, Configuration>(
                &file,
                bincode::config::standard(),
            ) {
                Err(_) => (store_user(gen_user_credentials()), true),
                Ok(user) => (user.0, false),
            }
        }
    }
}

fn main() {
    print!("\x1Bc"); // Clear Terminal

    let (user, is_new) = get_user_file();

    if !is_new {
        println!("Welcome Back!");
    }
    println!("Secret [ {} ]", b2h(&user.secret));
    println!("Public [ {} ]", b2h(&user.public));
}
