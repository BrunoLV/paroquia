# Specification: Performance & Caching Optimization

## Goal
Improve the responsiveness of the API by implementing a caching layer for frequently accessed and computationally expensive data, specifically targeting project summaries and annual parochial year states.

## Requirements
- Enable Spring Caching abstraction.
- Use Caffeine as the local in-memory cache provider.
- Implement caching for `ProjetoAgregacaoService.getProjetoResumo`.
- Implement caching for `AnoParoquialController` list/get operations.
- Ensure cache eviction (TTL or manual) when data is updated.

## Success Criteria
- Cache is active and used for repeated requests.
- Cache is invalidated when a project or event within a project is updated.
- Cache is invalidated when an annual parochial year status changes.
