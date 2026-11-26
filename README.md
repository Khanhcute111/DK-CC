# DK-CC — Android Video Editor

DK-CC is a native Android adaptation inspired by [OpenReel Video](https://github.com/Augani/openreel-video), maintained in a separate repository.

## Current milestone: native app foundation

The first Android foundation is now committed:

- Kotlin + Jetpack Compose application shell.
- Vietnamese project library and editor screen.
- Android document picker for importing video files without broad storage permissions.
- Media3 ExoPlayer preview for the selected imported clip.
- Local project metadata persistence using Android AtomicFile.
- GitHub Actions workflow to run unit tests and assemble a debug APK.

## Build

Requirements: JDK 17, Android SDK Platform 35, Android Build Tools 35.0.0, Gradle 8.9.

```bash
gradle testDebugUnitTest assembleDebug
```

The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`. GitHub Actions uploads it as the `dk-cc-debug-apk` artifact after a successful build.

## Planned milestones

1. **Foundation (current):** library, project persistence, video import and preview.
2. **Timeline:** clip ordering, trim in/out, seek/playhead, undo/redo and recovery.
3. **Editing:** multi-clip composition, transforms, crop, transitions and effects.
4. **Audio and text:** audio tracks, volume/fades, titles and captions.
5. **Export:** Media3 Transformer pipeline, progress/cancel, MP4 H.264 and supported device profiles.
6. **Reliability:** automated tests, large-file handling, memory/thermal profiling and device tests.

## Honest feature status

This is an early native foundation, not yet a full-featured replacement for OpenReel. At this stage, imported videos can be previewed and project metadata can be saved locally. A multi-clip timeline, editing operations and final video export are not implemented yet. The app does not claim 4K/60 FPS support until that path is implemented and verified on real devices.

## Device target

Initial optimization target: Xiaomi Redmi Note 13 Pro 4G / mid-range Android devices. Runtime performance still needs physical-device testing.

## Upstream and license

The upstream OpenReel Video project is MIT-licensed. Any upstream source code copied or adapted must retain the original copyright and license notices. This initial Android scaffold is a clean native implementation and does not copy upstream application source files. See [upstream LICENSE](https://github.com/Augani/openreel-video/blob/main/LICENSE).
