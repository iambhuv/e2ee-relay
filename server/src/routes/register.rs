use axum::body::Bytes;
use axum::http::StatusCode;
use axum::response::IntoResponse;
use scylla::value::CqlTimestamp;
// use serde::Deserialize;

use crate::SC_POOL;

type PublicKey = [u8; 32];

// #[derive(Deserialize)]
// struct RegisterForm {
//     public_key: PublicKey,
// }

pub async fn register(body: Bytes) -> impl IntoResponse {
    let key: PublicKey = match body.as_ref().try_into() {
        Ok(key) => key,
        Err(_) => return (StatusCode::BAD_REQUEST).into_response(),
    };

    let scylla = SC_POOL.get().unwrap();

    let now = chrono::Utc::now();

    match scylla
        .query_unpaged(
            r"INSERT INTO known_users (public_key, created_at) VALUES(?, ?) IF NOT EXISTS",
            (key.as_slice(), CqlTimestamp::from(now)),
        )
        .await
    {
        Ok(_) => (StatusCode::OK).into_response(),
        Err(_) => (StatusCode::INTERNAL_SERVER_ERROR).into_response(),
    }
}
