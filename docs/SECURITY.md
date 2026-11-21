# Security

## Supported versions

Security fixes target the latest published release and the current development branch.

## Reporting a vulnerability

Please do not publish exploitable details in a public issue. Use GitHub's private vulnerability reporting feature for this repository if enabled. If private reporting is unavailable, contact the repository owner privately through GitHub before disclosure.

## Engineering requirements

- Never commit credentials, signing keys or personal data.
- Never place Supabase service-role keys or other privileged secrets in the APK.
- Validate imported data and exported backup files.
- Use Android's permission model and provide clear disclosures.
- Keep dependencies current and review dependency changes.
- Treat accessibility and VPN capabilities as sensitive platform integrations.
- Avoid collecting or transmitting app-usage information without explicit user action.

## Scope

This is an early-stage project. A security policy is not a claim that the application has completed an independent security audit.
