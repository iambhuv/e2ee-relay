pub mod scylla {
    use scylla::client::session_builder::SessionBuilder;

    use crate::SC_POOL;
    use crate::utils::config::AppConfig;

    pub async fn connect(config: &AppConfig) {
        let mut sbuilder = SessionBuilder::new();

        for snode in &config.scylla.known_nodes {
            sbuilder = sbuilder.known_node(snode);
        }

        let scylla_session = sbuilder.build().await.expect("[-] Failed to connect scylla");
        scylla_session.use_keyspace(&config.scylla.keyspace, true).await.ok();

        println!("[+] Connected to ScyllaDB : true");
        scylla_session.get_cluster_state().get_nodes_info().iter().for_each(|node| {
            println!("[+] *Cluster [{}] Connected : {}", node.address, node.is_connected())
        });

        SC_POOL.set(scylla_session).unwrap();
    }
}
