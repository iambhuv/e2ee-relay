use std::sync::Arc;

use quinn::ClientConfig;
use quinn::rustls;

use crate::quic::insecure::SkipServerVerification;

pub mod insecure;

pub fn get_client_config() -> Result<ClientConfig, Box<dyn std::error::Error>> {
    // Create client config that accepts self-signed certs (INSECURE - dev only!)
    let mut client_crypto = rustls::ClientConfig::builder()
        .dangerous() // This is the key part for self-signed certs
        .with_custom_certificate_verifier(SkipServerVerification::new())
        .with_no_client_auth();

    // Set ALPN to match your server
    client_crypto.alpn_protocols = vec![b"ProtoCall".to_vec()]; // Match your server's ALPN

    let client_config = ClientConfig::new(Arc::new(
        quinn::crypto::rustls::QuicClientConfig::try_from(client_crypto)?,
    ));
    Ok(client_config)
}
