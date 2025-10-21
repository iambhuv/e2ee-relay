use common::EphemeralSecret;
use jni::{
    JNIEnv,
    objects::{JByteArray, JClass},
    sys::jlong,
};

use crate::utils::KeyConversion;

#[unsafe(no_mangle)]
pub extern "C" fn Java_com_promtuz_rust_Crypto_ephemeralDiffieHellman<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass,
    // pointer to EphemeralSecret
    ephemeral_secret_ptr: jlong,
    public_key_bytes: JByteArray,
) -> JByteArray<'local> {
    let esk = unsafe { *Box::from_raw(ephemeral_secret_ptr as *mut EphemeralSecret) };
    let public_key = public_key_bytes.to_public(&mut env);

    let shared_key = esk.diffie_hellman(&public_key);
    let shared_jarray = env.byte_array_from_slice(&shared_key.to_bytes()).unwrap();

    JByteArray::from(shared_jarray)
}

#[unsafe(no_mangle)]
pub extern "C" fn Java_com_promtuz_rust_Crypto_diffieHellman<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass,
    secret_key_bytes: JByteArray,
    public_key_bytes: JByteArray,
) -> JByteArray<'local> {
    let secret_key = secret_key_bytes.to_secret(&mut env);
    let public_key = public_key_bytes.to_public(&mut env);

    let shared_key = secret_key.diffie_hellman(&public_key);
    let shared_jarray = env.byte_array_from_slice(&shared_key.to_bytes()).unwrap();

    JByteArray::from(shared_jarray)
}
