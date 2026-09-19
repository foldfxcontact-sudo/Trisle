# Trisle: Product and Engineering Master Plan (v2)

Revision date: 2026-09-19
Supersedes: "Tri-Island: Commercial Product and Engineering Master Plan" (v1). "Tri-Island" is now only the internal codename.

---

## 0. Notes for the build agent (read first)

- Evidence tags used in this document:
  - **[V]** verified against an official or primary source on 2026-09-19 (URLs in section 13).
  - **[R]** reported by secondary sources or user reviews; confirm before relying on it.
  - **[K]** general platform knowledge not re-verified on this date; check current Android docs before coding against it.
- **DECISION** marks a choice the owner has not confirmed. Implement it behind a config flag so it can flip cheaply.
- Never embed a Polar access token, API secret, or any other secret in the APK. Only the public license-key endpoints (section 8.4) are called from the app.
- Before writing billing code, fetch `https://polar.sh/docs/llms.txt` and use the newest API version listed there (the index currently lists 2026-04 and 2026-10). **[V]**
- If a platform fact here conflicts with what you observe on a real device or in current docs, trust the device and the docs, and record the discrepancy in `docs/PLATFORM_NOTES.md`.

---

## 1. Product summary

Trisle is a paid, privacy-first Android personalization utility for phones with a front-camera cutout (hole-punch, notch, pill). It draws a small black "anchor pill" around the camera that expands to show the highest-priority background activity, and it can split off up to two satellite bubbles so three activities are visible at once (for example: music in the pill, a timer on the left, a message on the right).

Distribution is **direct download first** (website plus Polar checkout and license keys). Google Play is a separate, later track (section 4.6).

Positioning: an indie alternative to ad-heavy and subscription-heavy notch utilities. No ads, no analytics SDKs, one-time price.

---

## 2. Name and identifiers

| Item | Value |
|---|---|
| Display name | **Trisle** (pronounced "TRY-sul": "tri" + "isle") |
| Tagline | "Three islands. One cutout." |
| Internal codename | Tri-Island |
| Suggested applicationId | `<owner-reverse-domain>.trisle` (DECISION: owner supplies the domain) |
| License key prefix | `TRISLE` |
| Pro product name | "Trisle Pro (Lifetime)" |

Naming rules for all copy, store text, and keywords:
- Do **not** use Apple's "Dynamic Island" phrase in the app name, description, keywords, or screenshots. Describe the feature as "camera cutout", "hole-punch", or "notch".
- The applicationId and the app signing key become permanent once registered with Google's developer verification (section 4.4). Choose the applicationId once and back up the keystore in two places.

Clearance status (be honest in any listing or legal text):
- A web search on 2026-09-19 found no exact-match app or brand named "Trisle". This is **not** a trademark clearance.
- Before launch the owner must run: Google Play search, GitHub search, domain check (`trisle.app`, `trisle.com`), and a trademark search (USPTO, EUIPO, WIPO Global Brand Database) in the relevant classes.
- Names already rejected during research, with reasons: "Atoll" (an existing macOS notch/island app of that name is referenced in the same niche, plus an audio-hardware company with an Android control app); "Skerry" (existing SSH client and an existing iOS app); "Archipill" (existing architecture platform of that name).
- Backups if "Trisle" fails clearance (unchecked): "Trinode", "Triorb".

---

## 3. Positioning and differentiators (revised)

### 3.1 Competitive reality **[R]**
- dynamicSpot: large install base (1M+ installs reported), supports a multi-island split into two smaller pills, uses the AccessibilityService API, and its Play listing discloses that.
- Action Notch / Touch the Notch: camera cutout as a gesture button, 1M+ downloads reported, also accessibility-based.
- Material Capsule, Dynamic Notch: Smart Pill, NotiGuy, Dynamic Bar and others: same overlay approach; several are ad-supported.
- Pricing anchors seen in reviews of a competitor: a one-time option reported at $7.99 and a weekly subscription reported at $3.99. Treat as anecdotal.

