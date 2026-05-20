# Domain: Projeto

The **Projeto** domain allows grouping related events into strategic initiatives or specific campaigns (e.g., "Feira da Partilha 2026", "Reforma da Matriz").

## Core Concepts

### Projeto (Project)
A project is a temporal container for events and actions. It has a name, description, responsible organization, and a defined time period (start/end).

### Status de Projeto
Projects can be tracked to monitor their progress and overall health.

## Use Cases

- **CreateProjetoUseCase**: Define a new project and its boundaries.
- **UpdateProjetoUseCase**: Modify project details.
- **ListProjetosUseCase**: Lists projects with filtering capabilities.
- **ProjetoAgregacaoService**: A specialized service that consolidates data from associated events to provide a high-level view of project health, collaboration maps, and progress metrics.

## Key Features

- **Event Association**: Events in the **Calendário** domain can be linked to a project.
- **Project Visibility**: Provides a way to see all activities related to a specific parish initiative across different pastorals.
