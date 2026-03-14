# Task List

## 1. Project Setup & Core Architecture
- [x] Configure KMP project with `androidApp` and `desktopApp` targets
- [x] Setup Koin for dependency injection
    - [ ] Koin ViewModels using androidx-lifecycle-viewmodel
- [x] Setup SQLDelight for database
- [ ] Setup Google OAuth2 for Android and Desktop
    - [x] JVM 
        - [x] get access and refresh token
        - [x] save token to secure storage (DPAPI + DataStore)
        - [x] get ID token
        - [x] save ID token to secure storage
    - [x] Android
        - [x] get access and refresh token
        - [x] save token to secure storage (AndroidKeyStore + Google Tink + DataStore)
        - [x] get ID token
        - [x] save ID token to secure storage
    - [x] load ID token on client Bearer Header
    - [ ] Silent Sign in on App Start
- [x] Setup Ktor for network requests and proxy server
- [x] Setup Google Cloud Run for proxy server
    - [x] need to add Auth Header to requests 
        - [x] get tokens from secure storage
    - [ ] need to check refreshing token works
- [ ] Setup Turso for cloud storage
    - [x] pull words from remote DB
    - [x] push words to remote DB
    - [ ] Sync words to remote DB 
        - [x]check internet is available 
        - [x] push from front
        - [x] pull from back
        - [x] compare at server
        - [x] last write wins
        - [x] return names of words that are synced
- [x] Setup Gemini
- [ ] Connect Google Cloud Run to Turso and Gemini
    - [x] assign url
    - [x] turso
    - [x] gemini
    - [x] check id token for all connections
    - [ ] need to check refreshing token works
- [ ] Tag Management
- [ ] Setup Glance for Android widget

## 2. Data Layer
### Model
- [x] Define `Word` class
    - [x] Add `name`,  `meaningKr`, `example`, `oppositeEn`, `tags`, `createdTime`, `modifiedTime`, `isDeleted`, `synced`, `note` fields
- [ ] Define `Tag` class
    - [ ] Add `tag_name` and `color` fields

### Storage (SQLDelight)
- [x] Create database and tables
- [x] Create columns including `tags`, `createdTime`, `modifiedTime`, `isDeleted`, `synced`, `note`
- [x] Implement `insertWord(word: Word)` (set createdTime, modifiedTime)
- [x] Implement `deleteWord(word: Word)` (soft delete: set isDeleted=true, update modifiedTime)
- [x] Implement `updateWord(word: Word)` (update modifiedTime too)
- [x] Implement `syncDB()` logic
    - [x] Get all `modifiedTime > LastSyncTime` words
    - [x] Batch update remote DB (check `modifiedTime` to determine win)
    - [x] Update all `LastSyncTime` if success
    - [x] Batch update local DB from remote
    - [x] Show sync results (deleted, updated, added)
- [ ] Tag Management
    - [x] `tag_name` and `color` field
    - [x] Add tag
    - [x] Delete tag
    - [x] Update tag
        - [x] cannot be empty
    - [ ] Get similar tags
    - [x] Delete tags that are not used
        - [ ] In setting add "Delete unused tags" button

### API Services
- [x] Implement `GoogleSignInService`
- [ ] Implement `GeminiService`
    - [x] Prompt Engineering
    - [x] miss-spelling correction
    - [x] JSON response
- [x] Implement `TursoService`
    - [x] `syncDB()`: sync localDB and remoteDB

## 3. UI Layer (Compose Multiplatform)
### Design System
- [x] Define Theme of colors

### Screens
- [ ] **Main List Screen**
    - [ ] List of words with popular translation
    - [ ] Search bar
        - [ ] input field
            - [ ] filter by content or tags
        - [ ] button to switch filter
        - [ ] Debounce Search (300ms)
    - [ ] Sort button
        - [ ] options: Alphabetical, Asc/Desc, tags
    - [ ] FAB to add
    - [ ] Settings button
    - [ ] Login Status Display
- [ ] **Word Detail Screen** (Modal)
    - [ ] Show translation, examples, opposites, tags
    - [ ] TTS button (Android only)
    - [ ] ESC to close (Desktop only)
- [ ] **Add Screen** (Modal)
    - [ ] Input field for word
    - [ ] gemini button to generate fields
        - [ ] show error if not Google Sign-in
    - [ ] other editable fields (meaningKr, example, oppositeEn, tags, note)
    - [ ] tags field showing similar tags (suggestion)
    - [ ] save button (FAB)
    - [ ] ESC to close (Desktop only)
- [ ] **Edit Screen** (Modal)
    - [ ] editable fields (word, meaningKr, example, oppositeEn, tags, note)
    - [ ] save button (FAB)
    - [ ] ESC to close (Desktop only)
- [ ] **Settings Screen**
    - [ ] Google Login button
    - [ ] Account info display
    - [ ] Manual Sync button
    - [ ] Tag list
    - [ ] ESC to close (Desktop only)
    - [ ] The Legal/Credits Button
    - [ ] Delete unused tags button
- [ ] The Legal/Credits Screen

### Loding / Skeleton UI
- [ ] Add Skeleton UI for loading states

### Logic to UI
- [ ] AndroidX Lifecycle ViewModel (share logics between Android and Desktop)
- [ ] Flow
- [ ] Koin ViewModels

### Navigation
- [ ] Navigation Compose (Multiplatform)

### Text and Fonts
- [ ] Compose Resources (support multiple languages, fonts for both platforms)

### Desktop Specific Features
- [ ] add hotkeys for desktop
- [ ] hover effects
- [ ] Tab Navigation
- [ ] remember window size and position

## 4. Android Specific Features
- [ ] **Widget (Glance)**
    - [ ] `VocabWidget` display logic (random subset)
    - [ ] `VocabWidgetReceiver` for updates
    - [ ] Click intents (Open App/Detail)
    - [ ] FAB (+) to add new word to DB

## 5. Integration & Polish & Debugging
- [x] createWord function that does not need all fields
- [ ] convert UTC to local time when displaying created/modified time
- [ ] Tag Management
- [ ] Gemini Model Switching if Request Per Day is exceeded
- [x] load tokens to header after google login (right now the app needs to be restarted)
- [ ] Sync Conflict UI: ask to choose which one to keep when conflict happens
- [ ] App Icon
