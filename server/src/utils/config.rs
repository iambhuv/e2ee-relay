use std::{env, fs, path::Path, process::{self, exit, ExitCode}};

use serde::Deserialize;

#[derive(Deserialize, Debug)]
pub struct AppConfig {
    pub server: ServerConfig,
    pub scylla: ScyllaConfig,
    pub nats: NatsConfig,
    pub quic: QuicConfig
}

#[derive(Deserialize, Debug)]
pub struct ServerConfig {
    pub port: u16,
    pub allowed_origins: Vec<String>,
}

#[derive(Deserialize, Debug)]
pub struct ScyllaConfig {
    pub known_nodes: Vec<String>,
    pub keyspace: String,
}

#[derive(Deserialize, Debug)]
pub struct NatsConfig {
    pub server: String,
    pub port: u16,
}


#[derive(Deserialize, Debug)]
pub struct QuicConfig {
    pub port: u16,
}


pub fn load_config() -> AppConfig {
    let path = env::args().nth(1).unwrap_or_else(|| "app.toml".into());
    let path = Path::new(&path);

    if !path.exists() {
        eprintln!("Config file not found: {}", path.display());
        std::process::exit(1);
    }

    let raw = fs::read_to_string(path).expect("[-] Failed to read config");
    
    match toml::from_str(&raw) {
        Ok(conf) => { conf },
        Err(err) => {
            eprintln!("[-] Failed to parse config : {:#?}", err);
            process::exit(1);
        }
    }
}
