# Farm Directory Security Implementation

This document outlines all security, validation, and performance optimizations implemented in the Farm Directory app.

## Table of Contents
1. [Input Validation](#input-validation)
2. [Data Sanitization](#data-sanitization)
3. [Secure Storage](#secure-storage)
4. [Database Security](#database-security)
5. [Network Security](#network-security)
6. [Performance Optimizations](#performance-optimizations)

---

## Input Validation

### Location
`/app/src/main/java/com/example/farmdirectoryupgraded/utils/ValidationUtils.kt`

### Features Implemented

#### 1. Email Validation
- Uses Android's built-in `Patterns.EMAIL_ADDRESS` for RFC-compliant validation
- Maximum length: 254 characters (RFC 5321)
- Validates format: `user@domain.com`

#### 2. Phone Number Validation
- Supports international formats with `+` prefix
- Minimum 10 digits, maximum 15 digits
- Accepts common separators: `()`, `-`, `.`, space
- Validates only numeric characters after cleanup

#### 3. URL Validation
- Validates protocol: `http://`, `https://`, `ws://`, `wss://`
- Uses Android's `Patterns.WEB_URL` for format validation
- Maximum length: 2048 characters
- Required field validation

#### 4. GPS Coordinates Validation
- Latitude: -90.0 to 90.0
- Longitude: -180.0 to 180.0
- Validates numeric format
- Optional fields

#### 5. Farm ID Validation
- Alphanumeric with hyphens and underscores only
- Minimum 3 characters, maximum 50 characters
- Regex: `^[a-zA-Z0-9_-]+$`

#### 6. Worker Name Validation
- Minimum 2 characters, maximum 100 characters
- Required field

### Usage Example
```kotlin
val emailValidation = ValidationUtils.validateEmail(email)
if (!emailValidation.isValid) {
    // Display error: emailValidation.errorMessage
}
```

---

## Data Sanitization

### Location
`/app/src/main/java/com/example/farmdirectoryupgraded/utils/SanitizationUtils.kt`

### Features Implemented

#### 1. Text Sanitization
- Removes HTML/script injection characters: `<>\"'\``
- Normalizes whitespace
- Trims leading/trailing spaces
- Maximum length: 500 characters

#### 2. Email Sanitization
- Converts to lowercase
- Trims whitespace
- Maximum length: 254 characters

#### 3. Phone Number Sanitization
- Keeps only digits, `+`, `()`, `-`, `.`, space
- Maximum length: 20 characters

#### 4. URL Sanitization
- Removes all whitespace
- Maximum length: 2048 characters

#### 5. Alphanumeric Sanitization
- For IDs: keeps only `a-zA-Z0-9_-`
- Maximum length: 50 characters

#### 6. SQL String Escaping
- Escapes single quotes to prevent SQL injection
- Note: Room uses parameterized queries, this is an extra layer

#### 7. Address Sanitization
- Allows more characters than regular text
- Removes script injection characters
- Maximum length: 1000 characters

### Usage Example
```kotlin
val sanitizedEmail = SanitizationUtils.sanitizeEmail(rawEmail)
val sanitizedName = SanitizationUtils.sanitizeText(rawName)
```

---

## Secure Storage

### Location
`/app/src/main/java/com/example/farmdirectoryupgraded/security/SecureSettingsManager.kt`

### Features Implemented

#### EncryptedSharedPreferences
- Uses AndroidX Security Crypto library
- AES256-GCM encryption for values
- AES256-SIV encryption for keys
- MasterKey backed by Android Keystore

#### Stored Data (Encrypted)
- Access tokens
- Refresh tokens
- API keys
- Worker IDs
- Session tokens

#### Security Features
- Automatic key generation
- Hardware-backed encryption (when available)
- Secure key storage in Android Keystore
- Protection against root access

### Usage Example
```kotlin
val secureSettings = SecureSettingsManager(context)
secureSettings.setAccessToken("token123")
val token = secureSettings.getAccessToken()
secureSettings.clearAll() // Logout
```

---

## Database Security

### SQL Injection Prevention

#### Location
`/app/src/main/java/com/example/farmdirectoryupgraded/data/FarmerDao.kt`

#### Features
- All queries use Room's parameterized statements
- Automatic parameter escaping
- No string concatenation in queries
- Type-safe query parameters

#### Example
```kotlin
// Safe - parameterized query
@Query("SELECT * FROM farmers WHERE name = :name")
suspend fun getFarmerByName(name: String): Farmer?

// Room automatically escapes :name parameter
```

### Database Indexes

#### Location
`/app/src/main/java/com/example/farmdirectoryupgraded/data/Farmer.kt`

#### Implemented Indexes
1. `idx_farmer_name` - On `name` column
2. `idx_farmer_type` - On `type` column
3. `idx_farmer_favorite` - On `isFavorite` column
4. `idx_farmer_health_status` - On `healthStatus` column
5. `idx_farmer_farm_name` - On `farmName` column

#### Performance Benefits
- Faster queries on indexed columns
- Improved search performance
- Optimized filtering operations
- Better ORDER BY performance

---

## Network Security

### SSL/TLS Certificate Pinning

#### Location
`/app/src/main/java/com/example/farmdirectoryupgraded/security/CertificatePinning.kt`

#### Features
- OkHttp-based certificate pinning
- Prevents man-in-the-middle attacks
- SHA-256 fingerprint validation
- Backup certificate support

#### Configuration
```kotlin
val client = CertificatePinning.createSecureOkHttpClient("yourserver.com")
```

#### Getting Certificate Pins
```bash
# Method 1: Using OpenSSL
openssl s_client -connect yourserver.com:443 | \
openssl x509 -pubkey -noout | \
openssl pkey -pubin -outform der | \
openssl dgst -sha256 -binary | \
openssl enc -base64

# Method 2: Using SSL Labs
Visit: https://www.ssllabs.com/ssltest/
```

#### Production Setup
Replace placeholder pins in `CertificatePinning.kt`:
```kotlin
"sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=" // Replace with actual pin
"sha256/BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB=" // Backup pin
```

---

## Performance Optimizations

### 1. Database Pagination

#### Location
`/app/src/main/java/com/example/farmdirectoryupgraded/data/FarmerDao.kt`

#### Features
- Paginated queries using LIMIT/OFFSET
- Prevents loading entire dataset into memory
- Smooth scrolling for large lists

#### Usage
```kotlin
// Load 20 farmers at a time
val farmers = farmerDao.getFarmersPaginated(limit = 20, offset = page * 20)
val totalCount = farmerDao.getFarmerCount()
```

### 2. Database Indexes
See [Database Security](#database-indexes) section above.

### 3. Query Optimization
- Queries use indexed columns in WHERE clauses
- Efficient ORDER BY using indexed columns
- LIMIT used to restrict result sets

---

## Form Validation Implementation

### AddFarmerScreen
**Location**: `/app/src/main/java/com/example/farmdirectoryupgraded/ui/FarmerEditScreens.kt`

#### Validated Fields
- Name (required)
- Farm Name (required)
- Email (optional, format validated)
- Phone (optional, format validated)
- Cell Phone (optional, format validated)
- Latitude (optional, range validated)
- Longitude (optional, range validated)

#### Features
- Real-time error display
- Clear error on user input
- Keyboard type hints (email, phone, decimal)
- Submit button disabled until required fields filled
- Error messages shown below fields

### EditFarmerScreen
Same validation as AddFarmerScreen, applied to edit operations.

### SettingsScreen
**Location**: `/app/src/main/java/com/example/farmdirectoryupgraded/ui/Screens.kt`

#### Validated Fields
- Backend URL (required, URL format)
- Farm ID (required, alphanumeric)
- Worker Name (required)

#### Features
- Validation before connection attempt
- Error display card for validation failures
- Auto-sanitization on input
- Placeholder text for URL format guidance

---

## Dependencies Added

### Security
```gradle
implementation("androidx.security:security-crypto:1.1.0-alpha06")
implementation("com.squareup.okhttp3:okhttp:4.12.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
```

### Performance
```gradle
implementation("androidx.room:room-paging:2.6.1")
implementation("androidx.paging:paging-runtime-ktx:3.2.1")
implementation("androidx.paging:paging-compose:3.2.1")
```

---

## Security Best Practices Followed

### 1. Defense in Depth
- Multiple layers of security (validation + sanitization + encryption)
- Client-side and database-level protections

### 2. Principle of Least Privilege
- Encrypted storage only for sensitive data
- Minimal permissions requested

### 3. Secure by Default
- All user inputs validated and sanitized
- Parameterized queries by default
- Certificate pinning for production

### 4. Privacy Protection
- Encrypted storage for tokens
- No sensitive data in logs
- Secure key management

### 5. Performance Optimization
- Database indexes on frequently queried columns
- Pagination for large datasets
- Efficient query patterns

---

## Testing Recommendations

### 1. Input Validation Testing
```kotlin
@Test
fun testEmailValidation() {
    val result = ValidationUtils.validateEmail("invalid-email")
    assertFalse(result.isValid)
    assertNotNull(result.errorMessage)
}
```

### 2. SQL Injection Testing
- Test with malicious inputs: `'; DROP TABLE farmers;--`
- Verify parameterized queries prevent injection

### 3. Certificate Pinning Testing
- Test with invalid certificates
- Verify connection fails with wrong pin

### 4. Performance Testing
- Test with 10,000+ farmers
- Verify pagination works correctly
- Measure query performance with indexes

---

## Migration Notes

### Database Migration from v3 to v4
The database schema was updated to add indexes. Room's `fallbackToDestructiveMigration()` is used, which will recreate the database. For production, implement proper migration:

```kotlin
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE INDEX idx_farmer_name ON farmers(name)")
        database.execSQL("CREATE INDEX idx_farmer_type ON farmers(type)")
        database.execSQL("CREATE INDEX idx_farmer_favorite ON farmers(isFavorite)")
        database.execSQL("CREATE INDEX idx_farmer_health_status ON farmers(healthStatus)")
        database.execSQL("CREATE INDEX idx_farmer_farm_name ON farmers(farmName)")
    }
}
```

---

## Future Security Enhancements

1. **Biometric Authentication**
   - Use BiometricPrompt API for sensitive operations

2. **Data Encryption at Rest**
   - SQLCipher for full database encryption

3. **API Rate Limiting**
   - Implement client-side rate limiting for API calls

4. **Network Security Config**
   - Add network_security_config.xml for additional certificate pinning

5. **ProGuard Rules**
   - Obfuscate security-sensitive code in release builds

6. **Runtime Security Checks**
   - Detect rooted devices
   - Verify app integrity

---

## Contact

For security concerns or questions, contact the development team.

Last Updated: 2025-12-25
Version: 2.0
