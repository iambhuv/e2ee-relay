use axum::Router;
use axum::response::Html;
use axum::response::IntoResponse;
use axum::routing::get;

async fn register() -> impl IntoResponse {
    let page_bytes = include_bytes!("./register.html");
    let page_string = unsafe { String::from_utf8_unchecked(page_bytes.to_vec()) };
    let key = std::env::var("HCAP_SITEKEY").expect("[-] Sitekey Not Found");

    Html(page_string.replace("<<SITE_KEY>>", &key))
}

pub fn ui_routes() -> Router {
    let router = Router::new();
    router.route("/register", get(register))
}
