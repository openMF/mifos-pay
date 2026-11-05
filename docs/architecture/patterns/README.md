# Patterns & Best Practices - Coding Standards Guide

**For:** Writing quality code
**Time:** 20-30 minutes read
**Last Updated:** 2026-02-13

---

## 📖 Overview

This guide establishes coding standards, patterns, and best practices for the KMP Project Template.

**Why follow these patterns?**
- Consistent codebase across all platforms
- Easier code reviews
- Fewer bugs
- Better maintainability
- Automated enforcement (Spotless, Detekt)

---

## 📋 Table of Contents

1. [Project Conventions](#project-conventions)
2. [Code Style](#code-style)
3. [Git Workflow](#git-workflow)
4. [Commit Message Format](#commit-message-format)
5. [Testing Practices](#testing-practices)
6. [Security Guidelines](#security-guidelines)
7. [Kotlin Multiplatform Patterns](#kotlin-multiplatform-patterns)
8. [Platform-Specific Guidelines](#platform-specific-guidelines)
9. [Code Review Guidelines](#code-review-guidelines)

---

## Project Conventions

### Directory Structure

**Rule:** Follow established module structure

```
cmp-shared/src/
├── commonMain/          # ⭐ Shared code (prefer this)
│   ├── kotlin/
│   │   ├── data/        # Data models, DTOs
│   │   ├── domain/      # Business logic, use cases
│   │   ├── network/     # API clients
│   │   ├── database/    # Database (Room, SQL Delight)
│   │   └── util/        # Utilities, extensions
│   └── resources/
├── androidMain/         # Android-specific only when necessary
├── iosMain/            # iOS-specific only when necessary
├── desktopMain/        # Desktop-specific only when necessary
└── jsMain/             # Web-specific only when necessary
```

**Principle:** Maximize `commonMain`, minimize platform-specific code.

---

### Package Naming

**Convention:** `com.yourcompany.app.module`

```kotlin
// Good
package com.yourcompany.app.data.model
package com.yourcompany.app.domain.usecase
package com.yourcompany.app.network.api

// Bad
package data  // Too generic
package MyApp // Wrong case
package com.yourcompany.app.utils // Use 'util' not 'utils'
```

---

### File Naming

**Convention:** `PascalCase` for classes, `camelCase` for files with multiple classes

```kotlin
// Single class per file - match class name
UserRepository.kt        // class UserRepository
LoginViewModel.kt        // class LoginViewModel

// Multiple related items
UserModels.kt           // data class User, data class UserProfile
NetworkExtensions.kt    // fun String.toUrl(), fun Response.isSuccess()

// Constants
Constants.kt            // object Constants
```

---

### Naming Conventions

| Type | Convention | Example |
|------|-----------|---------|
| Class | PascalCase | `UserRepository`, `LoginUseCase` |
| Interface | PascalCase | `UserDataSource`, `AuthService` |
| Object | PascalCase | `Constants`, `AppConfig` |
| Function | camelCase | `fetchUserData()`, `isValidEmail()` |
| Variable | camelCase | `userName`, `userId` |
| Constant | UPPER_SNAKE_CASE | `MAX_RETRY_COUNT`, `API_BASE_URL` |
| Private property | _camelCase (optional) | `_isLoading`, `_userData` |

**Boolean naming:**
```kotlin
// Good
val isEnabled: Boolean
val hasPermission: Boolean
val canEdit: Boolean
fun shouldRetry(): Boolean

// Bad
val enabled: Boolean  // Ambiguous
val permission: Boolean  // Not clear it's a boolean
```

---

## Code Style

### Automatic Formatting

**Tool:** Spotless (enforced in CI)

```bash
# Check formatting
./gradlew spotlessCheck

# Auto-format all code
./gradlew spotlessApply
```

**Pre-commit hook:**
```bash
# Install hook (recommended)
cp .claude/hooks/pre-commit .git/hooks/
chmod +x .git/hooks/pre-commit
```

---

### Kotlin Style Guide

**Follow:** [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)

**Key rules:**

#### 1. Indentation

```kotlin
// 4 spaces (enforced by Spotless)
class MyClass {
    fun myFunction() {
        if (condition) {
            doSomething()
        }
    }
}
```

#### 2. Line Length

```kotlin
// Max 120 characters (enforced by Spotless)
// Good
val message = "This is a reasonably short message"

// Bad (wrap long lines)
val message = "This is a very long message that exceeds the maximum line length and should be wrapped properly"

// Fix: Break into multiple lines
val message = "This is a very long message that exceeds the " +
    "maximum line length and should be wrapped properly"
```

#### 3. Blank Lines

```kotlin
// One blank line between functions
class MyClass {
    fun firstFunction() {
        // ...
    }

    fun secondFunction() {
        // ...
    }
}

// No blank line needed in short blocks
fun simple() {
    val x = 1
    return x * 2
}
```

#### 4. Imports

```kotlin
// Organized automatically by Spotless
// Order: Standard library → Third-party → Project
import kotlin.math.sqrt
import androidx.compose.runtime.*
import com.yourcompany.app.domain.User
```

---

### Function Guidelines

#### Length

```kotlin
// Good: Short, focused functions
fun calculateTotal(items: List<Item>): Double {
    return items.sumOf { it.price }
}

// Bad: Too long (>20 lines) - refactor
fun processOrder(...) {
    // 50 lines of code
}

// Fix: Extract smaller functions
fun processOrder(...) {
    validateOrder()
    calculateTotals()
    applyDiscounts()
    finalizeOrder()
}
```

#### Parameters

```kotlin
// Good: 3 or fewer parameters
fun createUser(name: String, email: String, age: Int)

// Bad: Too many parameters
fun createUser(name: String, email: String, age: Int, phone: String,
               address: String, city: String, zip: String)

// Fix: Use data class
data class UserInfo(
    val name: String,
    val email: String,
    val age: Int,
    val phone: String,
    val address: String,
    val city: String,
    val zip: String
)

fun createUser(info: UserInfo)
```

---

### Nullability

```kotlin
// Prefer non-null types
var name: String = ""  // Good
var name: String? = null  // Only if truly optional

// Use safe calls
val length = name?.length  // Good
val length = name!!.length  // Avoid !! (crashes on null)

// Use elvis operator for defaults
val length = name?.length ?: 0

// Use let for null checks
name?.let { nonNullName ->
    println("Name is $nonNullName")
}
```

---

### Collections

```kotlin
// Prefer immutable
val users: List<User> = listOf(...)  // Good
val users: MutableList<User> = mutableListOf(...)  // Only if needed

// Use collection functions
val adults = users.filter { it.age >= 18 }
val names = users.map { it.name }
val total = items.sumOf { it.price }

// Avoid manual loops
// Bad
val adults = mutableListOf<User>()
for (user in users) {
    if (user.age >= 18) {
        adults.add(user)
    }
}

// Good
val adults = users.filter { it.age >= 18 }
```

---

### String Formatting

```kotlin
// Use string templates
val message = "Hello, $name!"
val full = "User: ${user.name} (${user.age})"

// Avoid concatenation
val message = "Hello, " + name + "!"  // Bad
```

---

### Companion Objects

```kotlin
class MyClass {
    companion object {
        const val TAG = "MyClass"
        const val MAX_RETRIES = 3

        fun create(): MyClass = MyClass()
    }
}

// Usage
val tag = MyClass.TAG
val instance = MyClass.create()
```

---

## Git Workflow

### Branch Strategy

```
main/master    Production releases (protected)
    │
    ├── dev    Active development (default branch)
    │   │
    │   ├── feature/feature-name
    │   ├── bugfix/bug-description
    │   └── chore/maintenance-task
    │
    └── hotfix/critical-fix    Emergency production fixes
```

**Rules:**
- `main`/`master`: Protected, requires PR review
- `dev`: Active development, base for feature branches
- Feature branches: Branch from `dev`, merge back to `dev`
- Hotfix branches: Branch from `main`, merge to both `main` and `dev`

---

### Creating Feature Branches

```bash
# 1. Start from latest dev
git checkout dev
git pull origin dev

# 2. Create feature branch
git checkout -b feature/user-authentication

# 3. Work on feature
git add .
git commit -m "feat(auth): add login screen"

# 4. Push regularly
git push origin feature/user-authentication

# 5. Create PR when ready
gh pr create --base dev --title "feat(auth): add user authentication"
```

**Branch naming:**
```
feature/short-description     # New features
bugfix/issue-description     # Bug fixes
chore/maintenance-task       # Maintenance (dependencies, cleanup)
hotfix/critical-fix          # Production hotfixes
release/v2026.1.0            # Release preparation
```

---

### Pull Request Guidelines

**Title:** Follow conventional commits format

```
feat(auth): add user authentication
fix(android): crash on Android 12
docs(readme): update installation instructions
```

**Description template:**

```markdown
## Summary
Brief description of changes

## Changes
- Added login screen
- Implemented authentication API
- Updated user model

## Testing
- Tested on Android emulator
- Tested on iOS simulator
- All unit tests pass

## Screenshots (if UI changes)
[Attach screenshots]

Closes #123
```

**Before creating PR:**
- [ ] All tests pass
- [ ] Code formatted (`./gradlew spotlessApply`)
- [ ] Static analysis passes (`./gradlew detekt`)
- [ ] No merge conflicts with target branch
- [ ] Branch is up to date with base branch

---

### Code Review Process

**For Authors:**
1. Self-review code before requesting review
2. Respond to feedback promptly
3. Mark resolved comments as "Resolved"
4. Request re-review after changes

**For Reviewers:**
1. Review within 24 hours
2. Be constructive, not critical
3. Focus on logic, security, performance
4. Approve when satisfied, request changes if needed

**Review checklist:**
- [ ] Code follows style guide
- [ ] Logic is correct and efficient
- [ ] Tests are adequate
- [ ] No security vulnerabilities
- [ ] Documentation updated if needed
- [ ] No secrets or sensitive data committed

---

## Commit Message Format

### Conventional Commits

**Format:**
```
<type>(<scope>): <subject>

[optional body]

[optional footer]
```

**Types:**

| Type | Description | Example |
|------|-------------|---------|
| `feat` | New feature | `feat(auth): add biometric login` |
| `fix` | Bug fix | `fix(android): crash on startup` |
| `docs` | Documentation | `docs(readme): update setup guide` |
| `style` | Code style (no logic) | `style: format with spotless` |
| `refactor` | Code refactoring | `refactor(db): simplify query logic` |
| `test` | Adding tests | `test(auth): add login tests` |
| `chore` | Maintenance | `chore: update dependencies` |
| `perf` | Performance | `perf(api): optimize network calls` |
| `ci` | CI/CD changes | `ci: update GitHub Actions` |
| `build` | Build system | `build: update Gradle to 8.5` |
| `revert` | Revert commit | `revert: revert feat(auth)` |

**Scope (optional):** Module or area affected
- `auth`, `profile`, `network`, `database`
- `android`, `ios`, `desktop`, `web`
- `shared`, `api`, `ui`

---

### Commit Message Examples

**Good:**

```bash
feat(auth): add email/password authentication

Implemented login and registration screens with form validation.
Uses Firebase Authentication for backend.

Closes #123
```

```bash
fix(android): resolve crash on Android 12

Fixed SecurityException when accessing camera on Android 12+
by requesting runtime permission properly.

Fixes #456
```

```bash
docs(deployment): add iOS deployment guide

Created comprehensive guide for deploying iOS app to TestFlight
and App Store, including code signing setup.
```

**Bad:**

```bash
update stuff  # No type, vague subject
```

```bash
fix bug  # No details, which bug?
```

```bash
add new feature for users  # Too vague
```

---

### Commit Frequency

**Good practice:**
- Commit often, push regularly
- Each commit should be a logical unit
- Can always squash before merging

```bash
# Good: Small, focused commits
git commit -m "feat(auth): add login screen UI"
git commit -m "feat(auth): add login validation"
git commit -m "feat(auth): integrate login API"

# Bad: One massive commit
git commit -m "add authentication" # 50 files changed
```

---

## Testing Practices

### Test Coverage Goals

| Type | Target Coverage | Priority |
|------|----------------|----------|
| Unit Tests | 80%+ | High |
| Integration Tests | 60%+ | Medium |
| E2E Tests | Critical paths | High |

---

### Testing Strategy

**Test Pyramid:**

```
        /\
       /  \      E2E Tests (Few, slow, expensive)
      /____\
     /      \    Integration Tests (Moderate)
    /________\
   /          \  Unit Tests (Many, fast, cheap)
  /__________\
```

**Focus:** Write more unit tests, fewer E2E tests

---

### Unit Testing

**Location:** `src/commonTest/kotlin/`

**Example:**

```kotlin
// src/commonMain/kotlin/domain/EmailValidator.kt
class EmailValidator {
    fun isValid(email: String): Boolean {
        return email.contains("@") && email.contains(".")
    }
}

// src/commonTest/kotlin/domain/EmailValidatorTest.kt
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EmailValidatorTest {
    private val validator = EmailValidator()

    @Test
    fun `valid email returns true`() {
        assertTrue(validator.isValid("test@example.com"))
    }

    @Test
    fun `email without @ returns false`() {
        assertFalse(validator.isValid("testexample.com"))
    }

    @Test
    fun `email without dot returns false`() {
        assertFalse(validator.isValid("test@examplecom"))
    }

    @Test
    fun `empty email returns false`() {
        assertFalse(validator.isValid(""))
    }
}
```

**Naming:** Use descriptive test names with backticks

```kotlin
// Good
@Test
fun `login with valid credentials succeeds`()

// Bad
@Test
fun testLogin()
```

---

### Testing Best Practices

**AAA Pattern:** Arrange, Act, Assert

```kotlin
@Test
fun `calculate total returns sum of item prices`() {
    // Arrange
    val items = listOf(
        Item("A", 10.0),
        Item("B", 20.0),
        Item("C", 30.0)
    )
    val calculator = PriceCalculator()

    // Act
    val total = calculator.calculateTotal(items)

    // Assert
    assertEquals(60.0, total)
}
```

**Test isolation:** Each test should be independent

```kotlin
// Good
class UserRepositoryTest {
    @Test
    fun `test 1`() {
        val repo = UserRepository()  // Fresh instance
        // test
    }

    @Test
    fun `test 2`() {
        val repo = UserRepository()  // Fresh instance
        // test
    }
}

// Bad
class UserRepositoryTest {
    private val repo = UserRepository()  // Shared instance

    @Test
    fun `test 1`() {
        repo.addUser(...)  // Modifies shared state
    }

    @Test
    fun `test 2`() {
        // Test may fail due to state from test 1
    }
}
```

---

### Running Tests

```bash
# Run all tests
./gradlew test

# Run specific module tests
./gradlew :cmp-shared:test

# Run specific test class
./gradlew :cmp-shared:test --tests EmailValidatorTest

# Run with coverage
./gradlew :cmp-shared:koverReport
open cmp-shared/build/reports/kover/html/index.html
```

---

## Security Guidelines

### Secrets Management

**❌ NEVER commit:**
- API keys
- Passwords
- Keystores
- Certificate files
- Firebase credentials
- OAuth tokens
- Private keys

**✅ Always use:**
- Environment variables
- Secret files in `secrets/` directory (gitignored)
- GitHub Actions secrets for CI/CD

---

### Checking for Secrets

```bash
# Before committing
git status
git diff

# Check for common secret patterns
git diff | grep -i "password\|api_key\|secret\|token"

# Use pre-commit hook (recommended)
# .git/hooks/pre-commit checks for secrets automatically
```

---

### Secure Coding Practices

#### 1. Input Validation

```kotlin
// Always validate user input
fun processEmail(email: String): Result {
    if (email.isBlank()) {
        return Result.Error("Email cannot be empty")
    }
    if (!isValidEmail(email)) {
        return Result.Error("Invalid email format")
    }
    // Process email
}

// Avoid:
fun processEmail(email: String) {
    // No validation - security risk
    sendEmail(email)
}
```

#### 2. SQL Injection Prevention

```kotlin
// Good: Use parameterized queries
fun getUserById(id: Int): User? {
    return database.query("SELECT * FROM users WHERE id = ?", id)
}

// Bad: String concatenation - SQL injection risk
fun getUserById(id: String): User? {
    return database.query("SELECT * FROM users WHERE id = $id")
}
```

#### 3. Network Security

```kotlin
// Always use HTTPS
val client = HttpClient {
    install(HttpsRedirect)
}

// Validate SSL certificates
// Don't disable SSL verification in production
```

#### 4. Data Storage

```kotlin
// Encrypt sensitive data
val encrypted = encryptionManager.encrypt(sensitiveData)
storage.save(encrypted)

// Don't store plain passwords
val hashedPassword = hashPassword(password)  // Good
storage.savePassword(password)  // Bad
```

---

### Dependency Security

```bash
# Check for known vulnerabilities
./gradlew dependencyCheckAnalyze

# Keep dependencies up to date
./gradlew dependencyUpdates
```

---

## Kotlin Multiplatform Patterns

### Expect/Actual Pattern

**Use for:** Platform-specific implementations

```kotlin
// commonMain
expect class Platform() {
    val name: String
}

expect fun currentTimeMillis(): Long

// androidMain
actual class Platform {
    actual val name: String = "Android"
}

actual fun currentTimeMillis(): Long = System.currentTimeMillis()

// iosMain
actual class Platform {
    actual val name: String = "iOS"
}

actual fun currentTimeMillis(): Long = NSDate().timeIntervalSince1970.toLong()
```

**When to use:**
- Platform-specific APIs (camera, location, etc.)
- Different implementations required per platform
- Cannot be abstracted in common code

**When NOT to use:**
- Logic that can be shared - use commonMain instead
- Trivial differences - consider using interfaces

---

### Shared Business Logic

**Maximize commonMain:**

```kotlin
// commonMain/kotlin/domain/LoginUseCase.kt
class LoginUseCase(
    private val repository: UserRepository
) {
    suspend fun login(email: String, password: String): Result<User> {
        // Validate
        if (!isValidEmail(email)) {
            return Result.Error("Invalid email")
        }

        // Call repository
        return repository.login(email, password)
    }

    private fun isValidEmail(email: String): Boolean {
        return email.contains("@") && email.contains(".")
    }
}
```

**Platform-specific UI:**

```kotlin
// androidMain - Compose UI
@Composable
fun LoginScreen(viewModel: LoginViewModel) {
    // Android UI with Compose
}

// iosMain - SwiftUI (in Swift)
struct LoginScreen: View {
    @StateObject var viewModel: LoginViewModel

    var body: some View {
        // iOS UI with SwiftUI
    }
}
```

---

## Platform-Specific Guidelines

### Android

**Use Jetpack Compose:**
```kotlin
@Composable
fun UserProfile(user: User) {
    Column {
        Text(user.name)
        Text(user.email)
    }
}
```

**Follow Material Design 3**

**Minimum SDK:** 24 (Android 7.0)

---

### iOS

**Use SwiftUI for UI:**
```swift
struct UserProfile: View {
    let user: User

    var body: some View {
        VStack {
            Text(user.name)
            Text(user.email)
        }
    }
}
```

**Minimum iOS:** 15.0+

---

### Desktop

**Use Compose Multiplatform:**
```kotlin
@Composable
fun App() {
    MaterialTheme {
        // Desktop UI
    }
}
```

---

### Web

**Use Compose for Web:**
```kotlin
fun main() {
    renderComposable(rootElementId = "root") {
        // Web UI
    }
}
```

---

## Code Review Guidelines

### What to Look For

**Functionality:**
- Does the code do what it's supposed to?
- Are edge cases handled?
- Is error handling adequate?

**Code Quality:**
- Is the code readable?
- Are names descriptive?
- Is complexity appropriate?
- Are there code smells?

**Tests:**
- Are there tests?
- Do tests cover edge cases?
- Are tests clear and maintainable?

**Security:**
- No secrets committed?
- Input validation present?
- No SQL injection risks?

**Performance:**
- Are there performance concerns?
- Could it be optimized?
- Are large operations asynchronous?

---

### Review Comments

**Good comments:**

```
Consider using `filter` instead of a manual loop here for clarity:
val adults = users.filter { it.age >= 18 }
```

```
This could potentially cause a crash if `user` is null.
Consider using safe call: `user?.name`
```

**Bad comments:**

```
This is wrong, fix it.  # Not constructive
```

```
I would have done this differently.  # Not actionable
```

---

## Tools

### Enforced by CI

- **Spotless:** Code formatting
- **Detekt:** Static analysis
- **Dependency Guard:** Dependency changes
- **Unit Tests:** Test execution

### Run Locally

```bash
# Format code
./gradlew spotlessApply

# Check code quality
./gradlew detekt

# Run tests
./gradlew test

# Check dependencies
./gradlew dependencyGuard
```

---

## Resources

- **Kotlin Style Guide:** https://kotlinlang.org/docs/coding-conventions.html
- **Conventional Commits:** https://www.conventionalcommits.org/
- **Kotlin Multiplatform:** https://kotlinlang.org/docs/multiplatform.html
- **Compose Multiplatform:** https://www.jetbrains.com/lp/compose-multiplatform/

---

**Last Updated:** 2026-02-13
**Maintainer:** See CLAUDE.md