### 3.2 What that means
"Three concurrent nodes" is **not** unique on its own. Do not market it as the only differentiator. Lead with:

1. **Privacy and honesty:** no ads, no analytics SDKs, permissions explained before they are requested, and the exact list of network calls published (section 8.5).
2. **Price model:** one payment, no weekly or monthly subscription, and a real chance to try Pro before paying (section 8.6).
3. **Motion quality:** spring physics with a "gooey" detach/merge between pill and satellites.
4. **Calibration that actually works:** center, offset, dual cutouts, and foldables where the cutout differs per screen (a real-world example: a foldable whose outer display has a top-center cutout and whose inner display has a top-right cutout **[R]**).
5. **Battery discipline:** rendering fully stops when the screen is off or a fullscreen app is active.

---

## 4. Platform and distribution constraints (verified 2026-09-19)

### 4.1 Android versions
- Android 17 (API 37) was released stable on 2026-06-16. **[V]** Android development guidance is now Compose-first; View-based libraries are in maintenance mode. **[V]**
- Android 17 enforces app memory limits based on device total RAM and can terminate offenders. The exit reason to look for is `REASON_OTHER` with a description containing `MemoryLimiter:AnonSwap`; `ProfilingManager` supports `TRIGGER_TYPE_ANOMALY` heap dumps. **[V]** Apps targeting API 37 also get: read-only requirement for natively loaded libraries, lock-free `MessageQueue`, and local-network permission gating. **[V]**
- Android 15+ hides the content of "sensitive" notifications (for example one-time codes) from notification listeners unless a special app-op is granted through ADB. **[R]** Treat such content as redacted and never depend on it.
- DECISION (recommended): `minSdk 29` (Android 10), because cutout bounding rectangles are exposed from API 29 **[K]**; `compileSdk 37`; `targetSdk 36` for v1.0, moving to 37 in a 1.x release after memory-limit testing. Check Google Play's current target-API requirement in Play Console Help before any Play submission.

### 4.2 Overlay window type (the v1 plan left this undefined; it must be decided now)
- `TYPE_APPLICATION_OVERLAY` (via the "Display over other apps" permission) is documented as sitting above activity windows but **below** critical system windows such as the status bar. **[K]** A developer report states the same reading of the docs. **[R]** That means it cannot reliably draw over the status bar area where the camera cutout lives. Confirm on real devices in Phase 0.
- Established competitors draw over the cutout using an **AccessibilityService** with `TYPE_ACCESSIBILITY_OVERLAY`, and their store listings disclose this. **[R]** Developer reports say accessibility overlays can render above the status bar and often above the lock screen. **[R]** Confirm on real devices in Phase 0.
- **DECISION (recommended architecture):** a single `AccessibilityService` hosts the overlay window(s) using `TYPE_ACCESSIBILITY_OVERLAY`. Do **not** declare `isAccessibilityTool="true"` (Play reserves that flag for tools whose primary purpose is helping people with disabilities). **[V]** The service must request only the event types and capabilities it needs; do not read window content for any purpose other than detecting foreground-app changes for the exclusion list.
- Fallback for devices where the accessibility overlay misbehaves: `TYPE_APPLICATION_OVERLAY` with `SYSTEM_ALERT_WINDOW`, accepting that it may sit under the status bar on some devices.

### 4.3 Sideloaded install friction ("Restricted settings")
- From Android 13, apps installed from an APK file (not from an app store) have the **Accessibility** and **Notification access** toggles greyed out until the user opens App info, taps the three-dot menu, and chooses **Allow restricted settings**. The option only appears after the user first tries to enable the toggle and gets the "Restricted setting" dialog. **[V]**
- Consequence: onboarding must detect this state and walk the user through it with screenshots. Expect this to be the largest drop-off point of the direct-download channel. Build a dedicated "Permission helper" screen (section 6.4).
- Do not rely on installer tricks to bypass restricted settings.

