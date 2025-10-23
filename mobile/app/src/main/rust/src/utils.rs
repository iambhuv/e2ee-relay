use common::{PublicKey, StaticSecret};
use jni::{
    JNIEnv,
    objects::{JByteArray, JValue},
    sys::jobject,
};

pub fn get_pair_object(env: &mut JNIEnv, first: JValue, second: JValue) -> jobject {
    let pair_class = env
        .find_class("kotlin/Pair")
        .expect("kotlin.Pair is not found.");

    env.new_object(
        pair_class,
        "(Ljava/lang/Object;Ljava/lang/Object;)V",
        &[JValue::from(first), JValue::from(second)],
    )
    .expect("Failed to create Pair object")
    .as_raw()
}

pub trait KeyConversion {
    fn to_bytes(self, env: &JNIEnv) -> [u8; 32];
    fn to_public(self, env: &mut JNIEnv) -> PublicKey;
    fn to_secret(self, env: &mut JNIEnv) -> StaticSecret;
}

impl KeyConversion for JByteArray<'_> {
    fn to_bytes(self, env: &JNIEnv) -> [u8; 32] {
        let vec_arr = env.convert_byte_array(self).unwrap();
        (*vec_arr).try_into().unwrap()
    }

    fn to_public(self, env: &mut JNIEnv<'_>) -> PublicKey {
        PublicKey::from(self.to_bytes(env))
    }

    fn to_secret(self, env: &mut JNIEnv<'_>) -> StaticSecret {
        StaticSecret::from(self.to_bytes(env))
    }
}


pub fn create_encrypted_data(
    env: &mut JNIEnv,
    nonce: Vec<u8>,
    cipher: Vec<u8>,
) -> Result<jobject, jni::errors::Error> {
    // Convert to byte arrays
    let nonce_array = env.byte_array_from_slice(&nonce)?;
    let cipher_array = env.byte_array_from_slice(&cipher)?;
    
    // Since Bytes is a value class, it's erased to ByteArray at runtime
    // So the constructor signature is still ([B[B)V
    let obj = env.new_object(
        "com/promtuz/rust/EncryptedData",
        "([B[B)V",  // Still byte arrays!
        &[
            JValue::Object(&nonce_array.into()),
            JValue::Object(&cipher_array.into()),
        ],
    )?;
    
    Ok(obj.into_raw())
}