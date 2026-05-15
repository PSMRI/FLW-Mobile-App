# CLAUDE.md — FLW Mobile App

This file gives AI coding agents (Claude Code, Cursor, Copilot) instant
contextual knowledge of the FLW-Mobile-App codebase so they can assist
without requiring manual context pasting.

---

## What this app is

The FLW (Field-Level Worker) Mobile App is an Android application built
on the AMRIT platform. It digitises the daily work of ASHA community
health workers across India — household registration, pregnancy and
delivery tracking, child health records, NCD screening, and immunization.

**Critical constraint:** The app is offline-first. ASHA workers operate
in low-connectivity rural environments. Every feature must work without
internet and sync when connectivity returns.

---

## Tech stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| Min SDK | 25 (Android 7.1) |
| Target SDK | 35 |
| Architecture | MVVM + Repository |
| DI | Hilt |
| Database | Room 2.6.1 + SQLCipher (encrypted) |
| Background sync | WorkManager 2.10.2 |
| Notifications | Firebase Cloud Messaging |
| Analytics | Firebase Analytics |
| Concurrency | Kotlin Coroutines + Flow |
| Languages | English, Hindi (values-hi/), Assamese (values-as/) |

---

## Package structure

```
org.piramalswasthya.sakhi
├── database/
│   ├── converters/          # Room TypeConverters
│   ├── room/
│   │   ├── dao/             # All DAO interfaces
│   │   │   └── dynamicSchemaDao/
│   │   └── InAppDb.kt       # @Database class — single source of truth
│   └── shared_preferences/
│       └── PreferenceDao.kt # All SharedPreferences access
├── di/                      # Hilt modules
├── gamification/            # Gamification engine (sealed events, engine, catalog)
├── helpers/                 # Utility classes
├── model/                   # All Room @Entity data classes
├── network/                 # Retrofit API services
├── repositories/            # Data layer — one repo per domain
├── ui/
│   ├── home_activity/       # Feature fragments (one folder per feature)
│   │   └── <feature>/
│   │       ├── <Feature>Fragment.kt
│   │       └── <Feature>ViewModel.kt
│   └── ...
├── work/                    # WorkManager workers
└── SakhiApplication.kt
```

---

## Critical conventions

### 1. Database (Room)

**DB class:** `InAppDb` — NOT `AppDatabase`. Located at
`database/room/InAppDb.kt`. Current version: **58**.

**Never** bump the version without adding a migration.

**Entity pattern:**
```kotlin
@Entity(tableName = "MY_TABLE")
data class MyCache(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Long,

    @ColumnInfo(name = "myField")
    val myField: String,

    // Always use Int for sync state, never Boolean
    @ColumnInfo(name = "syncState")
    val syncState: Int = 0   // 0=UNSYNCED, 1=SYNCING, 2=SYNCED
)
```

**syncState convention:** Always `Int`, never `Boolean isSynced`.
- 0 = UNSYNCED (dirty, needs sync)
- 1 = SYNCING
- 2 = SYNCED

**Migration pattern — always use guards:**
```kotlin
val MIGRATION_X_Y = object : Migration(X, Y) {
    override fun migrate(database: SupportSQLiteDatabase) {
        if (!tableExists(database, "MY_TABLE")) {
            database.execSQL("CREATE TABLE `MY_TABLE` (...)")
        }
        if (!columnExists(database, "MY_TABLE", "myColumn")) {
            database.execSQL("ALTER TABLE MY_TABLE ADD COLUMN myColumn TEXT")
        }
    }
}
```

`tableExists()` and `columnExists()` are companion object functions on
`InAppDb`. Always use them — never assume a table or column exists.

**Adding a new entity checklist:**
1. Create `model/MyCache.kt` with `@Entity`
2. Create `database/room/dao/MyDao.kt` with `@Dao`
3. Add `MyCache::class` to entities list in `@Database` annotation in `InAppDb`
4. Add `abstract val myDao: MyDao` to `InAppDb`
5. Bump version by 1
6. Add `MIGRATION_X_Y` with `tableExists` guard
7. Register migration in `builder.addMigrations(...)`
8. Add Hilt provider in appropriate `di/` module

---

### 2. DAO pattern

```kotlin
@Dao
interface MyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: MyCache)

    @Query("SELECT * FROM MY_TABLE WHERE id = :id")
    suspend fun getById(id: Long): MyCache?

    // Use Flow for observation — never LiveData
    @Query("SELECT * FROM MY_TABLE WHERE syncState = 0")
    fun observeUnsynced(): Flow<List<MyCache>>

    @Query("UPDATE MY_TABLE SET syncState = 2 WHERE id = :id")
    suspend fun markSynced(id: Long)
}
```

---

### 3. Repository pattern

