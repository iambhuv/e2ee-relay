use axum::http;

pub mod config;


pub fn get_cookie(headers: &http::HeaderMap, key: &str) -> Option<String> {
    headers.get("cookie").and_then(|v| v.to_str().ok()).and_then(|cookies| {
        cookies.split(';').find_map(|cookie| {
            let mut parts = cookie.trim().splitn(2, '=');
            match (parts.next(), parts.next()) {
                (Some(k), Some(v)) if k == key => Some(v.to_string()),
                _ => None,
            }
        })
    })
}