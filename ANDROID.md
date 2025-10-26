# Android App

Core Native Stuff in Rust compiled to "libcore", imported to app using NDK

> !INFO
> Gotta use QUIC

On App open it will check for existing / already stored keys and the data correspoding to it

if doesnt, it will generate a fresh account and try to connect to server

Register the token if it isn't, which it wont for first time ofc,
might need a captcha or something ig

An empty app is shown to the user ofc,

by design the user is supposed to be anonymous, but user CAN set decorations on profile like name, display picture, bio etc

this are updated on the database unencrypted for public use and so are completely optional to add

User can talk to other people using their public keys by design, or for convenience share around their "QR Code" which basically contains the Public Key

## First Screen (First Time)

done

## Chats

using room? or some sort of encrypted db? sqlite? i wonder
each chat's identifier should be other user's identity public key, *makes sense ig*

chats stored have no "basis" of pre-existing on the device

chats can either be 
  saved via scanning qr codes of other users
  or receiving a (message request)? from "unknown" user