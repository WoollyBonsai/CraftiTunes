# Authentication & API Architecture Plan

Integrating third-party streaming services (Spotify, YT Music, Apple Music, Amazon Music) into a Minecraft mod poses unique security and architectural challenges. Because a Minecraft mod is essentially a distributed client-side application, we cannot safely embed API Client Secrets into the `.jar` file (as they can be trivially decompiled and stolen).

This document outlines the planned approach for handling authentication and API interaction for Stage 3.

## 1. Spotify Integration
Spotify provides a robust Web API and explicitly supports client-side applications via the **OAuth 2.0 PKCE (Proof Key for Code Exchange)** flow.

### Planned Flow (Direct Login via PKCE):
1. **No Client Secret:** We will register a Spotify Developer Application and obtain a `Client ID`. We do not need a `Client Secret` for PKCE.
2. **Local Web Server:** When the user clicks "Login to Spotify" in-game, the mod will temporarily spin up a lightweight Java HTTP server (e.g., `com.sun.net.httpserver.HttpServer`) listening on `http://127.0.0.1:8888/callback`.
3. **Browser Redirect:** The mod will use Java's `Desktop.getDesktop().browse()` to open the user's default web browser to the Spotify Authorization URL, passing our `Client ID` and a generated code challenge.
4. **Callback Intercept:** After the user approves the login in their browser, Spotify redirects them to `http://127.0.0.1:8888/callback?code=XYZ`.
5. **Token Exchange:** The local HTTP server intercepts the `code`, shuts itself down, and the mod exchanges the code + code verifier for an `access_token` and `refresh_token` directly with Spotify.
6. **Token Storage:** The tokens will be encrypted and saved locally in a `.craftitunes_secrets` file in the Minecraft directory.

**Playback:** Spotify's terms of service prohibit extracting raw audio streams. We will use the Web API to control playback if the user has Spotify Premium, effectively acting as a remote control. Alternatively, we can use an open-source Spotify track resolver (like `librespot` or LavaPlayer's third-party Spotify source extensions) to stream the audio directly if legal compliance allows.

## 2. YouTube Music (YT Music)
YT Music does not have a standard public OAuth API for third-party streaming apps. 

### Planned Flow (Cookie-based / Device Auth):
- We will rely on existing LavaPlayer YouTube source managers.
- For personalized libraries (Playlists, Recommendations), we will require the user to provide their YT Music `OAuth tokens` (via a device authorization flow, similar to how Smart TVs log in to YouTube: "Go to youtube.com/activate and enter code").
- LavaPlayer natively supports YouTube Device Auth. We will trigger the device auth prompt in the Minecraft UI, wait for the user to authorize on their phone/browser, and then cache the credentials.

## 3. Apple Music
Apple Music requires a Developer Token (JWT) signed by a private key associated with an Apple Developer Account.

### Planned Flow (Proxy Server):
- **Security Issue:** We cannot put the private key or a long-lived JWT into the mod. 
- **Solution:** We will likely need to host a free, lightweight proxy server (e.g., on Cloudflare Workers or Vercel) that holds the Apple Developer Private Key.
- The mod will request an ephemeral Developer Token from our proxy server.
- The mod will then use Apple's **MusicKit JS / REST API** User Token flow, opening a browser window for the user to log in with their Apple ID and returning the User Token to the mod.

## 4. Amazon Prime Music
Amazon Music is extremely locked down and lacks a public developer API for playback in unauthorized applications.

### Planned Flow:
- Reverse-engineering the Amazon Music web player APIs is fragile and often violates TOS.
- We will investigate if LavaPlayer has any community-driven Amazon Music sources. 
- If direct login is impossible, this tab may be repurposed or limited to a "Now Playing" rich presence display if the desktop app is running, rather than direct streaming integration.

## Summary of Secrets Management
- **Rule 1:** NEVER embed a Client Secret, Private Key, or Developer Token in the `craftitunes.jar`.
- **Rule 2:** Use PKCE or Device Authorization flows whenever supported by the platform.
- **Rule 3:** Store user-specific access and refresh tokens locally on the player's machine, ideally utilizing a basic encryption layer (e.g., AES with a hardware-derived key) to prevent casual token theft by other malicious mods.
