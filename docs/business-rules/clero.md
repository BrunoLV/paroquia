# Regras de Negócio e Fluxos: Módulo Clero e Governança Eclesiástica (`clero`)

O módulo de Clero gerencia as autoridades eclesiásticas, ordenações, provisões canônicas, ofícios paroquiais e a vinculação diocesana da Paróquia.

---

## 1. Visão Geral e Propósito
Assegurar o registro formal do clero paroquial e diocesano, controlando a validade jurídica das provisões de ofício (Pároco, Vigários, Diáconos e Bispo Diocesano) em estrita consonância com o Direito Canônico.

---

## 2. Agregados, Entidades e Value Objects Principais
- **`Clerigo` (Raiz de Agregado):** Identificado por `ClerigoId`. Contém vínculo com pessoa física (`FielId`), nome religioso/título, grau de ordem (`DIACONO`, `PRESBITERO`, `BISPO`), data e local de ordenação, bispo ordenante e diocese de incardinação.
- **`ProvisaoCanonica`:** Ato jurídico emitido pelo Bispo Diocesano nomeando o clérigo para um ofício paroquial (`PAROCO`, `ADMINISTRADOR_PAROQUIAL`, `VIGARIO_PAROQUIAL`, `DIACONO_COLABORADOR`), com data de posse, livro/folha da cúria e período de vigência.
- **`OficioEclesiastico` (Enum):** Graus e funções de governo canônico.
- **`Diocese`:** Circunscrição eclesiástica territorial a que a paróquia pertence.

---

## 3. Regras de Negócio Invariantes

### RN-CLE-01: Exclusividade de Pároco em Exercício
- Uma paróquia só pode ter **exatamente um Pároco** (ou Administrador Paroquial) com provisão canônica ativa e vigente ao mesmo tempo. Não pode haver simultaneidade de párocos titulares na mesma paróquia.

### RN-CLE-02: Vigários e Diáconos Subordinados
- A paróquia pode possuir um ou mais Vigários Paroquiais e Diáconos, os quais atuam canonicamente como cooperadores do Pároco.

### RN-CLE-03: Vigência de Provisão Canônica
- Se uma provisão canônica possuir data limite expirada sem termo de prorrogação ou renovação, o sistema emite alerta preventivo de governança eclesiástica.

### RN-CLE-04: Paridade de Proteção de Dados (LGPD)
- Aplicação das mesmas restrições de privacidade: os dados civis e de saúde de padres e diáconos possuem a mesma blindagem dispensada aos fiéis leigos.

---

## 4. Fluxos Operacionais e Casos de Uso
1. **Posse de Novo Pároco:** Registro da provisão diocesana, data da missa de posse e transição automática do perfil no sistema.
2. **Designação de Vigários:** Associação de padres colaboradores às comunidades e atividades de confissão/missa.
3. **Consulta de Jurisdição:** Informações canônicas para constar em certidões de batismo, matrimônio e atas oficiais.

---

## 5. Integrações com outros Bounded Contexts
- **`fiel`:** Associação ao registro da pessoa física.
- **`iam`:** Atribuição dos papéis de segurança e permissões de sistema (`PAROCO`, `VIGARIO`, `DIACONO`).
- **`calendario`:** Designação do celebrante principal nas missas e solenidades.
