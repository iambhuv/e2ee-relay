use serde::{Deserialize, Serialize};

pub mod client;
pub mod server;

#[derive(Serialize, Deserialize, Debug)]
pub enum CaptchaReason {
    UnregisteredPublicKey,
    SuspiciousActivity,
}

#[derive(Serialize, Deserialize, Debug)]
pub struct Captcha {
    pub reason: CaptchaReason,
}

pub trait EventTrait {}