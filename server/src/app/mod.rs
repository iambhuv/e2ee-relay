pub mod db;
pub mod mq;
mod listener;

pub use listener::serve_quic;

pub fn init() {
    print!("\x1Bc"); // Clear Terminal
    println!("[!] Welcome to P2PE2E!");

    if dotenv::dotenv().is_err() {
        eprintln!("[-] Failed to load .env File")
    };
}
