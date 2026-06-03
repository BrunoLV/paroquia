# Specification: Clean Imports & Remove Inline Fully Qualified Names in gestao-paroquia

## Overview
This track focuses on cleaning up code style and formatting across the `gestao-paroquia` backend API. The objective is to replace inline fully qualified names (FQNs) of classes, interfaces, and annotations with standard Java imports at the top of the source files, complying with clean coding practices and improving readability.

## Scope & Core Objectives
1. **FQN Identification**: Scan the entire Java codebase of the `gestao-paroquia` project for inline fully qualified names.
2. **Refactoring**: Replace inline FQNs with simple names and introduce corresponding `import` statements.
3. **Collision Handling**: Only preserve inline FQNs when necessary to resolve conflict collisions between classes of the same simple name imported/used in the same Java file.
4. **Verification**: Compile the code and run the test suite to ensure that the code is behaviorally unchanged and correct.

## Acceptance Criteria
- No unnecessary inline fully qualified names are present in the Java source code of `gestao-paroquia`.
- Code compiles successfully.
- All automated tests pass successfully (`./gradlew test`).

## Out of Scope
- Formatting or editing `gestao-paroquia-ui` files.
- Refactoring business logic or other unrelated code smells.
