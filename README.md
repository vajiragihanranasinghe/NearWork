# NearWork — Classifieds & Jobs Marketplace

A complete Android classifieds marketplace app for Sri Lanka.
Built with Java, Firebase, and Material 3.

Package: lk.vajira.nearwork
Version: 11.0 (versionCode 11)
Min SDK: Android 8.0 (API 26) | Target SDK: Android 15 (API 35)

## Features

### Marketplace
- Post ads with photos (base64 or Supabase Storage)
- Buy, Sell, Exchange, Rent, Free, Request trade types
- Categories: Vehicles, Property, Electronics, Services, Jobs, and more
- Full-text search across title, description, category, and location
- Category filter chips + trade type + ad type spinners
- Featured ads shown first

### Location
- Search nearby places via LocationIQ (Nominatim-compatible)
- Map view of search results
- GPS and manual location picker

### User Accounts
- Email + password authentication (Firebase Auth)
- Profile management (name, phone, photo)
- My Ads — manage and edit your listings
- Favorites
- Multi-language: English, Sinhala, Tamil

### Safety & Compliance
- Report ads and users
- Block users — their content is hidden from your feed
- Blocked Users management screen
- In-app account deletion (Play Store compliant)
- Data export
- Privacy Policy + Terms of Service in-app
- Firestore security rules included

### Notifications
- Push notifications via Firebase Cloud Messaging

## Tech Stack

- Language: Java 17
- UI: XML layouts, Material 3 components
- Auth: Firebase Authentication
- Database: Cloud Firestore
- Storage: Supabase Storage + base64
- Messaging: Firebase Cloud Messaging
- Location search: LocationIQ API (Nominatim)
- HTTP: OkHttp
- Image loading: Glide

## Setup Instructions

### Prerequisites
- Android Studio (or Termux with openjdk-17 + Android SDK)
- Firebase project (free Spark plan)
- Supabase project (free tier) — optional, for image hosting
- LocationIQ account (free tier)

### Step 1 — Firebase
1. Create a Firebase project at console.firebase.google.com
2. Add an Android app with package name lk.vajira.nearwork
3. Download google-services.json and place in app/
4. Enable Authentication, Email/Password
5. Enable Cloud Firestore (production mode)
6. Enable Cloud Messaging (optional)

### Step 2 — Firestore Rules
Deploy rules from firestore.rules:
firebase deploy --only firestore:rules

### Step 3 — Supabase (optional)
Create a project and public bucket named nearwork, add to local.properties:
SUPABASE_URL=https://your-project.supabase.co
SUPABASE_ANON_KEY=your-anon-key

### Step 4 — LocationIQ
Sign up at locationiq.com, add to local.properties:
LOCATIONIQ_KEY=pk.your_token_here

### Step 5 — Build
./gradlew assembleDebug

Release AAB:
./gradlew bundleRelease

## Customization

- App name: app/src/main/res/values/strings.xml
- Colors: app/src/main/res/values/colors.xml
- Categories: AdManager.java getCategories()
- Ad types: AdManager.java getAdTypes()

## Legal

- Privacy Policy: https://vajiragihanranasinghe.github.io/nearwork-legal/privacy.html
- Terms of Service: https://vajiragihanranasinghe.github.io/nearwork-legal/terms.html
- Account Deletion: https://vajiragihanranasinghe.github.io/nearwork-legal/delete-account.html

## License

Source code provided for development use. Commercial redistribution requires permission.

## Support

Email: wajira4u@gmail.com
