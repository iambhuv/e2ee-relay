use std::sync::Arc;

use common::EncryptedData;
use common::PublicKey;
use common::decrypt_data;
use common::encrypt_data;
use common::events::client;
use common::events::client::UnsafeSeverHello;
use common::events::client::UnsafeSeverReject;
use common::events::client::payloads::ServerHelloPayload;
use common::events::server;
use common::get_nonce;
use common::get_shared_key;
use common::get_static_keypair;
use common::shared::info;
use common::shared::salts;

use crate::realtime::SocketData;
use crate::realtime::routes::MessageSender;
use crate::realtime::send_msg;
use crate::user::User;

pub async fn listener(
    bytes: Vec<u8>, sockdat: &Arc<tokio::sync::Mutex<Option<SocketData>>>, tx: &MessageSender,
) {
    if let Ok(data) = serde_cbor::from_slice::<server::UnsafeClientHello>(&bytes) {
        println!("[!] Got Client Hello : {:?}", data);

        let payload = data.0;
        let c_epk = PublicKey::from(payload.epk);
        let c_ipk = PublicKey::from(payload.ipk);

        let mut user = User::new(payload.ipk);
        let exists = user.exists().await.unwrap_or(false);

        if !exists {
            tx.send(
                serde_cbor::to_vec(&UnsafeSeverReject(
                    client::payloads::ServerRejectReasons::UnregisteredPublicKey,
                ))
                .unwrap(),
            )
            .await
            .ok();

            return;
        }

        let (s_esk, s_epk) = get_static_keypair();

        // Server::EphemeralSecretKey + Client::IdentityPublicKey
        // ! THE ONLY USE OF EISK IS TO DECRYPT MESSAGE IN HANDSHAKE
        let ephemeral_identity_shared_secret = s_esk.diffie_hellman(&c_ipk);

        let identity_proof = get_nonce::<32>();

        let shared_secret = s_esk.diffie_hellman(&c_epk);

        *sockdat.lock().await = Some(SocketData {
            user,
            client: super::SDClient(c_epk, c_ipk),
            server: super::SDServer(s_epk),
            shared_secret,
            identity_proof,
        });

        // Preparing Challenge for Client
        let msg = encrypt_data(
            &identity_proof,
            &get_shared_key(
                &ephemeral_identity_shared_secret.to_bytes(),
                salts::HANDSHAKE,
                info::SERVER_HANDSHAKE_SV_TO_CL,
            ),
            &serde_cbor::to_vec(&payload).unwrap(),
        );

        let server_hello = ServerHelloPayload { epk: s_epk.to_bytes(), msg };

        if tx.send(serde_cbor::to_vec(&UnsafeSeverHello(server_hello)).unwrap()).await.is_err() {
            return;
        }
    } else if let Ok(data) = serde_cbor::from_slice::<EncryptedData>(&bytes)
        && let Some(sockdat) = sockdat.lock().await.as_ref()
    {
        println!("[!] Got Some Encrypted Data : {:?}", data);

        // Decrypting the data using a key with proper information
        // key used in decrypting an event sent by client to server
        match decrypt_data(
            data,
            &get_shared_key(&sockdat.shared_secret.to_bytes(), salts::EVENT, info::CLIENT_EVENT_CL_TO_SV),
            &[],
        )
        .map(|dat| serde_cbor::from_slice::<server::Events>(&dat))
        {
            Ok(Ok(events)) => match events {
                server::Events::Connect(payload) => {
                    if payload.proof != sockdat.identity_proof {
                        return;
                    }
                    // Accept
                    if send_msg(&tx, &sockdat.shared_secret, client::Events::Accept()).await {
                        return;
                    }

                    // WINDOW AFTER CONN ACCEPT

                    // let nats = MQ_POOL.get().unwrap();
                    //nats.publish(subject, payload)
                    // nats.send(PublishMessage { subject: "real".into(), payload: (),
                    // reply: (), headers: () });
                },
            },
            _ => return,
        }
    } else {
        // someone spamming i suppose
        return;
    }
}
