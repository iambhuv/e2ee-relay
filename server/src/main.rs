use async_nats::Client;
use scylla::client::session::Session;
use tokio::sync::OnceCell;

use crate::utils::config::load_config;

static SC_POOL: OnceCell<Session> = OnceCell::const_new();
static MQ_POOL: OnceCell<Client> = OnceCell::const_new();

pub mod app;
pub mod pages;
pub mod realtime;
pub mod user;
pub mod utils;
pub mod quic;

#[tokio::main]
async fn main() {
    app::init();

    let config = load_config();

    app::db::scylla::connect(&config).await;
    app::mq::nats::connect(&config).await;
    
    app::serve_quic(&config).await.expect("[-] Huh?");
}
