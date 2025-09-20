use axum::Router;
use axum::routing::post;

use crate::pages::ui_routes;
use crate::realtime;

mod register;

pub use register::register;

pub fn get_routes() -> Router {
    let mut router = Router::new();

    router = router.merge(realtime::routes());
    router = router.route("/register", post(register));

    router = router.nest("/ui", ui_routes());

    router
}
