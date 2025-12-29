# Task List

## 1. Project Setup & Core Architecture
- [ ] Configure KMP project with `androidApp` and `desktopApp` targets <!-- id: 0 -->
- [ ] Setup Koin for dependency injection <!-- id: 1 -->
- [ ] Setup SQLDelight for database <!-- id: 2 -->
- [ ] Setup Ktor for network requests and proxy server <!-- id: 3 -->
- [ ] Setup Google Sign-in for Android and Desktop <!-- id: 67 -->
- [ ] Setup Turso for cloud storage <!-- id: 68 -->
- [ ] Setup Glance for Android widget <!-- id: 4 -->

## 2. Data Layer
### Model
- [ ] Define `Word` class <!-- id: 43 -->
- [ ] Add `name`,  `meaningKr`, `example`, `oppositeEn`, `tags`, `created`, `modified`, `isDeleted`, `synced` fields <!-- id: 44 -->

### Storage (SQLDelight)
- [ ] Create database and tables <!-- id: 5 -->
- [ ] Create columns including `tags`, `CreatedTime`, `LastModifiedTime`, `isDeleted`, `synced` <!-- id: 7 -->
- [ ] Implement `insertWord(word: Word)` (set CreatedTime, LastModifiedTime) <!-- id: 45 -->
- [ ] Implement `deleteWord(word: Word)` (soft delete: set isDeleted=true, update LastModifiedTime) <!-- id: 46 -->
- [ ] Implement `updateWord(word: Word)` (update LastModifiedTime) <!-- id: 47 -->
- [ ] Implement `syncDB()` logic <!-- id: 48 -->
    - [ ] Get all `synced=false` words <!-- id: 49 -->
    - [ ] Batch update remote DB (check `modified` to determine win) <!-- id: 50 -->
    - [ ] Update all `synced=true` if success <!-- id: 51 -->
    - [ ] Batch update local DB from remote <!-- id: 52 -->
    - [ ] Show sync results (deleted, updated, added) <!-- id: 53 -->

### API Services
- [ ] Implement `GeminiService` <!-- id: 9 -->
    - [ ] Function `enrichWord(word: String): EnrichedWordData` <!-- id: 10 -->
    - [ ] Prompt engineering for strict JSON response <!-- id: 11 -->
    - [ ] Implement `resultvlidation(JSON)` to validate and parse response <!-- id: 54 -->
- [ ] Implement `TursoService` <!-- id: 12 -->
    - [ ] `syncDB()`: sync localDB and remoteDB <!-- id: 13 -->

## 3. UI Layer (Compose Multiplatform)
### Design System
- [ ] Define Typography, Colors (Material 3), and Theme <!-- id: 18 -->

### Screens
- [ ] **Main List Screen** <!-- id: 19 -->
    - [ ] List of words with popular translation <!-- id: 20 -->
    - [ ] Search bar <!-- id: 21 -->
    - [ ] Sort options (Alphabetical, Asc/Desc, tags) <!-- id: 22 -->
    - [ ] FAB to add <!-- id: 23 -->
    - [ ] Settings button <!-- id: 24 -->
- [ ] **Word Detail Screen** (Modal) <!-- id: 25 -->
    - [ ] Show translation, examples, opposites, tags <!-- id: 26 -->
    - [ ] TTS button (Android only) <!-- id: 56 -->
    - [ ] ESC to close (Desktop only) <!-- id: 57 -->
- [ ] **Add Screen** (Modal) <!-- id: 27 -->
    - [ ] Input field for word <!-- id: 28 -->
    - [ ] Tags input field <!-- id: 58 -->
    - [ ] ESC to close (Desktop only) <!-- id: 59 -->
- [ ] **Edit Screen** (Modal) <!-- id: 60 -->
    - [ ] Input field for word <!-- id: 61 -->
    - [ ] Manual edit fields for translations/examples <!-- id: 30 -->
    - [ ] Tags input field <!-- id: 62 -->
    - [ ] FAB (Save) <!-- id: 63 -->
    - [ ] ESC to close (Desktop only) <!-- id: 64 -->
- [ ] **Settings Screen** <!-- id: 31 -->
    - [ ] Google Login button <!-- id: 32 -->
    - [ ] Account info display <!-- id: 33 -->
    - [ ] Manual Sync button <!-- id: 34 -->
    - [ ] Tag list <!-- id: 65 -->
    - [ ] ESC to close (Desktop only) <!-- id: 66 -->

## 4. Android Specific Features
- [ ] **Widget (Glance)** <!-- id: 35 -->
    - [ ] `VocabWidget` display logic (random subset) <!-- id: 36 -->
    - [ ] `VocabWidgetReceiver` for updates <!-- id: 37 -->
    - [ ] Click intents (Open App/Detail) <!-- id: 38 -->
    - [ ] FAB (+) to add new word to DB <!-- id: 39 -->

## 5. Integration & Polish
- [ ] Google Sign-in integration (KMPAuth) <!-- id: 40 -->
- [ ] Token management (SharedPreferences/Keychain) <!-- id: 41 -->
- [ ] Testing & Bug Fixes <!-- id: 42 -->

## 6. Debugging
- [ ] How to handle adding same word