use std::net::IpAddr;
use std::net::Ipv4Addr;
use std::net::SocketAddr;

use axum::Router;

use crate::utils::config::AppConfig;

pub async fn serve(config: &AppConfig, router: Router) -> Result<(), std::io::Error> {
    let listener = tokio::net::TcpListener::bind(SocketAddr::new(
        IpAddr::V4(Ipv4Addr::new(0, 0, 0, 0)),
        config.server.port,
    ))
    .await
    .unwrap();

    println!("[+] Listening at http://0.0.0.0:{}", config.server.port);

    axum::serve(listener, router.into_make_service_with_connect_info::<SocketAddr>()).await
}
