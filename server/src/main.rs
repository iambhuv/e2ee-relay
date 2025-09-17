// use std::cell::OnceCell;

use std::net::{
    IpAddr,
    Ipv4Addr,
    SocketAddr,
};

use axum::{routing::get, Router};
use scylla::client::{
    session::Session,
    session_builder::SessionBuilder,
};
use tokio::sync::OnceCell;

use crate::utils::config::load_config;

static SC_POOL: OnceCell<Session> = OnceCell::const_new();

pub mod realtime;
pub mod utils;

async fn root() -> &'static str {
    "Sup"
}

#[tokio::main]
async fn main() {
    print!("\x1Bc"); // Clear Terminal
    println!("Welcome to P2PE2E!");

    let config = load_config();

    let mut sbuilder = SessionBuilder::new();

    for snode in config.scylla.known_nodes {
        sbuilder = sbuilder.known_node(snode);
    }

    let scylla_session = sbuilder.build().await.expect("Failed to connect scylla");
    scylla_session.use_keyspace(config.scylla.keyspace, true).await.ok();

    println!("Connected to ScyllaDB : true");
    scylla_session.get_cluster_state().get_nodes_info().iter().for_each(|node| {
        println!("  Cluster [{}] Connected : {}", node.address, node.is_connected())
    });

    SC_POOL.set(scylla_session).unwrap();

    let app = realtime::routes::routes().route("/", get(root));

    let listener = tokio::net::TcpListener::bind(SocketAddr::new(
        IpAddr::V4(Ipv4Addr::new(0, 0, 0, 0)),
        config.server.port,
    ))
    .await
    .unwrap();

    println!("Should be listening on http://0.0.0.0:{}", config.server.port);

    axum::serve(listener, app.into_make_service_with_connect_info::<SocketAddr>()).await.unwrap();
}
