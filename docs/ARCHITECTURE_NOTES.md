# YumeHyou Architecture Notes

This document summarizes the current architecture of `xkana-shii/YumeHyou` and maps where key concerns live.

## 0) YumeHyou additive architecture status (Phases 3-5)

### Phase 3: additive YumeHyou package layer

- New additive packages exist under `/app/src/main/java/com/axiel7/yumehyou/`: `core`, `metadata`, `tracker`, `sync`, `search`, `activity`, `export`, `settings`.
- `App` wires YumeHyou modules (`yumehyouCoreModule`, `metadataModule`, `trackerModule`, `syncModule`, `searchModule`, `activityModule`, `exportModule`, `settingsModule`) in addition to existing AniHyou modules (`/app/src/main/java/com/axiel7/anihyou/App.kt:44-59`).
- Boundary: upstream AniHyou repositories/API/viewmodels remain in `com.axiel7.anihyou.*`; YumeHyou layer composes those via gateway/provider wrappers rather than moving upstream files (`/app/src/main/java/com/axiel7/yumehyou/*`).
- Merge-compatibility constraint currently implemented as additive composition: no upstream package reorganization and no upstream class renames were introduced for this layer.

### Phase 4: unified domain model architecture

- Unified models are implemented in `/app/src/main/java/com/axiel7/yumehyou/core/model/`.
- `UnifiedMedia` is the cross-source metadata aggregate (titles, status, staff/characters/relations/tags/links, and tracker mappings) (`UnifiedMedia.kt:3-29`).
- `UnifiedLibraryEntry` is the unified list/tracking state record (status, score, progress, repeat count, notes/privacy, tracker mappings) (`UnifiedMedia.kt:31-44`).
- `TrackerMapping` links unified entities to tracker-specific IDs/entry IDs and optional canonical URL (`TrackerMapping.kt:3-9`).
- `TrackerType` identifies supported trackers (AniList, MyAnimeList, MangaUpdates, MangaBaka) (`CommonTypes.kt:10-15`).
- `MediaType` and `MediaStatus` define media classification and lifecycle status (`CommonTypes.kt:17-31`); `MediaType` sets default canonical source (`ANIME -> ANILIST`, `MANGA -> MANGA_BAKA`).
- Related model roles:
  - `Title` stores preferred/variant names (`Title.kt:5-29`)
  - `Score` stores normalized scoring metadata (`Score.kt:3-30`)
  - `Staff`, `Character`, `Relation`, `Tag`, `ExternalLink`, `ContentRating`, `PartialDate` enrich `UnifiedMedia`/`UnifiedLibraryEntry` (`PeopleAndLinks.kt`, `CommonTypes.kt`).

### Phase 5: tracker capability system

- Capability matrix is implemented in `/app/src/main/java/com/axiel7/yumehyou/tracker/TrackerCapabilities.kt`.
- `TrackerCapability` enumerates feature-level support flags (tracking, notes/tags, score/progress/status updates, rewatch/reread, favorites/social/profile/activity/export/preferences/links/title language).
- `TrackerCapabilities` wraps `trackerType + supported:Set<TrackerCapability>` and exposes `supports`, `supportsAll`, `supportsAny`, plus per-capability booleans for direct business-logic checks.
- `TrackerAdapter` carries `trackerType` and declared capabilities; `TrackerGateway` resolves adapters by tracker and provides capability queries (`TrackerGateway.kt:7-30`).
- Business logic should query `TrackerGateway.supports(trackerType, capability)` or adapter capabilities instead of branching on tracker identity.

## 1) High-level module layout

