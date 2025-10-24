use bincode::Decode;
use bincode::Encode;
use common::PublicKey;
use common::StaticSecret;
use reqwest::StatusCode;

// #[derive(Encode, Decode)]
pub struct User {
    pub public: PublicKey,
    pub secret: StaticSecret,
}

#[derive(Encode, Decode)]
/**
 * (PubKey, SecKey)
 */
pub struct UserFile(pub [u8; 32], pub [u8; 32]);

impl User {
    pub async fn register(&self) -> bool {
        let client = reqwest::Client::new();
        let public = self.public.as_bytes().to_vec();

        match client.post("http://0.0.0.0:1729/register").body(public).send().await {
            Err(err) => {
                let status = err.status().expect("[-] Server did not return Status Code");

                match status {
                    StatusCode::BAD_REQUEST => {
                        panic!("[-] Is this key bad? : {:?}", self.public)
                    },
                    StatusCode::INTERNAL_SERVER_ERROR => {
                        panic!("[-] Server gave up, me too")
                    },
                    _ => {
                        panic!("[-] Unknown Status Code by Server : {}", status)
                    },
                }
            },
            Ok(_) => {
                println!("[+] Public Key Registered, Rerun")
            },
        };

        true
    }
}
