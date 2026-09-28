# Farm2Market Android apps (native Kotlin)

This repository is a single Android Studio Gradle project with two installable native Kotlin apps and a shared Kotlin library:

- **Customer** (`:customer`, `com.farm2market.customer`)
- **Farmer** (`:farmer`, `com.farm2market.farmer`)
- **Shared** (`:shared`)

Both apps use Jetpack Compose and the shared Supabase layer. They target Android SDK 35, support Android 8.0 (API 26) and newer, and use JDK 17.

## Open, build, and debug

1. Install Android Studio and Android SDK Platform 35. Use Android Studio's bundled JDK (or another JDK 17 or newer).
2. Open this repository's root folder in Android Studio and let Gradle sync.
3. Choose the `customer` or `farmer` app configuration and press **Run** or **Debug**.
4. For a physical phone, enable **Developer options** and **USB debugging**, connect it by USB, accept the RSA prompt on the phone, and select it in Android Studio's device selector. Android Studio installs the debug build and can attach the debugger over USB.

You can also build from a terminal at the repository root with `gradlew.bat :customer:assembleDebug` or `gradlew.bat :farmer:assembleDebug` on Windows, and `./gradlew :customer:assembleDebug` or `./gradlew :farmer:assembleDebug` on macOS/Linux. Debug APKs are written under each app module's `build/outputs/apk/debug/` directory.

## Connect Supabase

The apps read the Supabase project URL and publishable key from the ignored local `supabase.properties` file. Keep that file private and use a publishable key, never a secret or service-role key.

1. Open the project in the Supabase Dashboard and go to **SQL Editor**. Run [`supabase/schema.sql`](supabase/schema.sql). It creates the profile, product, order, and order-item tables, row-level security policies, server-side 20 km marketplace/order functions, stock reservation, allowed order-status transitions, and Realtime publication for products and orders. You can rerun it to update the named policies and functions.
2. In **Authentication > Sign In / Providers**, keep Email enabled and disable Phone. Email confirmation links may stay enabled. The app uses email/password authentication and does not use OTP or anonymous sign-in.
3. In **Project Settings > API Keys**, copy the **publishable** key (starts with `sb_publishable_`). Do not use a secret or service-role key in either Android app.
4. Open the ignored root `supabase.properties` file and set:

```properties
supabase.url=https://YOUR_PROJECT_REF.supabase.co
supabase.publishableKey=sb_publishable_your_project_key
```

Use the URL and key from the same Supabase project, then rebuild both apps.

Email sign-ups can use Supabase's confirmation link when **Confirm email** is enabled; after confirming, sign in with the same password.

### First run

1. Create an account or sign in with an email address and password. Set a name and grant location permission to use the 20 km marketplace.
2. Sign in to the Farmer app with the account that owns the listed products. Customer orders are queried by the seller's account ID and appear in the farmer inbox; status changes refresh the customer's order tracking.

An account's role is fixed when its profile is first created. Use separate accounts for the Customer and Farmer apps. Legacy anonymous profiles and products are not automatically linked to newly created password accounts.

## Included flows

- Email/password sign-up and sign-in with role-specific customer or farmer profiles.
- Nearby products and realtime refresh when farmers add products or change stock, including sold-out state.
- Customer checkout split into seller orders when the cart contains products from multiple farms, with 20 km validation and atomic stock reservation.
- Realtime seller order inbox and customer order tracking as the farmer advances status through accepted, ready, out for delivery, and delivered.

The apps do not collect payment or include delivery-driver GPS tracking.
