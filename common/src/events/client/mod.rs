/*
  Client Events
*/

use serde::{Deserialize, Serialize};

use crate::events::{client::payloads::ServerHelloPayload, EventTrait};

pub mod payloads;

#[derive(Serialize, Deserialize)]
pub struct UnsafeSeverHello(pub ServerHelloPayload);

#[derive(Serialize, Deserialize)]
#[serde(rename_all = "SCREAMING_SNAKE_CASE")]
pub enum Events {
  Accept()
}

impl EventTrait for Events {}