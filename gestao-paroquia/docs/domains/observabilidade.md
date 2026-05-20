# Domain: Observabilidade

The **Observabilidade** domain is responsible for tracking system health, auditing operations, and providing metrics for administrative decision-making.

## Core Concepts

### Auditoria (Auditing)
Every significant state-changing operation in the system is recorded. This provides a clear trail of "who did what and when".

### Métricas (Metrics)
Consolidates data to provide insights, such as:
- **Event Rework Rate**: Frequency of event changes.
- **Extra Events Rate**: Number of events added after the calendar was locked.
- **Participation Levels**: Engagement metrics across different pastorals.

## Metrics and Analytics

The application provides specialized use cases for administrative insights:
- **GetIndicadorRetrabalhoUseCase**: Calculates the rate of changes to already confirmed events, identifying potential planning issues.
- **GetTaxaEventosExtraUseCase**: Measures the volume of events added after the official calendar was locked.
- **ListAuditTrailUseCase**: Provides a searchable history of all critical operations for transparency and accountability.

## Key Components

- **AuditoriaOperacaoEntity**: Records individual operations and their outcomes.
- **JobLockEntity**: Manages synchronization for background tasks (e.g., metric aggregation).

## Integration

- Cross-cuts all domains to ensure accountability and provide a data-driven view of parish activities.