### 4.4 Google developer verification (new in 2026; the v1 plan omitted it)
- Google now requires apps on **certified** Android devices (devices with Google Play services) to come from a verified developer. **[V]**
- Timeline: verification protections go live **2026-09-30** for users in Brazil, Indonesia, Singapore and Thailand, initially for installs from seven participating stores (Google Play, HONOR App Market, OPPO App Market, Galaxy Store, Palm Store, vivo V-Appstore, Xiaomi GetApps); global expansion to all apps distributed to certified devices in **2027**. **[V]** The Android Developer Console (with the full experience) has been open to all developers since March 2026. **[V]**
- Registration means: identity verification, registering the applicationId and signing key, and a **$25 one-time fee** for a full distribution account (a free "limited distribution" account exists for up to 20 devices without government ID). **[V]** Organizations need a D-U-N-S number, which can take up to 28 days to obtain. **[V]**
- Unverified apps can still be installed through an "advanced flow" (risk acknowledgement and a one-time setup) or via ADB. **[V]** A 24-hour waiting period in that flow has been reported. **[R]** A one-time-purchase utility should not rely on users doing that.
- Action items (Phase 5): register the developer account, register `applicationId` + signing key for all form factors you ship, and state in the website FAQ that Trisle is a verified-developer app.
- Custom ROMs without Google services (for example LineageOS, GrapheneOS) are not "certified devices" and are not directly affected. **[V]**

### 4.5 Google Play policy for accessibility-based apps
- Play permits `AccessibilityService` for non-accessibility apps, but such apps must show a **prominent in-app disclosure with affirmative consent** (not only in the listing or privacy policy), must complete the Permission Declaration in Play Console, and must document the API use in the listing. **[V]** Deceptive or undeclared use can lead to suspension or account termination. **[V]**
- Build the disclosure screen once (section 6.4) and reuse it for the direct-download build.

### 4.6 Play distribution and billing (later track; region rules are still changing)
- Google no longer requires Google Play Billing for apps in the US Play Store and allows communicating about other payment methods; developers enrolled in the US external-content and alternative-billing programs must report transactions and pay the relevant fees starting 2026-10-01. **[V]** Broader changes for the UK and EEA were reported for 2026-06-30, with other regions rolling out through 2027. **[R]**
- DECISION (recommended): v1.0 ships **direct only**. A Play build is a separate product flavor with its own entitlement source (Play Billing or a policy-compliant external option per region). Do not mix the two entitlement systems in the first release. Re-verify Play billing rules at the time the Play track starts.

---

## 5. Architecture

Language and UI: Kotlin. DECISION (recommended): Jetpack Compose for settings, onboarding, and calibration screens; a **lightweight View-based overlay layer** with `androidx.dynamicanimation` `SpringAnimation` **[K]** for the pill and satellites, because Compose inside a service-hosted window needs manual lifecycle and saved-state owners and costs more memory. The Phase 0 spike must compare both and choose by measurement.

### 5.1 Modules
1. **Cutout and inset calibration**
   - Read `DisplayCutout` bounding rects (API 29+) **[K]**, plus per-display data for foldables. Recompute on configuration change, rotation, and fold-state change.
   - Manual calibration canvas (drag/resize handles, corner radius, offset) with per-display-profile storage (folded and unfolded are separate profiles).
2. **Activity prioritization engine**
   - Inputs: media sessions, notifications (by category), timers, calls.
   - Priority tiers (unchanged from v1): 1 urgent (live call, navigation turn); 2 time-sensitive (countdown, stopwatch); 3 continuous media; 4 ephemeral alerts.
   - Highest-ranked activity goes to the anchor pill; second and third go to the left and right satellites. Ties are broken by most recent state change. Make the tier table data-driven.
   - Source guidance **[K]**: media via `MediaSessionManager.getActiveSessions` (requires an enabled notification listener component); calls and navigation are derived from notification categories (`CATEGORY_CALL`, navigation-related notifications), which avoids requesting the sensitive phone-state permission; timers and stopwatches are parsed from the clock app's ongoing notifications (there is no public cross-app timer API).
