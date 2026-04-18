# Collections Follow-Up Assistant - Database Schema Documentation

## Overview
Complete database schema with **3 local entities** (Room), **3 Firestore models**, and **3 domain models** for the Collections Follow-Up Assistant hackathon project.

---

## 1. LOCAL DATABASE (Room) - SQLite

### A. AccountEntity
**Table:** `accounts`
```
- id (Primary Key): String [UUID]
- customerId: String
- customerName: String
- contactNumber: String
- email: String? (nullable)
- totalAmountDue: Double
- amountPaid: Double (default: 0.0)
- amountRemaining: Double
- dueDate: Date (java.util.Date)
- createdAt: Date
- updatedAt: Date
- lastFollowUpDate: Date? (nullable)
- nextFollowUpDate: Date? (nullable)
- status: String ["active", "paid", "overdue", "partial"]
- notes: String? (nullable)
- userId: String (Firebase user ID)
- isSynced: Boolean (default: false)
```

**Queries Available:**
- getAllAccountsByUser(userId)
- getOverdueAccounts(userId)
- getDueAccountsToday(userId, today)
- getTotalPendingAmount(userId)
- getFollowUpsNeededToday(userId, today)
- getUnsyncedAccounts(userId)

---

### B. FollowUpEntity
**Table:** `follow_ups`
**Foreign Key:** accountId → accounts.id (CASCADE delete)

```
- id (Primary Key): String [UUID]
- accountId: String (Foreign Key)
- customerId: String
- followUpDate: Date
- status: String ["pending", "contacted", "completed", "rescheduled", "not_reachable"]
- outcome: String? ["no_response", "promise_to_pay", "paid_partial", "paid_full", "declined"]
- contactMethod: String? ["call", "whatsapp", "sms", "in_person"]
- suggestedMessage: String? (AI generated)
- actualMessage: String? (user edited)
- promiseDate: Date? (when customer promises to pay)
- nextFollowUpDate: Date? (auto-scheduled)
- createdAt: Date
- updatedAt: Date
- notes: String?
- userId: String
- isSynced: Boolean (default: false)
```

**Queries Available:**
- getFollowUpsByAccount(accountId)
- getPendingFollowUps(userId)
- getTodayFollowUps(userId, today)
- getTopPendingFollowUps(userId, today) [LIMIT 10]
- getUnsyncedFollowUps(userId)

---

### C. PaymentHistoryEntity
**Table:** `payment_history`
**Foreign Key:** accountId → accounts.id (CASCADE delete)

```
- id (Primary Key): String [UUID]
- accountId: String (Foreign Key)
- customerId: String
- amount: Double
- paymentDate: Date
- paymentMethod: String? ["cash", "check", "transfer", "card"]
- transactionId: String? (reference number)
- notes: String?
- createdAt: Date
- userId: String
- isSynced: Boolean (default: false)
```

**Queries Available:**
- getPaymentsByAccount(accountId)
- getAllPayments(userId)
- getTotalPaymentsByAccount(accountId)
- getUnsyncedPayments(userId)

---

## 2. FIRESTORE REMOTE MODELS

### A. AccountFirestore
**Collection Path:** `users/{userId}/accounts/{accountId}`

```kotlin
data class AccountFirestore(
    id: String,
    customerId: String,
    customerName: String,
    contactNumber: String,
    email: String?,
    totalAmountDue: Double,
    amountPaid: Double,
    amountRemaining: Double,
    dueDate: Long (timestamp in milliseconds),
    createdAt: Long,
    updatedAt: Long,
    lastFollowUpDate: Long?,
    nextFollowUpDate: Long?,
    status: String,
    notes: String?,
    userId: String
)
```

---

### B. FollowUpFirestore
**Collection Path:** `users/{userId}/followUps/{followUpId}`

```kotlin
data class FollowUpFirestore(
    id: String,
    accountId: String,
    customerId: String,
    followUpDate: Long,
    status: String,
    outcome: String?,
    contactMethod: String?,
    suggestedMessage: String?,
    actualMessage: String?,
    promiseDate: Long?,
    nextFollowUpDate: Long?,
    createdAt: Long,
    updatedAt: Long,
    notes: String?,
    userId: String
)
```

---

### C. PaymentHistoryFirestore
**Collection Path:** `users/{userId}/payments/{paymentId}`

```kotlin
data class PaymentHistoryFirestore(
    id: String,
    accountId: String,
    customerId: String,
    amount: Double,
    paymentDate: Long,
    paymentMethod: String?,
    transactionId: String?,
    notes: String?,
    createdAt: Long,
    userId: String
)
```

---

## 3. DOMAIN MODELS (UI Layer)

