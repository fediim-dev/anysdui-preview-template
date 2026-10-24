# anysdui-preview-template

A template for your own AnySDUI preview app: a small app for Android, iOS, desktop and web that joins a
room on your AnySDUI server, shows the flow pushed from Figma with your design system's real
components, and opens share links. Use this repository as a template (GitHub: "Use this template"),
set the Gradle properties below and build.

It follows [RFC-0012](https://github.com/fediim-dev/anysdui-kmp/blob/main/docs/rfc/0012-figma-screens-to-devices.md)
section 11. It uses only the published `dev.fediim` artifacts and builds your design system in; it
loads no code at run time.

## What you get

- A join screen: type a pairing code, or paste a share link.
- A room screen with the live flow, images, composites and a Follow / Explore toggle.
- Android App Links and iOS Universal Links, so a share link opens the installed app.
- Catalogs, in order: your design system first, then foundation, material3, material, charts,
  cupertino and basic last. 3D is left out (desktop needs JDK 22 and iOS needs a native bridge); a
  room that needs 3D is refused by the server.

## Settings (Gradle properties)

Set them in `gradle.properties`, or pass `-Pname=value`.

| Property | Meaning |
|---|---|
| `anysdui.version` | The AnySDUI version. While no release exists it is `0.1.0-SNAPSHOT` from mavenLocal. |
| `anysdui.sourceCommit` | The anysdui-kmp commit that snapshot is built from. |
| `preview.serverOrigin` | Your server: `https://host[:port]`. `http://localhost` and `http://127.0.0.1` are allowed for development only. |
| `preview.applicationId` | The Android application id, for example `com.example.preview`; also the iOS bundle identifier. |
| `preview.appleTeamId` | Your Apple Team ID, 10 upper-case letters or digits. |
| `preview.maxComponents`, `preview.maxPayloadBytes`, `preview.maxFrameBytes` | Optional client limits; unset means the transport's defaults. Use your server's values. |
| `designSystem.coordinates` | Comma-separated `group:artifact:version` entries or Gradle project paths (`:designsystems:sample`) compiled into the app. |
| `designSystem.renderers` | Comma-separated fully qualified names of the generated `*Renderer` objects, same order. |
| `designSystem.catalogs` | Comma-separated paths of the matching `catalog.json` files, same order. |

An invalid value stops the build naming the property, never the value.

### Your design system

The design system is compiled in, never downloaded. A Gradle module with your components, annotated
for the AnySDUI processor, generates a `*Renderer` object and a `catalog.json`; the renderer is
referenced by name from generated code and the catalog JSON is compiled in too (AnySDUI artifacts do
not ship catalog JSON).

`designsystems/sample` is the AnySDUI sample design system, copied from anysdui-kmp at
`anysdui.sourceCommit`. It is a module of this template and is never published. To see it:

```properties
designSystem.coordinates=:designsystems:sample
designSystem.renderers=dev.fediim.anysdui.designsystem.anysdui.generated.SampleDesignsystemRenderer
designSystem.catalogs=designsystems/sample/catalog.json
```

Without these properties the app shows the flow with the built-in catalogs only. For your own design
system, add a module the same way (copy `designsystems/sample/build.gradle.kts`, add it to
`settings.gradle.kts`) or publish it to your own Maven repository and list its coordinates.

## Building

dev.fediim artifacts come from one repository, chosen by `-PanysduiRepository=`:

- `mavenLocal` (default): publish AnySDUI from anysdui-kmp at the commit in `anysdui.sourceCommit`
  with `./gradlew publishToMavenLocal`. Do it on a Mac if you build the iOS app, because iOS
  libraries only build there.
- `githubPackages`: needs read access, below.

Then:

```bash
./gradlew :androidApp:assembleDebug             # Android
./gradlew :desktopApp:run --args="<share link>" # desktop; the link is optional
./gradlew :webApp:wasmJsBrowserDevelopmentRun   # web
./gradlew :app:generateIosConfig                # iOS: writes iosApp/Configuration/Preview.generated.xcconfig
open iosApp/iosApp.xcodeproj                    # iOS: build and run from Xcode
./gradlew :app:jvmTest :buildSrc:test           # tests
```

## GitHub Packages read access for a company's CI

The AnySDUI artifacts are on GitHub Packages (Maven) in the private repository
`fediim-dev/anysdui-kmp`. Your CI needs read access:

1. Your company uses its own dedicated machine account (not a person's account). fediim-dev grants that
   account read access to anysdui-kmp as an outside collaborator.
2. That account creates a **classic** personal access token with the `read:packages` scope only (the
   GitHub Packages Maven registry accepts classic tokens only). Never a `repo`-scoped token, and
   never a personal token of a fediim-dev member.
3. Store it in your CI as the secret `ANYSDUI_PACKAGES_TOKEN`; the workflow passes it to Gradle only
   through `env`, with `GITHUB_ACTOR` as the user name.
4. Locally, put `gpr.user` and `gpr.key` in your own `~/.gradle/gradle.properties` and build with
   `-PanysduiRepository=githubPackages`. Never write a token into this repository.

While `anysdui.version` is a snapshot, CI instead checks out anysdui-kmp at `anysdui.sourceCommit` and
publishes it to mavenLocal. That needs a separate credential, the secret `ANYSDUI_SOURCE_TOKEN` (a
fine-grained token with Contents: read on anysdui-kmp only, or a read-only deploy key). It goes away
at the first AnySDUI release, together with that step of `.github/workflows/build.yml`. Maven Central
distribution is planned and will remove the need for a token.

## Server settings

Share links open the installed app when the server publishes your app's identity:

- `SHARE_LINK_ANDROID_PACKAGE` (your `preview.applicationId`) and `SHARE_LINK_ANDROID_CERT_SHA256`
  (the SHA-256 fingerprint of your signing certificate, `AA:BB:...`), served as
  `/.well-known/assetlinks.json`.
- `SHARE_LINK_APPLE_APP_IDS` (`<team id>.<bundle id>`, your `preview.appleTeamId` and
  `preview.applicationId`), served as `/.well-known/apple-app-site-association` for `/view/*`.

See the [server settings](https://github.com/fediim-dev/anysdui-kmp/blob/main/docs/usage.md) in the
AnySDUI usage guide. Links open the app only on a verified https origin: Android verifies the
association on install, iOS needs the associated domain `applinks:<host>` signed with your team. On
desktop there is no link association; pass the link as the first argument or paste it.

### Deploying the web target

Serve the web build (`./gradlew :webApp:wasmJsBrowserDistribution`) from the same origin as the
server, because the image route sends no CORS headers. Send the headers of the server's own viewer
page:

```
Referrer-Policy: no-referrer
Content-Security-Policy: default-src 'none'; script-src 'self' 'wasm-unsafe-eval'; style-src 'self' 'unsafe-inline'; img-src 'self' data: blob:; connect-src 'self'; font-src 'self'; base-uri 'none'; form-action 'none'; frame-ancestors 'none'
```

(copied from anysdui-kmp `nginx.conf`.) `'wasm-unsafe-eval'` is needed by the WebAssembly build; never add
`'unsafe-eval'`. The page loads no 3D scripts.

## Images and private networks

Images come from the server origin only. AnySDUI's image loader refuses addresses in private ranges
(for example `10.0.0.0/8`, and the Android emulator's `10.0.2.2`) even when listed, so a server that
resolves to a private address shows image placeholders in the app. Use a public address for your
server, or, in development, a loopback origin (`http://127.0.0.1:8081`, with `adb reverse tcp:8081
tcp:8081` for an Android emulator): loopback over http is allowed only in debug builds.

## Bumping AnySDUI

`.github/workflows/bump-anysdui.yml` rewrites `anysdui.version` and `anysdui.sourceCommit` and opens
a pull request. It starts on a `repository_dispatch` event of type `anysdui-release` (payload
`version` and `commit`) or by hand (Actions, "bump-anysdui", Run workflow). It validates both values
(`MAJOR.MINOR.PATCH` and a 40-character hex commit) before using them. A pull request opened with
the workflow's `GITHUB_TOKEN` does not start the build workflow: run `build` by hand on the bump pull
request, then merge it.

## Security notes

- Only share links of the exact configured origin open (https; http only for a loopback development origin); the scheme, host and port are compared
  after normalising, and the socket always connects to the configured origin, never to a host a link
  names. A link of any other origin, a custom scheme, or one with a userinfo is refused before any
  socket opens.
- The pairing code and the link are kept in memory only: never saved, never logged.
- Cleartext http is allowed to `localhost` and `127.0.0.1` in debug builds only.
- No proxy credentials (such as Cloudflare Access headers) are compiled in.

## License

Apache-2.0, see [LICENSE](LICENSE).
