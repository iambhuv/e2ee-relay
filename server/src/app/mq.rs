pub mod nats {
    use crate::MQ_POOL;
    use crate::utils::config::AppConfig;

    pub async fn connect(config: &AppConfig) {
        let client =
            match async_nats::connect(format!("{}:{}", config.nats.server, config.nats.port)).await
            {
                Ok(client) => {
                    println!("[+] Connected to NATS MQ");
                    client
                },
                Err(err) => {
                    eprintln!("[-] NATS Connection Fail with Error : {}", err.kind());
                    std::process::exit(1)
                },
            };

        MQ_POOL.set(client).ok();
    }
}
