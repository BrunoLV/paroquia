# Code Documentation Update Plan

## Objective
Update the Javadoc for all Application Use Cases and API Controllers across the project to ensure they are fully documented according to project guidelines. The documentation must accurately reflect the implemented business logic and align with the newly created Docsify documentation.

## Scope & Impact
- **Controllers**: 14 files across all domains (`api/v1/controller/*`).
- **Use Cases**: 41 files across all domains (`application/usecase/*`).
- **Docsify**: Verify and update `docs/domains/*.md` if any discrepancies are found during the code review.

## Project Guidelines for Documentation
- Docstrings on public functions: intent + one usage example.
- Write WHY, not WHAT. Skip trivial comments like `// increment counter`.

## Proposed Solution
Due to the large number of files (55+), the implementation will be done by delegating the documentation updates to the `generalist` sub-agent, grouped by domains. This ensures efficiency and prevents context overload.

## Implementation Steps
1. **Domain 1 (Calendário & Aprovação)**: Invoke `generalist` to update Javadocs for Controllers and UseCases in these domains.
2. **Domain 2 (Membro, Organizacao, IAM)**: Invoke `generalist` to update Javadocs for Controllers and UseCases in these domains.
3. **Domain 3 (Projeto, Local, Governanca, Observabilidade)**: Invoke `generalist` to update Javadocs for Controllers and UseCases in these domains.
4. **Docsify Review**: Verify if any Docsify `.md` files need adjustments based on the updated code comments.

## Verification
- Run `./gradlew compileJava` to ensure no syntax errors were introduced in the comments.
- Spot-check random files to ensure the Javadocs follow the "intent + one usage example" rule.