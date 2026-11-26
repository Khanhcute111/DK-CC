# DK-CC — OpenReel Android

Android adaptation of [OpenReel Video](https://github.com/Augani/openreel-video), maintained separately from the upstream project.

## Product goals

- Android APK with a Vietnamese, touch-first interface.
- Preserve real video-editing workflows; this is not an AI-only video generator.
- Prefer on-device media processing and keep user media local.
- Target mid-range Android devices first, including the Xiaomi Redmi Note 13 Pro 4G.
- Never advertise an export format, frame rate, or editing tool as supported until it is implemented and tested on-device.

## Important implementation status

This repository was initially empty. The upstream repository currently contains a web editor and Android-port documentation, but the documented native Android implementation is not present in the upstream `main` source tree. Documentation is not a compilable Android application.

For that reason, this repository does not claim to contain a working APK yet. The Android app must be implemented and verified in stages rather than shipping a superficial WebView shell and calling it a full editor.

## Planned architecture

1. Native Android shell and Vietnamese navigation (Jetpack Compose).
2. Media import through Android Photo Picker / Storage Access Framework.
3. Native playback and export pipeline (AndroidX Media3).
4. Editing project model, timeline, undo/redo, autosave and recovery.
5. Effects and color pipeline, then audio editing and captions.
6. Device-level tests for import, preview, save/reopen, export, memory use and thermal stability.

## Upstream and license

The upstream project is MIT-licensed. This adaptation must retain the upstream copyright and license notices for any upstream code that is copied or adapted. See [LICENSE](https://github.com/Augani/openreel-video/blob/main/LICENSE).

## Build status

No APK has been built or device-tested at this initial commit. Build instructions will be added with the first compilable Android milestone.
