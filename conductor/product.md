# Initial Concept

projeto @gestao-paroquia-ui será um app web para permitir que os usuários interajam com a api exposta pelo projeto @gestao-paroquia. Precisa ser um sistema web com react. Deve prover interfaces para interagir com as funcionalidades expostas pela api @gestao-paroquia observando regras de permissionamento, fluxos de aprovação e demais funcionalidades implementadas.

## Target Audience
- **Admin/Managers:** Parish administrators who manage overall settings and users.
- **Secretariat/Staff:** Users handling daily operations, registrations, and documents.
- **Parishioners/Members:** End-users requesting services and viewing schedules.

## Core Features
- **Role-based Dashboard:** Personalized views based on user permissions and roles.
- **Reports & Metrics Module:** Comprehensive overview of parish members, leadership indicators, and activity volume distribution.
- **Design System & Component Standardization:** Global UI design tokens (Marian Blue/Gold palette, 0px border-radius, right-aligned form actions, unified NotificationContext alerts), plus a semantic action→color→icon mapping applied to every action `Button`/`IconButton` (e.g. Salvar=primary+Save, Cancelar=secondary+Close, Excluir=error+Delete, Aprovar=success+CheckCircle).
- **Batch Data Importation & Template Downloads:** Support for bulk uploading Membros, Usuários, Organizações, and Eventos via CSV and Excel (.xlsx) files, line-by-line validation preview, and sample template downloads.
- **Approval Workflows UI:** Interfaces to review, approve, or reject pending requests.
- **Management Modules:** CRUD interfaces for managing events, members, and resources.
- **Patrimônio (Asset Management):** Registration of parish assets, allocation/reservation per event, transfer between organizations, and maintenance tracking with ociosidade (idleness) metrics.
- **Conselho (CPC Governance):** Registration and homologation of Conselho Paroquial para Assuntos Econômicos (CPC) decisions, with historical tracking of opinions and approvals.

## Goals & Objectives
- **Process Automation:** Streamline administrative tasks and reduce manual paperwork.
- **Transparency & Access:** Improve visibility of parish activities and status of requests.
- **Centralized Management:** Provide a single source of truth for all parish data.

## UX & Device Strategy
- **Desktop First:** The user interface will be optimized for administrative tasks typically performed on desktop computers, ensuring high density and efficiency for complex operations.
