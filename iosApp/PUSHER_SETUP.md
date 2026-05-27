# Pusher iOS Dependency Setup

This project uses Kotlin Multiplatform shared code plus a native iOS app entry (`iosApp`).
For iOS Pusher, use the native Swift SDK via Swift Package Manager.

## 1) Add package in Xcode

Open `multiplatform/iosApp` in Xcode, then:

1. `File` -> `Add Package Dependencies...`
2. Package URL: `https://github.com/pusher/pusher-websocket-swift`
3. Choose latest stable version
4. Add package product to target `iosApp`

## 2) Why this is platform-specific

- Android dependency is managed in Gradle/TOML (`com.pusher:pusher-java-client`).
- iOS dependency is native Swift and should be managed by Xcode SPM for this app module.

## 3) Next wiring step

After package install, create a small Swift adapter in `iosApp/iosApp` and expose calls into shared KMP flow if needed.

Suggested adapter file name: `PusherRealtimeClient.swift`