- `app/`: app shell (application startup, DI wiring, main activity, top-level navigation) (`/home/runner/work/YumeHyou/YumeHyou/app/src/main/java/com/axiel7/anihyou/App.kt:28`, `/home/runner/work/YumeHyou/YumeHyou/app/src/main/java/com/axiel7/anihyou/ui/screens/main/MainActivity.kt:60`)
- `feature/*`: feature modules (home, explore, profile, media details, notifications, settings, etc.) (`/home/runner/work/YumeHyou/YumeHyou/feature`)
- `core/base`: shared primitives (`DataResult`, `PagedResult`, constants) (`/home/runner/work/YumeHyou/YumeHyou/core/base/src/main/java/com/axiel7/anihyou/core/base/DataResult.kt:3`, `/home/runner/work/YumeHyou/YumeHyou/core/base/src/main/java/com/axiel7/anihyou/core/base/Constants.kt:9`)
- `core/common`: utilities + base ViewModel classes (`/home/runner/work/YumeHyou/YumeHyou/core/common/src/main/java/com/axiel7/anihyou/core/common/viewmodel/UiStateViewModel.kt:13`)
- `core/domain`: DataStore + repositories + repository DI (`/home/runner/work/YumeHyou/YumeHyou/core/domain/src/main/java/com/axiel7/anihyou/core/domain/DataStoreModule.kt:11`, `/home/runner/work/YumeHyou/YumeHyou/core/domain/src/main/java/com/axiel7/anihyou/core/domain/RepositoryModule.kt:22`)
- `core/network`: Apollo client + API classes + GraphQL operations (`/home/runner/work/YumeHyou/YumeHyou/core/network/src/main/java/com/axiel7/anihyou/core/network/NetworkModule.kt:15`, `/home/runner/work/YumeHyou/YumeHyou/core/network/src/main/graphql/operations`)
- `core/ui`: shared UI components + typed navigation (`/home/runner/work/YumeHyou/YumeHyou/core/ui/src/main/java/com/axiel7/anihyou/core/ui/common/navigation/Routes.kt:8`)

## 2) Navigation flow

### Main entry points

- `MainActivity` is the app UI entry point (`MainActivity.kt:60`).
- It resolves deep links/intents (`MainActivity.kt:139`) and sends auth callbacks to `MainViewModel` (`MainActivity.kt:134-137`).
- `MainView` creates a `TopLevelBackStack`, `NavActionManager`, and responsive scaffold with bottom bar/rail (`MainActivity.kt:179-243`).

### Route model and navigation actions

- All destination keys are centralized in `Routes` (`Routes.kt:8-153`).
- Route transitions are performed through `NavActionManager` methods such as `toMediaDetails`, `toProfile`, `toNotifications`, etc. (`NavActionManager.kt:25-196`).
- Back stack wrapper: `TopLevelBackStack` (`MainActivity.kt:191`, `MainNavigation.kt:86`).

### Navigation graph composition

- `MainNavigation` is the top-level navigation graph using `NavDisplay` + `entryProvider` (`MainNavigation.kt:156`, `MainNavigation.kt:185`).
- It maps each `Routes.*` entry to a screen composable (`MainNavigation.kt:186-435`), including:
  - top tabs (`Home`, `AnimeTab`, `MangaTab`, `Profile`, `Explore`)
  - details screens (`MediaDetails`, `CharacterDetails`, `StaffDetails`, `ThreadDetails`, etc.)
  - settings stack (`Settings`, `ListStyleSettings`, `CustomLists`, `Translations`)
  - auth-gated screens (`Notifications`, publish screens) that fallback to `LoginView` when not logged in (`MainNavigation.kt:287-296`, `MainNavigation.kt:399-418`)

### Deep link handling

- `MainActivity.findDeepLink()` parses widget, search shortcut, and AniList URLs (`MainActivity.kt:139-174`).
- `MainNavigation` dispatches deep links to navigation actions via `LaunchedEffect(deepLink)` (`MainNavigation.kt:117-154`).

## 3) Compose screen structure

A repeated feature pattern is used:

- `*View.kt`: composable entry screen
- `*ViewModel.kt`: state + business flow
- `*UiState.kt`: UI state
- `*Event.kt`: event interface

Examples:

- Home (`/feature/home/HomeView.kt`, `/feature/home/HomeViewModel.kt`)
- Media Details (`/feature/mediadetails/MediaDetailsView.kt`, `/feature/mediadetails/MediaDetailsViewModel.kt`)
- Notifications (`/feature/notifications/NotificationsView.kt`, `/feature/notifications/NotificationsViewModel.kt`)
- Profile (`/feature/profile/ProfileView.kt`, `/feature/profile/ProfileViewModel.kt`)

Shared base state ViewModel classes:

- `UiStateViewModel<S>` (`UiStateViewModel.kt:13-31`)
- paged variant in `core/common/viewmodel/PagedUiStateViewModel.kt`

All feature ViewModels are wired in one Koin module:

- `ViewModelModule.kt:37-69`

