pub mod salts {
    pub const HANDSHAKE: &str = "Handshake Salt";
    pub const EVENT: &str = "Event Salt";
}


/**
 * no idea what im doing, definitely
 * but afaik, handshake is for unencrypted message payloads and vice versa
 */
pub mod info {
  pub const SERVER_HANDSHAKE_SV_TO_CL: &str = "Handshake: Server->Client";
  pub const CLIENT_HANDSHAKE_CL_TO_SV: &str = "Handshake: Client->Server";

  pub const SERVER_EVENT_SV_TO_CL: &str = "Event: Server->Client";
  pub const CLIENT_EVENT_CL_TO_SV: &str = "Event: Client->Server";
}