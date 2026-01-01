# Task List

## 1. Project Setup & Core Architecture
- [x] Configure KMP project with `androidApp` and `desktopApp` targets
- [x] Setup Koin for dependency injection
- [x] Setup SQLDelight for database
- [ ] Setup Google OAuth2 for Android and Desktop
    - [x] JVM 
        - [x] get access and refresh token
        - [ ] save token to secure storage
    - [ ] Android
        - [ ] get access and refresh token
        - [ ] save token to secure storage
- [x] Setup Ktor for network requests and proxy server
- [x] Setup Google Cloud Run for proxy server
    - [ ] need to add Auth Header to requests 
- [ ] Setup Turso for cloud storage
- [ ] Setup Gemini
- [ ] Connect Google Cloud Run to Turso and Gemini
    - [ ] assign url
    - [ ] 
- [ ] Setup Glance for Android widget

## 2. Data Layer
### Model
- [x] Define `Word` class
- [x] Add `name`,  `meaningKr`, `example`, `oppositeEn`, `tags`, `created`, `modified`, `isDeleted`, `synced` fields

### Storage (SQLDelight)
- [x] Create database and tables
- [x] Create columns including `tags`, `CreatedTime`, `LastModifiedTime`, `isDeleted`, `synced`
- [x] Implement `insertWord(word: Word)` (set CreatedTime, LastModifiedTime)
- [x] Implement `deleteWord(word: Word)` (soft delete: set isDeleted=true, update LastModifiedTime)
- [x] Implement `updateWord(word: Word)` (update LastModifiedTime)
- [ ] Implement `syncDB()` logic
    - [ ] Get all `synced=false` words
    - [ ] Batch update remote DB (check `modified` to determine win)
    - [ ] Update all `synced=true` if success
    - [ ] Batch update local DB from remote
    - [ ] Show sync results (deleted, updated, added)

### API Services
- [x] Implement `GoogleSignInService`
- [ ] Implement `GeminiService`
    - [ ] Prompt Engineering
    - [ ] Function `enrichWord(word: String): Word`
    - [ ] Prompt engineering for strict JSON response
    - [ ] Implement `resultValidation(JSON)` to validate and parse response
- [ ] Implement `TursoService`
    - [ ] `syncDB()`: sync localDB and remoteDB

## 3. UI Layer (Compose Multiplatform)
### Design System
- [ ] Define Typography, Colors (Material 3), and Theme

### Screens
- [ ] **Main List Screen**
    - [ ] List of words with popular translation
    - [ ] Search bar
    - [ ] Sort options (Alphabetical, Asc/Desc, tags)
    - [ ] FAB to add
    - [ ] Settings button
- [ ] **Word Detail Screen** (Modal)
    - [ ] Show translation, examples, opposites, tags
    - [ ] TTS button (Android only)
    - [ ] ESC to close (Desktop only)
- [ ] **Add Screen** (Modal)
    - [ ] Input field for word
    - [ ] Tags input field
    - [ ] ESC to close (Desktop only)
- [ ] **Edit Screen** (Modal)
    - [ ] Input field for word
    - [ ] Manual edit fields for translations/examples
    - [ ] Tags input field
    - [ ] FAB (Save)
    - [ ] ESC to close (Desktop only)
- [ ] **Settings Screen**
    - [ ] Google Login button
    - [ ] Account info display
    - [ ] Manual Sync button
    - [ ] Tag list
    - [ ] ESC to close (Desktop only)

## 4. Android Specific Features
- [ ] **Widget (Glance)**
    - [ ] `VocabWidget` display logic (random subset)
    - [ ] `VocabWidgetReceiver` for updates
    - [ ] Click intents (Open App/Detail)
    - [ ] FAB (+) to add new word to DB

## 5. Integration & Polish
- [ ] Google Sign-in integration (KMPAuth)
- [ ] Token management (SharedPreferences/Keychain)
- [ ] Testing & Bug Fixes

## 6. Debugging & Polish
- [ ] How to handle adding same word