## 4) AniList API integration layer

### Client and transport

- Apollo GraphQL endpoint: `ANILIST_GRAPHQL_URL` (`Constants.kt:9`).
- Apollo client + authorization interceptor + memory normalized cache are configured in `NetworkModule` (`NetworkModule.kt:22-37`).
- Authorization header uses token from mutable `NetworkVariables` (`NetworkModule.kt:39-50`).

### API class layer

Koin-registered API wrappers (`ApiModule.kt:19-33`):

- `ActivityApi`, `CharacterApi`, `FavoriteApi`, `LikeApi`, `MediaApi`, `MediaListApi`, `NotificationsApi`, `ReviewApi`, `StaffApi`, `StudioApi`, `ThreadApi`, `UserApi`, plus `MalApi`.

GraphQL operations are grouped by domain under:

- `/core/network/src/main/graphql/operations/*` (activity, media, mediadetails, user, viewer, etc.)

### Non-AniList network integration

- `MalApi` uses MAL endpoints (for anime themes), with `X-MAL-CLIENT-ID` header via separate `OkHttpClient` (`NetworkModule.kt:60-71`, `Constants.kt:27-29`).

## 5) Repositories / data access layer

### Repository registration

- Repositories are DI singletons in `RepositoryModule` (`RepositoryModule.kt:22-39`).

### Base network behavior

- `BaseNetworkRepository` centralizes response conversion to `DataResult` / `PagedResult` and error handling (`BaseNetworkRepository.kt:19-89`).
- It handles invalid token detection and clears stored token (`BaseNetworkRepository.kt:132-138`).

### Example repository flow

- `MediaRepository` orchestrates media fetches (details, chart, seasonal, activity, reviews, threads, stats, etc.) using `MediaApi` and maps to `DataResult`/`PagedResult` (`MediaRepository.kt:22-244`).
- It also retrieves anime themes through MAL (`MediaRepository.kt:223-232`).

## 6) Authentication flow

1. User taps login button in `LoginView` and opens AniList auth URL (`LoginView.kt:44`, `Constants.kt:13`).
2. Redirect URI uses custom scheme (`Constants.kt:14-18`) and is delivered to app intent.
3. `MainViewModel.onIntentDataReceived` routes auth callback to `LoginRepository.parseRedirectUri` (`MainViewModel.kt:69-74`).
4. `LoginRepository` extracts `access_token`, stores it, then fetches viewer options (`LoginRepository.kt:18-37`).
5. Token is propagated into `NetworkVariables` in `MainViewModel` so interceptor can attach auth headers (`MainViewModel.kt:65-67`, `MainViewModel.kt:90-94`, `NetworkModule.kt:45-47`).
6. Logout clears viewer/token state (`LoginRepository.kt:43-45`, `DefaultPreferencesRepository.kt:64-79`).

## 7) Settings storage

### DataStore setup

- Preferences DataStore file `"default"` is provided in `DataStoreModule` (`DataStoreModule.kt:15-20`).

### Stored settings

`DefaultPreferencesRepository` persists:

