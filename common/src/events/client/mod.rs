/*
  Client Events
*/

use serde::{Deserialize, Serialize};

use crate::events::{client::payloads::ServerAcceptPayload, EventTrait};

pub mod payloads;

#[derive(Serialize, Deserialize)]
#[serde(rename_all = "SCREAMING_SNAKE_CASE")]
pub enum Events {
  /**
   * First Client Side Event Received
   * 
   * Contains:
   * 1. Server's Epehemeral Public Key
   * 2. Signature Challenge
   */
  ServerAccept(ServerAcceptPayload)
}

impl EventTrait for Events {}