use bincode::{Decode, config::Configuration, error::DecodeError};
use serde::Deserialize;

// use crate::events::{EventTrait};

// pub fn decode_event<T: Deserialize>(event: &[u8]) -> Result<T, rmp_serde::decode::Error> {
//     let data = rmp_serde::from_slice::<T>(event)?;
//     Ok(data)
// }