- auth/user (`access_token`, `user_id`) (`DefaultPreferencesRepository.kt:292-294`)
- profile/list prefs (`title_language`, score format, list section orders/custom lists) (`DefaultPreferencesRepository.kt:295-303`)
- app prefs (`theme`, black colors, app color mode/palette) (`DefaultPreferencesRepository.kt:305-323`)
- navigation/home prefs (`last_tab`, `default_tab`, `default_home_tab`, `airing_on_my_list`) (`DefaultPreferencesRepository.kt:307-312`)
- notification prefs (`enabled_notifications`, interval, last_notification_created_at`) (`DefaultPreferencesRepository.kt:314-317`)

`SettingsViewModel` reads/writes these values and syncs relevant settings to AniList via `UserRepository.updateUser(...)` (`SettingsViewModel.kt:196-209`, `SettingsViewModel.kt:223-325`).

## 8) Profile screens / profile flow

- Entry: `ProfileView(arguments: Routes.UserDetails, ...)` (`ProfileView.kt:82-87`).
- Own-profile vs other-profile ViewModel store owner is selected based on arguments (`ProfileView.kt:88-93`).
- Main sections are tabbed through `ProfileInfoType`: About, Activity, Stats, Favorites, Social (`ProfileView.kt:186-250`).
- Section composables:
  - `UserAboutView`
  - `UserActivityView`
  - `UserStatsView`
  - `UserFavoritesView`
  - `UserSocialView` (`ProfileView.kt:72-77`)
- Follow/unfollow and profile actions are handled in profile event flow (`ProfileView.kt:329-343`).

## 9) Explore/Home logic

### Explore

- `ExploreView` is a navigation hub screen for charts/seasons/calendar/search (`ExploreView.kt:27-41`, `ExploreView.kt:59-243`).
- It uses `NavActionManager` calls (e.g., `toMediaChart`, `toAnimeSeason`, `toCalendar`) (`ExploreView.kt:77`, `101`, `125`, `183`, etc.).

### Home

- `HomeView` is a 3-tab shell (`HomeTab`): Discover, Activity Feed, Current (`HomeView.kt:108-121`, `127-149`).
- If not logged in, auth-required tabs render `LoginView` (`HomeView.kt:128-137`, `140-147`).
- Notifications badge comes from `HomeViewModel.unreadNotificationCount` and routes to `Notifications` (`HomeView.kt:85-99`).
- Subflows:
  - `DiscoverView` + `DiscoverViewModel`
  - `ActivityFeedView` + `ActivityFeedViewModel`
  - `CurrentView` + `CurrentViewModel`

## 10) Notifications / activity flow

### In-app notifications

- `NotificationsViewModel` is paged and filterable by notification type group (`NotificationsViewModel.kt:18-31`, `40-49`).
- It fetches from `NotificationRepository` and merges paged results into UI state (`NotificationsViewModel.kt:41-64`).

### Background notification worker

- `NotificationWorker` polls AniList notifications periodically with WorkManager (`NotificationWorker.kt:37-49`, `186-205`).
- It checks unread count first and exits early if none (`NotificationWorker.kt:55-60`).
- It filters against persisted `last_notification_created_at` to avoid duplicates (`NotificationWorker.kt:64-75`).
- Media-related notifications can deep-link into media details via launch intent action `"media_details"` (`NotificationWorker.kt:81-90`), consumed by `MainActivity.findDeepLink()` (`MainActivity.kt:141-147`).

## 11) Media detail screen flow

- Entry route: `Routes.MediaDetails(id, isLoggedIn)` (`Routes.kt:62-65`), rendered in navigation graph (`MainNavigation.kt:298-303`).
- `MediaDetailsViewModel`:
  - initial details load (`MediaDetailsViewModel.kt:223-243`)
  - toggle favorite (`MediaDetailsViewModel.kt:54-82`)
  - load characters/staff (`84-97`)
  - load relations/recommendations (`99-111`)
  - load stats/following (`113-140`)
  - load threads/reviews/activity (`142-197`)
  - fetch MAL themes for anime (`212-221`, `236-239`)
- Data source is `MediaRepository` for AniList + `MalApi` for themes (`MediaRepository.kt:118-185`, `225-232`).

## 12) Data flow: network/storage into UI

Typical network flow:

1. GraphQL operation (`/core/network/src/main/graphql/operations/...`)
2. API wrapper call (e.g., `MediaApi`) (`MediaApi.kt:34-318`)
3. Repository maps response (`asDataResult` / `asPagedResult`) (`BaseNetworkRepository.kt:22-89`)
4. ViewModel collects flow and updates state (`MediaDetailsViewModel.kt:224-243`, `NotificationsViewModel.kt:40-64`)
5. Compose view observes state with `collectAsStateWithLifecycle` (`ProfileView.kt:94`, `HomeView.kt:31`, etc.)

Typical preferences flow:

1. DataStore in `DefaultPreferencesRepository` (`DefaultPreferencesRepository.kt:30-323`)
2. ViewModel collects preference flows (`SettingsViewModel.kt:246-325`, `MainViewModel.kt:32-48`)
3. UI reflects values (theme, tabs, notifications, etc.) in composables.

## 13) Main app startup and DI entry points

- `App` implements `KoinStartup` and registers all modules (`App.kt:28-44`).
- Included modules:
  - `dataStoreModule`
  - `networkModule`
  - `apiModule`
  - `repositoryModule`
  - `viewModelModule`
  - `workerModule` (`App.kt:37-43`)
