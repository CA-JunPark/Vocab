# Task List

## 1. Project Setup & Core Architecture
- [x] Configure KMP project with `androidApp` and `desktopApp` targets
- [x] Setup Koin for dependency injection
- [x] Setup SQLDelight for database
- [x] Setup Google OAuth2 for Android and Desktop
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
- [x] Setup Ktor for network requests and proxy server
- [x] Setup Google Cloud Run for proxy server
    - [x] need to add Auth Header to requests 
        - [x] get tokens from secure storage
    - [x] need to check refreshing token works
- [x] Setup Turso for cloud storage
    - [x] pull words from remote DB
    - [x] push words to remote DB
    - [x] Sync words to remote DB 
        - [x]check internet is available 
        - [x] push from front
        - [x] pull from back
        - [x] compare at server
        - [x] last write wins
        - [x] return names of words that are synced
- [x] Setup Gemini
- [x] Connect Google Cloud Run to Turso and Gemini
    - [x] assign url
    - [x] turso
    - [x] gemini
    - [x] check id token for all connections
    - [x] need to check refreshing token works
- [x] Tag Management
- [ ] Setup Glance for Android widget

## 2. Data Layer
### Model
- [x] Define `Word` class
    - [x] Add `name`,  `meaningKr`, `example`, `oppositeEn`, `tags`, `createdTime`, `modifiedTime`, `isDeleted`, `synced`, `note` fields
- [x] Define `Tag` class
    - [x] Add `tag_name` and `color` fields

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
- [x] Tag Management
    - [x] `tag_name` and `color` field
    - [x] Add tag
    - [x] Delete tag
    - [x] Update tag
        - [x] cannot be empty
    - [x] Get similar tags
    - [x] Delete tags that are not used
        - [x] In setting add "Delete unused tags" button

### API Services
- [x] Implement `GoogleSignInService`
- [x] Implement `GeminiService`
    - [x] Prompt Engineering
    - [x] miss-spelling correction
    - [x] JSON response
- [x] Implement `TursoService`
    - [x] `syncDB()`: sync localDB and remoteDB

## 3. UI Layer (Compose Multiplatform)
### Design System
- [x] Define Theme of colors

### Screens
- [x] **Main List Screen**
    - [x] List of words with popular translation
    - [x] Search bar
        - [x] input field
            - [x] filter by content or tags
        - [x] button to switch filter
        - [x] Debounce Search (300ms)
    - [x] Sort button
        - [x] options: Alphabetical, Asc/Desc, tags
    - [x] FAB to add
    - [x] Settings button
    - [x] Login Status Display
- [x] **Word Detail Screen** (Modal)
    - [x] Show translation, examples, opposites, tags
    - [ ] TTS button (Android only)
- [x] **Add Screen** (Modal)
    - [x] Input field for word
    - [x] gemini button to generate fields
        - [x] show error if not Google Sign-in
    - [x] other editable fields (meaningKr, example, oppositeEn, tags, note)
    - [x] tags field showing similar tags (suggestion)
    - [x] save button (FAB)
- [x] **Edit Screen** (Modal)
    - [x] editable fields (word, meaningKr, example, oppositeEn, tags, note)
    - [x] save button
- [ ] **Settings Screen**
    - [x] Google Login button
    - [x] Account info display
    - [x] Manual Sync button
    - [x] Tag list
    - [ ] The Legal/Credits Button
    - [x] Delete unused tags button
    - [x] Reset Sync Time button
- [ ] The Legal/Credits Screen

### Navigation
- [x] Navigation Compose (Multiplatform)

### Desktop Specific Features
- [x] remember window size and position

## 4. Android Specific Features
- [ ] **Widget (Glance)**
    - [x] `VocabWidget` display logic (random subset)
    - [x] `VocabWidgetReceiver` for updates
    - [x] Click intents (Open App/Detail)
    - [x] (+) to add new word to DB
    - [ ] auto update displayed word when there was no update for an hour

## 5. Integration & Polish & Debugging
- [x] createWord function that does not need all fields
- [ ] Gemini Model Switching if Request Per Day is exceeded
- [x] load tokens to header after google login (right now the app needs to be restarted)
- [x] App Icon
- [x] Hard Delete isDeleted = 1 words in local and turso db
- [x] Back to close (Ansroid only)
- [x] Android Safe Area
- [x] JVM build login issue

