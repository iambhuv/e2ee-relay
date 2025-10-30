# Decentralization Plan

## _Ironically_ - Central DNS-like Servers

N number of central "resolver" servers, running while always talking with each other, every resolver knows the state of other resolvers.

- Resolvers Must Maintain the Network with atleast 10 Servers at once
- Each new node reports to any one resolver about its presence.
- Clients can request any resolver server about address of a working node
- Resolver will respond with a bunch of working dependable node addresses,
  along with addresses of all other resolver servers,
  Client must cache them.
- Client can maintain a real time connection with any of the node,
  after going through a proper handshake authentication
- Using Distributed Hash Table (DHT) with ephemeral entries and periodic gossip,
  lets say UserA connected on node#123 can connect with UserB on node#45
- for eg. node#45 and node#123 will create a p2p channel between them using quic,
  allowing a bi-directional stream to be used for relaying messages between UserA and UserB
