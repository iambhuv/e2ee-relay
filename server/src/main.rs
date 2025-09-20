use scylla::client::session::Session;
use tokio::sync::OnceCell;

use crate::routes::get_routes;
use crate::utils::config::load_config;

static SC_POOL: OnceCell<Session> = OnceCell::const_new();

pub mod app;
pub mod realtime;
pub mod routes;
pub mod utils;
pub mod pages;
pub mod user;

#[tokio::main]
async fn main() {
    app::init();

    let config = load_config();

    app::db::scylla::connect(&config).await;

    app::serve(&config, get_routes()).await.expect("[-] Huh?");
}
