# Implementation Plan: Clean Imports & Remove Inline Fully Qualified Names in gestao-paroquia

## Phase 1: Scan & Find Inline FQNs

- [x] Task: Scan Java Source Files for Inline FQNs ad12f3b
    - [x] Scan the `gestao-paroquia` Java source files (under `app/src/main` and `app/src/test`) for inline fully qualified package names (such as references starting with `br.com.nsfatima.`).
    - [x] Document all identified locations with file path, line number, and FQN details.
- [~] Task: Conductor - User Manual Verification 'Phase 1: Scan & Find Inline FQNs' (Protocol in workflow.md)

## Phase 2: Refactoring & Verification

- [ ] Task: Replace Inline FQNs with Simple Names and Imports
    - [ ] Replace identified inline FQNs with their corresponding simple names.
    - [ ] Add appropriate `import` statements at the top of the Java files.
    - [ ] Resolve any namespace collisions if two identical simple names from different packages are used in the same file.
- [ ] Task: Execute Build and Run Automated Tests
    - [ ] Run `./gradlew compileJava compileTestJava` to ensure compilation is successful.
    - [ ] Run `./gradlew test` to verify all 325 tests pass.
- [ ] Task: Conductor - User Manual Verification 'Phase 2: Refactoring & Verification' (Protocol in workflow.md)