### A. Account
```kotlin
data class Account(
    id: String,
    customerId: String,
    customerName: String,
    contactNumber: String,
    email: String?,
    totalAmountDue: Double,
    amountPaid: Double,
    amountRemaining: Double,
    dueDate: Date,
    createdAt: Date,
    updatedAt: Date,
    lastFollowUpDate: Date?,
    nextFollowUpDate: Date?,
    status: String,
    notes: String?,
    daysOverdue: Int
)
```

---

### B. FollowUp
```kotlin
data class FollowUp(
    id: String,
    accountId: String,
    customerId: String,
    followUpDate: Date,
    status: String,
    outcome: String?,
    contactMethod: String?,
    suggestedMessage: String?,
    actualMessage: String?,
    promiseDate: Date?,
    nextFollowUpDate: Date?,
    createdAt: Date,
    updatedAt: Date,
    notes: String?
)
```

---

### C. PaymentHistory
```kotlin
data class PaymentHistory(
    id: String,
    accountId: String,
    customerId: String,
    amount: Double,
    paymentDate: Date,
    paymentMethod: String?,
    transactionId: String?,
    notes: String?,
    createdAt: Date
)
```

---

### D. DashboardSummary (Aggregated)
```kotlin
data class DashboardSummary(
    totalPendingAmount: Double,
    overdueAccountsCount: Int,
    followUpsNeededToday: Int,
    overdueAccounts: List<Account>
)
```

---

## 4. MAPPERS (Data Layer Conversion)

Three mapper objects for converting between layers:

1. **AccountMappers**
   - `AccountEntity.toDomain(): Account`
   - `Account.toEntity(userId): AccountEntity`
   - `AccountEntity.toFirestore(): AccountFirestore`
   - `AccountFirestore.toEntity(userId): AccountEntity`

2. **FollowUpMappers**
   - `FollowUpEntity.toDomain(): FollowUp`
   - `FollowUp.toEntity(userId): FollowUpEntity`
   - `FollowUpEntity.toFirestore(): FollowUpFirestore`
   - `FollowUpFirestore.toEntity(userId): FollowUpEntity`

3. **PaymentHistoryMappers**
   - `PaymentHistoryEntity.toDomain(): PaymentHistory`
   - `PaymentHistory.toEntity(userId): PaymentHistoryEntity`
   - `PaymentHistoryEntity.toFirestore(): PaymentHistoryFirestore`
   - `PaymentHistoryFirestore.toEntity(userId): PaymentHistoryEntity`

---

## 5. DATA ACCESS OBJECTS (DAOs)

### AccountDao
- CRUD operations
- User-based queries
- Status-based filtering
- Due date calculations
- Sync tracking

### FollowUpDao
- CRUD operations
- Account-based history
- Pending follow-ups
- Daily scheduled follow-ups
- Sync tracking

### PaymentHistoryDao
- Insert & delete operations
- Account-based payment history
- Total payment calculations
- Sync tracking

---

## 6. DATABASE CONFIGURATION

**AppDatabase (Room)**
- Version: 2
- Entities: 4 (SyncItemEntity + 3 new entities)
- DAOs: 4 (SyncItemDao + 3 new DAOs)
- Type Converters: RoomConverters (Date ↔ Long, List ↔ JSON)

---

## 7. BUILD VERIFICATION

✅ **Build Status: SUCCESS**
- All Kotlin files compile without errors
- Room schema generation: OK
- Firebase Firestore mappings: OK
- Hilt dependency injection: OK

---

## 8. SYNC STRATEGY

Each entity has an `isSynced` boolean flag:
- **false** = needs to be uploaded to Firestore
- **true** = synced with Firebase

Use `getUnsynced*()` queries to retrieve offline-created records for upload.

---

## Files Created

```
app/src/main/java/com/app/softec/
├── data/
│   ├── local/
│   │   ├── entity/
│   │   │   ├── AccountEntity.kt
│   │   │   ├── FollowUpEntity.kt
│   │   │   └── PaymentHistoryEntity.kt
│   │   ├── dao/
│   │   │   ├── AccountDao.kt
│   │   │   ├── FollowUpDao.kt
│   │   │   └── PaymentHistoryDao.kt
│   │   └── AppDatabase.kt (UPDATED)
│   ├── remote/firestore/
│   │   ├── AccountFirestore.kt
│   │   ├── FollowUpFirestore.kt
│   │   └── PaymentHistoryFirestore.kt
│   └── mapper/
│       ├── AccountMappers.kt
│       ├── FollowUpMappers.kt
│       └── PaymentHistoryMappers.kt
└── domain/model/
    ├── Account.kt
    ├── FollowUp.kt
    ├── PaymentHistory.kt
    └── DashboardSummary.kt
```

---

## Next Steps

1. ✅ Schemas created and compiled
2. Next: Create ViewModels with use cases
3. Then: Implement Firestore repositories for sync
4. Then: Build UI screens with Compose
5. Finally: Connect Firebase auth to flows

**Ready to proceed with screen implementation!**

