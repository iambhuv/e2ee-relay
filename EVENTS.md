# Realtime Events?

## Handshake Events

already discussed

## Possible Events (after handshake)

1. `QueuedMessages`?

: are messagess that are not "messages" still messages if i choose QueuedMessages?
: ig

`QueuedMessages : Vec<QueueMessage>` ?

```rs
enum QueueMessage {
  //! Generic Messaging Events

  // not struct by default so payload can be reused for
  // non queued message events
  MessageCreate(Payload)
  MessageUpdate(Payload)
  // i suppose?
  MessageDelete(Payload)
}
```
Must be in chronological order

2. `PresenceUpdate` [if opted]

maybe the "SelfUser" doesn't care about presence?
maybe User(s) doesn't doesn't want their presence shown?

> [!NOTE]
> PresenceUpdate is sent by user and not server? ig

3. 