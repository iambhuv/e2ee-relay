pub mod salts {
    pub static HANDSHAKE: &str = "Handshake Salt";
    pub static EVENT: &str = "Event Salt";
}


/// 
/// no idea what im doing, definitely
/// but afaik, handshake is for unencrypted message payloads and vice versa
/// 
pub mod info {
  pub static SERVER_HANDSHAKE_SV_TO_CL: &str = "Handshake: Server->Client";
  pub static CLIENT_HANDSHAKE_CL_TO_SV: &str = "Handshake: Client->Server";

  pub static SERVER_EVENT_SV_TO_CL: &str = "Event: Server->Client";
  pub static CLIENT_EVENT_CL_TO_SV: &str = "Event: Client->Server";
}