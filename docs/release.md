# Release checklist

This project can build unsigned release APKs without secrets. Signed release APKs are enabled only when the following GitHub Secrets are configured.

## GitHub Secrets

- `RELEASE_KEYSTORE_BASE64`: base64 encoded Android keystore file.
- `RELEASE_KEYSTORE_PASSWORD`: keystore password.
- `RELEASE_KEY_ALIAS`: key alias inside the keystore.
- `RELEASE_KEY_PASSWORD`: key password.

Optional GitHub Variable:

- `VERSION_CODE`: integer Android version code used by CI.

## Create the keystore secret

```bash
base64 -w 0 release.keystore
```

Copy the output into `RELEASE_KEYSTORE_BASE64`.

## Release flow

1. Update `VERSION_CODE` in GitHub repository variables.
2. Create and push a tag like `v0.1.0`.
3. GitHub Actions runs unit tests, debug build, and release build.
4. On `v*` tags, GitHub Actions creates a GitHub Release with the release APK.

If signing secrets are missing, CI still uploads an unsigned release APK artifact for testing, but that APK is not Play-ready.

## RC test flow

For a release candidate, use a pre-release style tag:

```bash
git tag v0.1.0-rc1
git push origin v0.1.0-rc1
```

After the workflow finishes, download the APK from the generated GitHub Release and run the manual RC checklist:

- First launch
- Save Base URL / model / API Key
- Query `spring`
- Query `break the ice`
- Query `red apple`
- Favorite a card
- Add a note
- Copy a card
- Regenerate a card
- Restart the app and verify history, favorites, and notes are still present
