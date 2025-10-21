use common::events::server::UnsafeClientHello;
use common::events::server::payloads::ClientHelloPayload;
use common::get_ephemeral_keypair;

fn main() {
    let (esk, epk) = get_ephemeral_keypair();

    let hello = &UnsafeClientHello(ClientHelloPayload {
        epk: epk.to_bytes(),
        ipk: [
            1u8, 2u8, 1u8, 23u8, 1u8, 23u8, 1u8, 2u8, 1u8, 23u8, 1u8, 23u8, 1u8, 2u8, 1u8, 23u8,
            1u8, 23u8, 1u8, 2u8, 1u8, 23u8, 1u8, 23u8, 1u8, 2u8, 1u8, 23u8, 1u8, 23u8, 4u8, 9u8,
        ],
    });

    println!("Hello : {:?}", hello);
    println!("CBOR : {:?}", serde_cbor::to_vec(&hello).unwrap());
    println!("MPACK : {:?}", rmp_serde::to_vec(&hello).unwrap());
}
