## Handshake Plan
**NOTE:** There is No JWT or OAuth involved

Only 1st Messages on Both Client & Server will be Unencrypted to identity verification and accomplish handshake

### Client: To Successful Connection with Relay
- Generates Ephemeral Key Pair `Client::{ESK,EPK}`

### Client: First Payload (1) [DONE]
```rs
struct ClientHello {
  identity_pubkey // Client::IPK
  ephemeral_pubkey // Client::EPK
  timestamp // SystemTime in seconds
}
```

### Server: To Client's First Payload

- Ignore's `Client::EPK`
- Acknowledge's `Client::IPK`
- Creates's `Server::{EPK,ESK}`
- Diffie Hellman's `DH(Client::EPK, Server::ESK)`
- Epehemeral Identity Secret Key `Shared::EISK` = `HKDF(')`
- Randomly Generates an Ephemeral Identity Proof `Shared::EIP`
- Encrypts `Shared::EIP` using `Shared::EISK` with ChaCha20-Poly1305
  - Includes `ClientHello` Payload in AEAD preventing MITM/Replay Attack

### Server: First Payload (2)
```rs
struct ServerHello {
  ephemeral_pubkey // Server::EPK
  message // Encrypted `Shared::EIP`
}
```

### Client: To Server's First Payload

- Acknowledge's `Server::EPK`
- Derives its own copy of Epehemeral Identity Secret Key
  - Using `DH(Client::IPK, Server::EPK)`
- Decrypts `Shared::EIP`

### Client Connection Payload (3)
```rs
// == Encrypted using `Shared::EISK` ==
struct Connect {
  proof // Decrypted `Shared::EIP`
  ... // if needed
}
// == Encrypted  ==
```



### Server Response to Connection

Either Accept or Reject (by closing connection)[ if proof was wrong ]