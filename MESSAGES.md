# Sending Messages with Relay?

Hmm!, it's supposed to be as simple as possible.
Think of it as the server is the post office, but your posts are encrypted.
Once delivered the post is gone from server.

## How?

Well instead of IDs lets just use x25519 key pairs.
The Private Key is stored safely on the device,
the public key is the identifier for the others and the server

Why does the server need to identity user?
well ofc so you get your "posts" from the "post office"

1. Users can post messages to another person's address for this context its "Public Key"

How does server know this public key isn't just some random garbage?
Well the server registers user's public key on database,
making things clear for both user and the server
This doesn't do much as invasing the privacy of users

2. These posts are then queued in for eg. RabbitMQ, ready to be consumed by the receiver.

Consumed?
Yes, the user when connecting to the relay server, 
will be sent all the pending messages available to them.

---



## Registering a Public Key

`POST /register`, with 32 byte public key as body I suppose?

yes, and hcaptcha to prevent abuse.
/register with public key normally BUT server will respond with a challenge ofc to prove your ownership of the key




## NATS

*After succesful handshake*
All messages sent to subject 
PublishMessage {
  Subject -> DM:{USER_PUBLIC_KEY_HEX}
  Payload -> Payload(
    MessageCreate {
      id: {UUID},
      
      from: {SENDER_PUBLIC_KEY_BYTES},
      cipher: {ENCRYPTED_BYTES},
      nonce: {NONCE_BYTES},
      created_at: {TIMESTAMP},

      // SIGN = HMAC(HKDF(DH, `SIGNATURE:MESSAGE:{Message::id}`))
      signature: {SIGN(FROM,TO,PLAINTEXT,CREATED_AT)}
    }

    // Others
  )
}