```kotlin
@Singleton
class MyRepo @Inject constructor(
    private val myDao: MyDao,
    private val apiService: AmritApiService,
    private val prefDao: PreferenceDao
) {
    // All DB calls — no context switch needed, Room handles threading
    suspend fun getById(id: Long) = myDao.getById(id)

    // All network calls — always switch to IO dispatcher
    suspend fun syncToServer(): NetworkResult<String> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.myEndpoint(...)
                if (response.isSuccessful) NetworkResult.Success(...)
                else NetworkResult.Error(response.code(), ...)
            } catch (e: IOException) {
                NetworkResult.Error(-1, "Unable to connect to Internet!")
            } catch (e: SocketTimeoutException) {
                NetworkResult.Error(-3, "Request Timed out! Please try again!")
            }
        }
    }
}
```

---

### 4. ViewModel pattern

```kotlin
@HiltViewModel
class MyViewModel @Inject constructor(
    private val myRepo: MyRepo
) : ViewModel() {

    // Use StateFlow, never LiveData
    val myData: StateFlow<MyData?> = myRepo
        .observeMyData()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun doSomething() {
        viewModelScope.launch {
            myRepo.doSomething()
        }
    }
}
```

---

### 5. Fragment pattern

```kotlin
@AndroidEntryPoint
class MyFragment : Fragment() {

    private val viewModel: MyViewModel by viewModels()
    private var _binding: FragmentMyBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        _binding = FragmentMyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.myData.collect { data -> render(data) }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null   // Always null binding to prevent memory leaks
    }
}
```

---

### 6. WorkManager worker pattern

```kotlin
@HiltWorker
class MySyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val myRepo: MyRepo
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // do sync
            Result.success()
        } catch (e: CancellationException) {
            throw e   // Always rethrow CancellationException
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    companion object {
        fun schedule(workManager: WorkManager) {
            workManager.enqueueUniquePeriodicWork(
                "MySyncWorker",
                ExistingPeriodicWorkPolicy.KEEP,
                PeriodicWorkRequestBuilder<MySyncWorker>(6, TimeUnit.HOURS)
                    .setConstraints(Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build())
                    .build()
            )
        }
    }
}
```

---

### 7. Getting the logged-in user

```kotlin
// Inject PreferenceDao and call:
val user = preferenceDao.getLoggedInUser()  // returns User?
val userId = user?.userId                    // Int
val userName = user?.userName               // String
```

Never access SharedPreferences directly — always use `PreferenceDao`.

---

### 8. API 25 compatibility rules

- **Never use** `java.time.*` — requires API 26. Use `java.util.Calendar` + `SimpleDateFormat`
- **Never use** `paddingHorizontal`/`paddingVertical` XML attributes — use `paddingStart`/`paddingEnd`/`paddingTop`/`paddingBottom`
- **Never use** `ThreadLocal.withInitial {}` — use `object : ThreadLocal<T>() { override fun initialValue() = ... }`

---

### 9. Localization

All user-facing strings must exist in three files:
- `res/values/strings.xml` — English
- `res/values-hi/strings.xml` — Hindi
- `res/values-as/strings.xml` — Assamese

Never hardcode user-visible text in Kotlin or XML layout files.

---

### 10. What NOT to do

- Do not use `LiveData` — the project uses `Flow` + `StateFlow`
- Do not use `kotlin-android-extensions` (deprecated) — use ViewBinding
- Do not bump DB version without a migration
- Do not mark records as `syncState = 2` (SYNCED) before confirmed API success
- Do not access SharedPreferences directly — use `PreferenceDao`
- Do not use `java.time.*` — minSdk 25

---

## Health domain context

The app tracks:
- **Beneficiaries** — registered via `BenRegCache`, identified by `beneficiaryId: Long`
- **Households** — registered via `HouseholdCache`, identified by `householdId: Long`
- **Pregnant women** — `PregnantWomanRegistrationCache`, ANC via `PregnantWomanAncCache`
- **Delivery outcomes** — `DeliveryOutcomeCache`
- **Children** — `ChildRegCache`, `InfantRegCache`
- **Immunization** — `ImmunizationCache`
- **HRP (High Risk Pregnancy)** — `HRPPregnantAssessCache`, `HRPPregnantTrackCache`
- **NCD screening** — via dynamic forms
- **CBAC** — Community Based Assessment Checklist

The `BenBasicCache` is a Room `@DatabaseView` that joins most of these
into a single queryable view used by the main beneficiary list screen.

---

## Gamification module (added in v58)

Located at `gamification/`. Consists of:
- `GamificationEvent` — sealed class of health worker actions
- `GamificationEngine` — processes events, awards XP, updates streaks, unlocks badges
- `BadgeCatalog` — trilingual badge definitions

To trigger gamification from a health form after a successful save:
```kotlin
gamificationViewModel.onHealthEvent(
    userId,
    GamificationEvent.AncVisitCompleted(benId.toString())
)
```

Available events: `HouseholdRegistered`, `BeneficiaryRegistered`,
`AncVisitCompleted`, `DeliveryOutcomeRecorded`, `PncVisitCompleted`,
`ImmunizationRecorded`, `HrpCaseIdentified`, `NcdScreeningCompleted`,
`CbacFormFilled`, `DailyLogin`.
