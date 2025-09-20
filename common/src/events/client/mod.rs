/*
  Client Events
*/

use serde::{Deserialize, Serialize};

use crate::events::{
    EventTrait,
    client::payloads::{ServerHelloPayload, ServerRejectReasons},
};

pub mod payloads;

#[derive(Serialize, Deserialize)]
pub struct UnsafeSeverHello(pub ServerHelloPayload);

#[derive(Serialize, Deserialize)]
pub struct UnsafeSeverReject(pub ServerRejectReasons);

#[derive(Serialize, Deserialize)]
#[serde(rename_all = "SCREAMING_SNAKE_CASE")]
pub enum Events {
    Accept(),
}

impl EventTrait for Events {}