3. **Overlay window and input dispatcher**
   - Hosted by the accessibility service (section 4.2).
   - Keep the window as small as possible and resize it per state instead of using a fullscreen window with touchable regions.
   - Outside the visible pill and bubbles, touches must reach the underlying app and the status bar or notification shade. Use `FLAG_NOT_TOUCHABLE` toggling, and verify pass-through on Android 12 through 17. Never intercept the pull-down gesture.
4. **Motion engine**
   - Spring physics for expand, collapse, detach, merge. A "gooey" bridge between pill and satellites can use a blur-plus-threshold `RenderEffect` on API 31+ **[K]** with a plain scale/alpha fallback below that.
   - Respect the system "remove animations" / reduced-motion setting.
5. **Power and exclusion manager**
   - Stop all rendering and timers on screen-off, on lock (configurable), and when a fullscreen app on the exclusion list is foreground.
   - Default exclusions: camera apps, video players, games (category-based where possible), user-editable.
6. **Licensing** (section 8.4). No backend server of our own in v1.

### 5.2 Non-functional targets
- The v1 target of "under twenty megabytes" was unvalidated. Replace it: in Phase 0 record idle and active PSS on a mid-range and a low-RAM device, then set the budget from the measurements. Android 17's RAM-based limits make a low, stable budget important, but the exact enforcement thresholds are not fully published. **[V]**
- Use R8 full mode, and profile with LeakCanary and `ProfilingManager` anomaly triggers. **[V]**
- Zero dropped frames on 120 Hz displays during detach and merge animations (Phase 3 acceptance test).

---

## 6. UX and interaction lifecycle

### 6.1 States
- **Idle:** minimal pill hugging the cutout, or fully invisible (user preference).
- **Single activity:** pill extends to show metadata (for media: title, elapsed time, mini visualizer).
- **Tri-mode (Pro):** a second activity detaches to the left as a rounded bubble (for example a timer); a third detaches to the right as a smaller circle (for example a message indicator).

### 6.2 Gestures
- **Tap:** opens the source app.
- **Long press (Pro):** expanded floating card below the island: media scrubbing and transport, timer pause/reset, quick reply via notification `RemoteInput` **[K]**.
- **Outward flick on a satellite:** silence or hide that satellite until its next state change.

### 6.3 Edge cases to specify and test
- Rotation to landscape (default: hide), split-screen, freeform windows, always-on display, lock screen, in-call screen, display size and font scale changes, RTL locales, dark/light status bar icon contrast, multiple users and work profiles.

### 6.4 Onboarding and permission helper
- Interactive sandbox on first launch: demonstrate detach and merge with simulated events **before** requesting any permission.
- One permission per screen, each with plain-language purpose, exactly what is read and not read, and an affirmative "Continue" tap:
  1. Accessibility service (prominent disclosure text stored in one shared string resource, reused for any future Play build).
  2. Notification access.
  3. Battery-optimization exemption and manufacturer-specific guidance (link to dontkillmyapp.com content for the detected OEM **[K]**).
- Restricted-settings helper: detect when the toggles are blocked and show the "App info, three dots, Allow restricted settings" steps with screenshots per OEM (section 4.3).
- Never request more permissions than the feature set uses.

---

## 7. Development roadmap and acceptance criteria

### Phase 0: Feasibility spike (do first)
- Build a throwaway accessibility-hosted overlay pill; test on at least: a Pixel on stock Android 16/17, a Samsung One UI device, a Xiaomi HyperOS device, and one foldable if available.
- Acceptance: overlay renders over the status bar area on all tested devices (or the failures are documented with the fallback from 4.2); touch pass-through works; measured idle/active memory recorded; View+SpringAnimation vs Compose decision recorded.

### Phase 1: Foundation and hardware alignment
- Overlay service, cutout detection, per-display profiles, manual calibration screen, boot/restart handling.
- Acceptance: correct alignment on every phase-0 device; survives rotation and fold changes without leaking windows.

### Phase 2: Event ingestion
- Media session listener, notification listener with fallback parsing (no crash on malformed or missing extras), timer/call/navigation detection, priority engine.
- Acceptance: unit tests for the priority engine with fixture events; fuzz test with malformed notifications; no crash on any fixture.

