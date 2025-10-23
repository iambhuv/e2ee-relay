## Handshake Plan

> [!NOTE]
> **Concept Implemented**
>
> There is No JWT or OAuth involved
> Only 1st Messages on Both Client & Server will be Unencrypted to identity verification and accomplish handshake

### Client: To Successful Connection with Relay
- Generates Ephemeral Key Pair `Client::{ESK,EPK}`

### Client: First Payload (1) [DONE]
```rs
struct ClientHello {
  identity_pubkey // Client::IPK
  ephemeral_pubkey // Client::EPK
}
```

### Server: To Client's First Payload

- ~~Ignore's~~ Acknowledge's `Client::EPK`
- Acknowledge's `Client::IPK`
- Creates's `Server::{EPK,ESK}`
- Diffie Hellman's `DH(Client::IPK, Server::ESK)`
- Epehemeral Identity Secret Key `Shared::EISK` = `HKDF(DH(Client::IPK, Server::ESK))`
- Randomly Generates an Ephemeral Identity Proof `Shared::EIP`
- Encrypts `Shared::EIP` using `Shared::EISK` with ChaCha20-Poly1305
  - Includes `ClientHello` Payload in AEAD preventing MITM/Replay Attack

### Server: First Payloads (2)
```rust
struct ServerHello {
  ephemeral_pubkey // Server::EPK
  message // Encrypted `Shared::EIP`
  // if public key is unregistered, captcha is sent by server
  captcha?
}
```

### Client: To Server's Hello

- Acknowledge's `Server::EPK`
- Derives its own copy of Epehemeral Identity Secret Key
  - Using `DH(Client::ISK, Server::EPK)`
- Decrypts `Shared::EIP`

### Client Connection Payload (3)
```rs
// == Encrypted using `Shared::EISK` ==
struct Connect {
  // required if captcha was present in ServerHello, 
  // missing or incorrect captcha response will cause ServerReject
  captcha?
  proof // Decrypted `Shared::EIP`
  ... // if needed
}
// == Encrypted  ==
```


### Server Response to Connection

Either Accept or Reject (by closing connection)[ if proof was wrong ]