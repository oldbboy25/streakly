# Streakly — Play Console submission content

Copy-paste these directly into Play Console. Char counts are kept under the hard limits.

---

## App details

**App name** (30 char max)
```
Streakly: Habit Tracker
```
(23 characters)

**Short description** (80 char max)
```
Build daily habits offline. No account, no ads, no habit limit — ever.
```
(72 characters)

**Full description** (4000 char max)
```
Streakly is a simple, 100% offline habit tracker that helps you build daily routines and keep your streaks alive — without an account, ads, or a subscription.

Most habit trackers ask you to sign up, show ads, or lock basic features behind a paywall. Streakly doesn't. Every habit, streak, and check-in lives only on your device. Nothing is uploaded unless you choose to export it yourself.

WHY STREAKLY
• No account, ever — open the app and start tracking in seconds
• No ads, no subscription, no "pro" tier — every feature is free, permanently
• No habit limit — track 1 habit or 50
• Your data stays yours — automatic local backup plus one-tap export/import, so uninstalling the app or switching phones never means losing your streak history

FEATURES
• Daily habit checklist with one-tap complete
• Streak flames that track your current and longest streak per habit
• Visual stats: weekly, monthly, yearly, and all-time completion charts, plus a completed-vs-missed breakdown
• Home-screen widget to check off habits without opening the app
• Flexible reminders so you never forget a habit
• Drag to reorder your habit list
• Light and dark theme, matched to your system or set manually
• English and French supported, with more languages planned
• Swipe to complete or archive habits right from the list
• Milestone celebrations at 7, 14, 30, and 100-day streaks

BUILT TO BE TRUSTED
Streakly has no server and no backend — not because it's an early-stage limitation, but because it's the whole point. There is nothing to hack, nothing to sell, and nothing that can disappear if a company shuts down. Your habits belong to you.

Whether you're building a meditation practice, drinking more water, reading daily, or exercising consistently, Streakly stays out of your way and keeps your streak honest.

Download Streakly and start your streak today.
```
(~1,750 characters — well under the 4,000 limit; intentionally not padded further per ASO guidance against keyword stuffing)

---

## Categorization

- **App category:** Health & Fitness
- **Tags (Play Console lets you pick up to 5 from a fixed list):** Habit tracker fits under Health & Fitness; if a secondary category option is offered, Productivity is the next-best fit.

---

## Graphics checklist

| Asset | Requirement | File |
|---|---|---|
| App icon | 512×512, 32-bit PNG | `store/icon-512.png` ✅ |
| Feature graphic | 1024×500, JPG/PNG, no alpha | `store/feature-graphic-1024x500.png` ✅ |
| Phone screenshots | 2–8 images, 320–3840px/side | `store/screenshots/01_today.png` … `04_habit_detail.png` ✅ (4 provided — add more any time by recapturing) |

All screenshots are real on-device captures (1080×2400), not mockups.

---

## Content rating questionnaire — expected answers

Streakly has no violence, sexual content, gambling, user-generated content, social features, or in-app purchases, and collects no data. Answering the Play Console questionnaire honestly should land on **Everyone** (or PEGI 3 depending on region). Go through the questionnaire yourself since Google requires the developer to answer it directly — this is guidance, not a substitute for completing the form.

---

## Data safety form — verified against the actual code

Checked `app/build.gradle`: no ad SDK, no analytics SDK, no crash-reporting SDK (only AndroidX/Material/Room/Gson/Play-in-app-review, all local). Expected answers:

- **Does your app collect or share any of the required user data types?** No
- **Is all user data encrypted in transit?** N/A (no data leaves the device)
- **Do you provide a way for users to request data deletion?** N/A (nothing is collected to delete — Export/Import gives users full control of their own local file)

This is a genuine differentiator — say so plainly in the listing, which the full description above already does.

---

## Privacy policy — REQUIRED, not yet hosted

Play Console requires a **live URL** to a privacy policy before it will let you publish, even for a no-data-collection app. Draft text below — you need to host it somewhere public and put that URL into Play Console's "Privacy policy" field.

**Suggested free option:** GitHub Pages on the existing `oldbboy25/streakly` repo (zero cost, same place everything else already lives). Say the word and this can be wired up — add `docs/privacy.html` to the repo and enable Pages (Settings → Pages → Deploy from branch → `main` /docs) and the URL becomes `https://oldbboy25.github.io/streakly/privacy.html`. Not done yet since it touches repo settings outside the codebase — confirm and it'll be set up next.

Draft text:
```
Privacy Policy for Streakly

Last updated: [date]

Streakly does not collect, store, transmit, or share any personal data.

- Streakly has no server, no account system, and no analytics or advertising SDKs.
- All habit data you create (names, streaks, completion history) is stored only in this app's local storage on your device.
- Data leaves your device only if you explicitly use the "Export my data" feature, which saves a file to a location you choose using Android's standard file picker. Streakly never uploads this file anywhere.
- Uninstalling the app permanently deletes all local data, unless you previously exported it yourself.

If you have questions about this policy, contact: [your email]
```

---

## Still needed from you before submitting

1. Pick/confirm a contact email and developer name for the Play Console listing (not something to guess on your behalf).
2. Decide on the privacy policy hosting (GitHub Pages suggested above) and fill in the `[date]` / `[your email]` placeholders.
3. Walk through the Content rating and Data safety questionnaires in Play Console yourself — answers above are accurate guidance but Google requires the developer account holder to submit them directly.
4. Upload `app-release.aab` (downloaded to your machine at `%TEMP%\streakly-release-aab\app-release.aab`) as the production/internal-testing release.
