pub mod nats {
    use crate::{utils::config::AppConfig, MQ_POOL};

    pub async fn connect(config: &AppConfig) {
        let client = match async_nats::connect(format!("{}:{}", config.nats.server, config.nats.port)).await{
          Ok(client) => {
            println!("[+] Connected to NATS MQ");
            client
          },
          Err(err) => {
            panic!("[-] NATS Connection Fail with Error : {}", err.kind());
          }
        };

        MQ_POOL.set(client).ok();
    }
}