### Phase 3: Motion, transitions, modals
- Spring detach/merge, long-press cards, flick-dismiss, reduced-motion support.
- Acceptance: no dropped frames at 120 Hz on a reference device; animations obey the system animation-scale setting.

### Phase 4: Power, exclusions, edge handling
- Screen-off and lock sleep, exclusion list with defaults, memory audit against the Phase-0 budget on Android 17.
- Acceptance: no wakeups while the screen is off (verified with battery/trace tooling); no `MemoryLimiter` exits in a 24-hour soak test.

### Phase 5: Licensing, packaging, verification
- Polar integration (section 8), signing, developer-account verification and app registration (section 4.4), website, privacy policy, closed testing with enthusiast communities.
- Acceptance: end-to-end test purchase in Polar's sandbox activates Pro on a clean device; the release APK installs on a verified-developer test path; privacy policy lists exactly the network calls in section 8.5.

---

## 8. Commercialization and Polar.sh integration

### 8.1 Tiers
**Free:** single anchor pill, standard media indicators, manual calibration, idle styles, basic exclusion list.

**Pro Lifetime (one-time):** Tri-Node satellites, long-press dashboards, per-app exclusion and custom trigger rules, premium effects and accent matching, all updates within the 1.x line (DECISION: state the update policy on the checkout page; a lifetime license with yearly Android changes needs a stated promise).

### 8.2 Price: **US$5.99** (DECISION, recommended)
The v1 plan assumed US$3 to US$5. Polar changed its pricing for organizations created on or after **2026-05-27**: the free Starter plan is now **5% + US$0.50 per transaction** (previously 4% + US$0.40), plus **+1.5%** for international (non-US) cards. **[V]** A fixed per-transaction fee hurts low prices most, so the price floor moves up.

Take-home per sale, Starter plan, US buyer, domestic card, before sales tax:

| List price | Polar fee | You keep | Effective fee |
|---|---|---|---|
| $3.99 | $0.70 | $3.29 | 17.5% |
| $4.99 | $0.75 | $4.24 | 15.0% |
| **$5.99** | **$0.80** | **$5.19** | **13.3%** |
| $6.99 | $0.85 | $6.14 | 12.2% |
| $7.99 | $0.90 | $7.09 | 11.3% |

