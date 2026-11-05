# Code Style Guide

This document outlines the coding standards and best practices for the KMP Multi-Module Project.
Following these guidelines ensures consistency, readability, and maintainability across the
codebase.

## Table of Contents

- [General Principles](#general-principles)
- [Kotlin Coding Conventions](#kotlin-coding-conventions)
- [Multiplatform-Specific Guidelines](#multiplatform-specific-guidelines)
- [Documentation Requirements](#documentation-requirements)
- [Architecture Guidelines](#architecture-guidelines)
- [Testing Standards](#testing-standards)
- [Enforcement and Tools](#enforcement-and-tools)

## General Principles

### Code Clarity

Write code that is easy to understand:

- Optimize for readability rather than cleverness
- Prefer explicit over implicit
- Keep functions and classes focused on a single responsibility
- Avoid deep nesting of code blocks

### Consistency

Maintain consistent patterns across the codebase:

- Follow established conventions for module structure
- Use similar solutions for similar problems
- Be consistent with formatting, naming, and organization

### Performance Considerations

Balance readability with performance:

- Profile before optimizing
- Document performance-critical sections
- Consider all platforms when making performance decisions

## Kotlin Coding Conventions

### Naming Conventions

#### Files

- Match file names to the top-level class: `UserRepository.kt` for class `UserRepository`
- Use camel case with an uppercase first letter: `NetworkClient.kt`
- Extension functions: Use the format `TypeExtensions.kt`: `StringExtensions.kt`

#### Classes and Interfaces

- Use PascalCase: `UserRepository`, `NetworkClient`
- Suffix interfaces with their role when appropriate: `UserRepository`, not `IUserRepository`
- Use descriptive and concise names that reflect the responsibility

#### Functions

- Use camelCase: `getUserProfile()`, `calculateTotal()`
- Prefer verb phrases for action functions: `fetchData()` not `data()`
- Boolean functions should be phrased as questions: `isValid()`, `hasAccess()`

#### Variables

- Use camelCase: `userName`, `isEnabled`
- Avoid single-letter names except for loop indices or mathematical formulas
- Constants and immutable top-level properties: Use SCREAMING_SNAKE_CASE: `MAX_COUNT`, `API_KEY`

#### Packages

- All lowercase, with meaningful names: `org.example.feature.profile`
- Avoid plural forms: `org.example.model` not `org.example.models`

### Formatting

The project enforces formatting with Spotless and the official Kotlin style guide:

- 4 spaces for indentation (no tabs)
- Maximum line length of 120 characters
- Use trailing commas in parameter/argument lists for better diffs

### Code Organization

#### Top-level Declarations

- Organize top-level declarations in the following order:
    1. Package statement
    2. Import statements (alphabetical order, no wildcards)
    3. Top-level declarations

#### Class Structure

Organize class members in the following order:

1. Properties
    - Constants
    - Immutable properties
    - Mutable properties
2. Secondary constructors
3. Factory methods/companion object members
4. Public methods
5. Internal methods
6. Private methods
7. Inner classes and interfaces
8. Companion object

### Language Features

#### Null Safety

- Avoid nullable types when possible
- Use the Elvis operator (`?:`) for default values
- Prefer safe calls (`?.`) over null checks where appropriate
- Always handle potential null values explicitly

#### Extension Functions

- Use extension functions to enhance existing classes
- Keep extensions focused and single-purpose
- Place related extensions in a dedicated `TypeExtensions.kt` file

#### Scope Functions

Use appropriate scope functions:

- `let`: For executing code block with non-null values
- `run`: For executing code block and returning result
- `with`: For calling multiple methods on the same object
- `apply`: For configuring objects (returns the object)
- `also`: For additional actions that don't change the object (returns the object)

#### Coroutines

- Prefer structured concurrency patterns
- Always define and respect the CoroutineScope
- Use the appropriate dispatchers for the work being done
- Handle exceptions properly with try-catch or supervisorScope

## Multiplatform-Specific Guidelines

### Code Sharing

- Place platform-agnostic code in the `common` source set
- Use intermediate source sets (e.g., `jvmCommon`, `jsCommon`) for platform group-specific code
- Move code to the most general source set that can support it

### Expect/Actual

- Use `expect`/`actual` declarations for platform-specific implementations
- Keep `expect` declarations minimal and focused
- Provide complete documentation on `expect` declarations
- Ensure all `actual` implementations maintain the contract specified by the `expect` declaration

### Platform-Specific Code

- Isolate platform-specific code in appropriate source sets
- Use clear naming to indicate platform specificity: `AndroidNetworkClient`, `IOSNetworkClient`
- Minimize platform branching in common code

## Documentation Requirements

### Code Comments

- Use KDoc format for documentation comments
- Document all public APIs
- Include usage examples for complex or non-obvious functionality
- Explain the "why" rather than the "what" when the code is not self-explanatory

### KDoc Requirements

- All public classes, interfaces, and functions must have KDoc comments
- Include `@param`, `@return`, and `@throws` tags where applicable
- Document expected threading/coroutine usage where relevant

Example KDoc:

```kotlin
/**
 * Fetches user data from the remote API.
 *
 * This method handles authentication and caching internally.
 * If the network is unavailable, it will return cached data if available.
 *
 * @param userId The unique identifier of the user
 * @param forceRefresh Whether to bypass cache and force a network request
 * @return A [Flow] emitting the [User] data
 * @throws [NetworkException] if the network request fails and no cache is available
 */
fun fetchUser(userId: String, forceRefresh: Boolean = false): Flow<User>
```

### README Files

- Each module should have a README.md file explaining:
    - The purpose of the module
    - Key components and their responsibilities
    - How to use the module
    - Dependencies and relationships to other modules

## Architecture Guidelines

### Clean Architecture

- Respect layer boundaries:
    - Domain layer should not depend on data or presentation layers
    - Data layer should not depend on presentation layer
- Use models appropriate to each layer; don't leak implementation details

### Feature Modules

- Each feature module should be self-contained
- Minimize dependencies between feature modules
- Use interfaces for cross-feature communication

### Dependency Injection

- Use constructor injection where possible
- Configure Koin modules in a consistent way
- Scope dependencies appropriately

## Testing Standards

### Unit Tests

- Test all business logic in isolation
- Use descriptive test method names: `when_invalid_credentials_given_then_return_error()`
- Organize tests using the Given-When-Then pattern
- Use appropriate mocking strategies and fakes

### UI Testing

- Test key user flows
- Focus on user-visible behavior
- Use screenshot testing where appropriate

### Test Coverage

- Aim for high coverage of business logic (domain layer)
- Test all edge cases and error conditions
- Include tests for both success and failure scenarios

## Enforcement and Tools

### Automated Checks

The project uses several tools to enforce code quality:

- **Detekt**: Static code analysis
- **Spotless**: Code formatting
- **Git Hooks**: Pre-commit checks
- **GitHub Actions**: CI/CD enforcement

### Detekt Configuration

Detekt is configured with custom rules to enforce project-specific guidelines. Key areas include:

- Complexity metrics (cyclomatic complexity, LOC)
- Potential bugs
- Performance issues
- Style violations

### Spotless Configuration

Spotless enforces formatting using the ktlint ruleset with project-specific adjustments.

### Code Review Guidelines

During code reviews, pay special attention to:

- Adherence to the architecture and layer separation
- Consistency with existing code
- Test coverage and quality
- Documentation completeness
- Performance considerations

## Continuous Improvement

This style guide is a living document. If you identify patterns or practices that should be
standardized, propose changes through:

1. Opening an issue with the "style-guide" label
2. Discussing with the team
3. Submitting a PR to update this document

## Additional Resources

- [Official Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- [Kotlin Multiplatform Documentation](https://kotlinlang.org/docs/multiplatform.html)
- [Clean Architecture by Robert C. Martin](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)