use common::get_static_keypair;

use jni::JNIEnv;
use jni::objects::{JClass, JObject, JValue};
use jni::sys::jobject;

use crate::utils::get_pair_object;
pub mod crypto;
pub mod insecure;
pub mod quic;
pub mod utils;

// getStaticKey -> Java_com_promtuz_chat_native_Core_getStaticKey

/* Example JNI Function
#[unsafe(no_mangle)]
pub extern "C" fn Java_com_promtuz_rust_Core_NAME(
  mut env: JNIEnv,
  _class: JClass,
  // Further Arguments
) {
  // Body
}
*/

/**
*
```kt
Core {
 external fun getStaticKeypair(): Pair
}
```
*
*/
#[unsafe(no_mangle)]
pub extern "C" fn Java_com_promtuz_rust_Core_getStaticKeypair(
    mut env: JNIEnv,
    _class: JClass,
) -> jobject {
    let (secret, public) = get_static_keypair();

    let secret_bytes = secret.to_bytes();
    let public_bytes = public.to_bytes();

    let secret_jarray = env.byte_array_from_slice(&secret_bytes).unwrap();
    let public_jarray = env.byte_array_from_slice(&public_bytes).unwrap();

    get_pair_object(
        &mut env,
        JValue::Object(&JObject::from(secret_jarray)),
        JValue::Object(&JObject::from(public_jarray)),
    )
}