Same prices for a buyer in a VAT-inclusive market at 20% VAT with an international card (fee is charged on the tax-inclusive total, matching Polar's own worked example **[V]**):

| List price | VAT (20%) | Polar fee (5% + $0.50 + 1.5%) | You keep |
|---|---|---|---|
| $4.99 | $0.83 | $0.82 | $3.33 |
| **$5.99** | **$1.00** | **$0.89** | **$4.10** |
| $7.99 | $1.33 | $1.02 | $5.64 |

Why $5.99: +20% list price over $4.99 yields about +23% take-home in the international case, it stays below the $7.99 lifetime anchor reported for the closest competitor **[R]**, and it is only about a dollar above the top of the v1 range. Floor: do not go below **$4.99**.

Notes:
- If the owner's Polar organization was created **before 2026-05-27**, it is on the grandfathered "Early Member" rate (4% + $0.40, +1.5% international). At $5.99 that is a $0.64 fee. Upgrading to any paid plan permanently retires that rate. **[V]** Do not upgrade unless the numbers justify it.
- Paid plans (Pro $20/mo at 3.8% + $0.40; Growth $100/mo; Scale $400/mo) only pay off at volume. At $5.99 the Pro plan saves about $0.17 per sale, so it breaks even near **116 sales per month (about $700/month)**. Polar's own published break-even of about $1,379/month assumes a $40 average order. **[V]**
- Tax: Polar is the Merchant of Record and handles VAT/sales tax. Prices default to tax-inclusive in most of the world and tax-exclusive in the US, Canada and India. **[V]**
- Refunds do not return the original fee. Disputes cost **$15 each** regardless of outcome, and chargeback rates near about 0.7% can trigger account intervention. **[V]** At $5.99, one dispute costs roughly three sales' worth of net revenue, so refunds must be easy and fast.
- Payout costs (Stripe pass-through): $2 per month with active payouts, plus 0.25% + $0.25 per payout, plus currency-conversion fees of 0.25% to 1%. **[V]** Withdraw manually and infrequently.
- Optional later: local-currency prices are supported (a price in the default currency is mandatory and all currency prices must share the same structure). **[V]**

### 8.3 Polar setup checklist
1. Create the Polar organization and complete Polar's account review; confirm the owner's payout country is on the supported-countries list (Stripe Connect Express based). **[V]**
2. Create a **License Keys** benefit: prefix `TRISLE`, activation limit **3** devices, enable customer self-deactivation, no expiry. **[V]**
3. Create product "Trisle Pro (Lifetime)": one-time, fixed price USD 5.99, attach the benefit. Billing cycle and pricing type cannot be changed later; the amount can. **[V]**
4. Create a persistent **Checkout Link** for the product; the app's "Buy Pro" button opens it in a browser Custom Tab. **[V]**
5. Create a launch discount code (for example 25% off for launch week). **[V]**
6. Publish the refund policy (DECISION: 14 days, no questions). Polar may itself refund up to 60 days after purchase to reduce disputes. **[V]**
7. Use Polar's sandbox environment for all testing (docs: `polar.sh/docs/integrate/sandbox`). **[V]**

### 8.4 License flow (no backend of our own)
Base URL (production): `https://api.polar.sh`. These customer-portal endpoints accept the license key and organization ID without an API secret. **[V]**

1. User taps "Buy Pro", pays on Polar's checkout, and receives the key by email and in the Polar customer portal.
2. User pastes the key into the app. App calls `POST /v1/customer-portal/license-keys/activate` with `key`, `organization_id`, and a human-readable `label` (device model or a name the user types; do **not** send hardware identifiers). Store `activation_id`.
3. App calls `POST /v1/customer-portal/license-keys/validate` with `key`, `organization_id`, `activation_id`. Grant Pro only when the returned `status` is `granted`. **[V]**
4. Validation cadence: at activation, then at most once every 7 to 30 days when online. **Never** revoke Pro because a request failed or the device is offline; only revoke on an explicit non-granted response.
5. Store the key and activation ID with Android Keystore-backed encryption (use the Keystore directly or Tink; check the current status of Jetpack security-crypto before using it **[K]**).
6. Customers can rotate a compromised key in Polar's portal, and deactivate old devices themselves. **[V]**
7. Client-side license checks can always be bypassed on a device the user controls. Accept this; do not add heavy anti-tamper. It matches the DRM-free positioning.

### 8.5 Privacy statement (must match reality)
"Zero telemetry" and "complete offline operation" in v1 conflict with license validation. Replace the claim with: "No analytics, no ads, no tracking. The only network requests Trisle ever makes are to Polar to activate and re-check your license key, and only if you are a Pro customer." Publish the exact request contents in the privacy policy: key, organization ID, activation ID, device label. Free-tier users make **no** network requests.

### 8.6 Trial (DECISION)
Reviews of a competitor show users angry about being asked to pay before trying the app. **[R]** Options, in order of simplicity:
1. Local 7-day Pro trial started on first launch (bypassable; accept it).
2. A free Polar product whose license-key benefit expires automatically after 7 days (Polar supports free products and key expiry **[V]**); requires the user's email.
Recommended: option 1 for v1.0.

### 8.7 Distribution channels
- Primary: own website with Polar checkout link and signed APK download (publish SHA-256 checksums). Register the developer account and app first (section 4.4).
- Secondary, optional: F-Droid or other stores are not viable for a paid, closed-source utility; do not plan for them.
- Play track: see section 4.6.

---

## 9. Marketing and launch

- High-framerate screen recordings showing three simultaneous activities (music center, delivery countdown left, chat ping right). Show the real overlay on a real device, not mock-ups.
- Community launch posts in Android customization and indie-dev forums; publish honest permission explanations and the exact network calls.
- Positioning line: "The notch utility with no ads, no tracking, and no subscription."
- Do not claim "first" or "only" three-node experience (section 3.1).
- Include the restricted-settings walkthrough on the download page so users are not surprised.
- Closed testing with enthusiast communities before public release; collect OEM-specific failures into the device matrix.

---

## 10. Risk management and QA

| Risk | Mitigation |
|---|---|
| OEMs kill background services or force-stop apps (a force-stop clears the accessibility permission) | Device-specific battery guidance; detect when the service is off and show a one-tap recovery screen; test on Samsung, Xiaomi, OPPO/OnePlus, vivo, Motorola, Nothing |
| Restricted settings block permissions for sideloaded installs (section 4.3) | Permission helper with screenshots; test the sideload path on every release |
| Google developer verification blocks installs on certified devices (section 4.4) | Register developer account and app before public release; monitor the 2026-09-30 and 2027 phases |
| Play policy on accessibility use | Prominent disclosure, no `isAccessibilityTool`, only for the Play track (section 4.5) |
| Android 17 memory limits | Phase-0 baseline, R8 full mode, soak tests, `ApplicationExitInfo` logging locally (never uploaded) |
| Notification format variability across messaging apps | Fallback parsers; fixtures from top messaging apps; never crash on missing extras |
| Redacted sensitive notifications on Android 15+ | Treat as opaque; show a generic "new message" bubble |
| Overlay intercepting status bar or shade gestures | Tight window bounds; explicit pass-through tests on Android 12 to 17 |
| Foldables and dual cutouts | Per-display profiles; foldable in the test matrix |
| Payments: disputes and refunds | Clear refund policy, fast support, receipt email with license instructions |
| Name collision | Complete clearance checks in section 2 before public launch |

Minimum device test matrix: Pixel (stock Android 16 and 17), Samsung One UI, Xiaomi HyperOS, OPPO/OnePlus ColorOS, one budget device with low RAM, one foldable, plus emulators for Android 10 (minSdk) and 17.

---

## 11. Open decisions for the owner

1. Confirm the display name (Trisle) after clearance, and the reverse-domain applicationId.
2. Confirm the price ($5.99) and launch discount.
3. Confirm the refund window (14 days recommended) and the update policy for "lifetime".
4. Confirm the trial approach (local 7-day recommended).
5. Confirm whether the owner's Polar organization predates 2026-05-27 (Early Member rate).
6. Confirm the overlay architecture (accessibility-hosted, recommended) after the Phase-0 spike.
7. Decide when, if ever, to start the Google Play track.

## 12. Explicit non-goals for v1.0

Cloud sync, accounts, analytics, ads, a backend server, iOS, wearables, and any subscription billing.

---

## 13. Sources checked on 2026-09-19

- Polar fees and plans: https://polar.sh/docs/merchant-of-record/fees
- Polar plan change announcement (effective for organizations created on or after 2026-05-27): https://polar.sh/blog/introducing-polar-plans
- Polar products, pricing models, currencies, tax display: https://polar.sh/docs/features/products.md
- Polar license keys (activate, validate, rotate): https://polar.sh/docs/features/benefits/license-keys.md
- Polar supported countries and payouts: https://polar.sh/docs/merchant-of-record/supported-countries.md
- Polar documentation index (API versions): https://polar.sh/docs/llms.txt
- Android 17 release notes: https://developer.android.com/blog/posts/android-17-is-here
- Android 17 behavior changes: https://developer.android.com/about/versions/17/behavior-changes-17 and https://developer.android.com/about/versions/17/behavior-changes-all
- Google developer verification (dates, stores, fees, advanced flow): https://support.google.com/android-developer-console/answer/16561738
- Play AccessibilityService policy: https://support.google.com/googleplay/android-developer/answer/10964491
- Play US billing and link policy: https://support.google.com/googleplay/android-developer/answer/15582165
- Android restricted settings: https://support.google.com/android/answer/12623953
- Competitor listings and reviews (secondary): dynamicSpot Play listing; Androxus guides on notch apps; Material Capsule review; Notch: Touch the Notch Play listing.
