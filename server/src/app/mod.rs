pub mod db;
pub mod mq;
mod listener;

pub use listener::serve;

pub fn init() {
    print!("\x1Bc"); // Clear Terminal
    println!("[!] Welcome to P2PE2E!");

    dotenv::dotenv().expect("[-] Failed to load .env File");
}
