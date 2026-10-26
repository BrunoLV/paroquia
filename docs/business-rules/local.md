# Regras de Negócio e Fluxos: Módulo Locais e Espaços Físicos (`local`)

O módulo de Locais gerencia a infraestrutura predial da paróquia, incluindo a Igreja Matriz, salões paroquiais, salas de catequese, capelas e centros comunitários.

---

## 1. Visão Geral e Propósito
Assegurar o mapeamento cadastral, capacidade de público, manutenção e disponibilidade operacional de todos os espaços físicos paroquiais sob jurisdição da paróquia.

---

## 2. Agregados, Entidades e Value Objects Principais
- **`Local` (Raiz de Agregado):** identificado por UUID. Armazena nome, tipo, endereço (`EnderecoLocal`), descrição, capacidade máxima de pessoas, a organização (igreja) a que pertence e o status ativo. O ciclo de vida admite **exclusão lógica**.
- **Tipo do local:** texto de até 32 caracteres, escolhido na interface entre `SALAO_PAROQUIAL`, `IGREJA`, `CAPELA`, `SALA_REUNIAO`, `AUDITORIO`, `QUADRA`, `SECRETARIA` e `OUTRO`.
- **`EnderecoLocal` (Value Object):** endereço do espaço, imutável.

---

## 3. Regras de Negócio Invariantes

### RN-LOC-01: Capacidade Positiva
- A capacidade máxima, quando informada, é um número positivo.

### RN-LOC-02: Inativação e Exclusão com Eventos Futuros
- Um local com **eventos futuros agendados** não pode ser inativado nem excluído: os eventos precisam ser remanejados antes. A exclusão é sempre lógica.

### RN-LOC-03: Vínculo com a Igreja
- Todo local pertence a uma organização (igreja ou comunidade) da paróquia, e a listagem pode ser restrita a ela.

### RN-LOC-04: Alçada de Cadastro
- Cadastram, alteram e excluem locais o administrador, o Pároco, o Bispo Diocesano, a secretaria paroquial, o secretário de conselho e o membro do conselho pastoral. Qualquer usuário autenticado consulta.

---

## 4. Fluxos Operacionais e Casos de Uso
1. **Cadastro e Manutenção de Espaços:** cadastro do local com tipo, endereço, capacidade e descrição.
2. **Consulta:** listagem e busca de locais para o calendário e as reservas.
3. **Inativação:** o local sai de uso, desde que não tenha eventos futuros.

---

## 5. Integrações com outros Bounded Contexts
- **`calendario`:** eventos alocados ao local; a inativação e a exclusão consultam os eventos futuros.
- **`reserva`:** reservas do local como dependência física (ver `reserva.md`).
- **`organizacao`:** a organização a que o local pertence.
