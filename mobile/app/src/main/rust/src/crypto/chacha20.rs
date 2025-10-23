use common::{EncryptedData, decrypt_data, encrypt_data};
use jni::{
    JNIEnv,
    objects::{JByteArray, JClass, JObject},
    sys::jobject,
};

use crate::utils::{KeyConversion, create_encrypted_data};

#[unsafe(no_mangle)]
pub extern "C" fn Java_com_promtuz_rust_Crypto_decryptData<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass,

    cipher: JByteArray<'local>,
    nonce: JByteArray<'local>,
    key: JByteArray<'local>,
    ad: JByteArray<'local>,
) -> JByteArray<'local> {
    let cipher = env.convert_byte_array(cipher).unwrap();
    let nonce = env.convert_byte_array(nonce).unwrap();
    let key = key.to_bytes(&mut env);
    let ad = env.convert_byte_array(ad).unwrap();

    let data = match decrypt_data(EncryptedData { cipher, nonce }, &key, &ad) {
        Ok(dat) => dat,
        Err(err) => {
            log::error!("DecryptData Fail : {}", err);
            vec![0u8]
        }
    };

    let jarray = env.byte_array_from_slice(&data).unwrap();

    JByteArray::from(jarray)
}

#[unsafe(no_mangle)]
pub extern "C" fn Java_com_promtuz_rust_Crypto_encryptData<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass,

    data: JByteArray<'local>,
    key: JByteArray<'local>,
    ad: JByteArray<'local>,
) -> jobject {
    let data = env.convert_byte_array(data).unwrap();
    let key = key.to_bytes(&mut env);
    let ad = env.convert_byte_array(ad).unwrap();

    let data = encrypt_data(&data, &key, &ad);

    create_encrypted_data(&mut env, data.nonce, data.cipher)
        .unwrap_or_else(|_| JObject::null().as_raw())
}
