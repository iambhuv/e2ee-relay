use common::get_static_keypair;

use jni::JNIEnv;
use jni::objects::{JClass, JObject, JValue};
use jni::sys::jobject;

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

    let secret_jarray = match env.byte_array_from_slice(&secret_bytes) {
        Ok(arr) => arr,
        Err(e) => {
            let _ = env.throw_new(
                "java/lang/RuntimeException",
                format!("Failed to create secret key array: {}", e),
            );

            return JObject::null().into_raw();
        }
    };

    let public_jarray = match env.byte_array_from_slice(&public_bytes) {
        Ok(arr) => arr,
        Err(e) => {
            let _ = env.throw_new(
                "java/lang/RuntimeException",
                format!("Failed to create public key array: {}", e),
            );
            return JObject::null().into_raw();
        }
    };

    let pair_class = match env.find_class("kotlin/Pair") {
        Ok(cls) => cls,
        Err(e) => {
            let _ = env.throw_new(
                "java/lang/RuntimeException",
                format!("Failed to find Pair class: {}", e),
            );
            return JObject::null().into_raw();
        }
    };

    let pair_obj = match env.new_object(
        pair_class,
        "(Ljava/lang/Object;Ljava/lang/Object;)V",
        &[
            JValue::Object(&JObject::from(secret_jarray)),
            JValue::Object(&JObject::from(public_jarray))
        ]
    ) {
        Ok(obj) => obj,
        Err(e) => {
            let _ = env.throw_new("java/lang/RuntimeException", format!("Failed to create Pair object: {}", e));
            return JObject::null().into_raw();
        }
    };

    pair_obj.into_raw()
}
