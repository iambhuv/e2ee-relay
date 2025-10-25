#![deny(clippy::expect_used, clippy::panic, clippy::indexing_slicing)]
#![warn(clippy::unwrap_used)]
#![forbid(unsafe_code)]

use async_nats::Client;
use scylla::client::session::Session;
use tokio::sync::OnceCell;

use crate::utils::config::load_config;

static SC_POOL: OnceCell<Session> = OnceCell::const_new();
static MQ_POOL: OnceCell<Client> = OnceCell::const_new();

pub mod app;
pub mod quic;
pub mod realtime;
pub mod user;
pub mod utils;

#[tokio::main]
async fn main() {
    app::init();

    let config = load_config();

    app::db::scylla::connect(&config).await;
    app::mq::nats::connect(&config).await;

    if let Err(err) = app::serve_quic(&config).await {
        eprintln!("[-] QUIC Serve Failed with error : {:?}", err);
    };
}
