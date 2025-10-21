use common::{get_ephemeral_keypair, get_shared_key};
use jni::{
    JNIEnv,
    objects::{JByteArray, JClass, JObject, JString, JValue},
    sys::{jlong, jobject},
};

use crate::utils::{KeyConversion, get_pair_object};

#[unsafe(no_mangle)]
pub extern "C" fn Java_com_promtuz_rust_Crypto_getEphemeralKeypair<'local, 'jval>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
) -> jobject {
    let res = std::panic::catch_unwind(move || {
        // Pair(SecretKeyPointer, PublicKeyBytes)
        let (esk, epk) = get_ephemeral_keypair();
        log::debug!("Got Epehem KP? {:?}", epk);
        let esk_ptr = Box::new(esk);
        let public_jarray = env
            .byte_array_from_slice(epk.as_bytes())
            .expect("Failed to allocate public buffer");
        log::debug!("Jarray {:?}", public_jarray);

        let jarray_obj: JObject<'local> = JObject::from(public_jarray);

        let long_class = env.find_class("java/lang/Long").unwrap();
        let long_obj = env
            .new_object(
                long_class,
                "(J)V",
                &[JValue::Long(Box::into_raw(esk_ptr) as jlong)],
            )
            .unwrap();

        get_pair_object(
            &mut env,
            JValue::Object(&long_obj),
            JValue::Object(&jarray_obj),
        )
    });

    match res {
        Ok(o) => o,
        Err(err) => {
            log::error!("Panic on getEphemeralKeypair : {:#?}", err);
            JObject::null().as_raw()
        }
    }
}

#[unsafe(no_mangle)]
pub extern "C" fn Java_com_promtuz_rust_Crypto_deriveSharedKey<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass,
    raw_key: JByteArray<'local>,
    salt: JString<'local>,
    info: JString<'local>,
) -> JByteArray<'local> {
    let salt: String = env.get_string(&salt).unwrap().into();
    let info: String = env.get_string(&info).unwrap().into();

    let raw_shared_key_slice = raw_key.to_bytes(&mut env);

    let shared_key = get_shared_key(&raw_shared_key_slice, &salt, &info);

    env.byte_array_from_slice(&shared_key).unwrap()
}
