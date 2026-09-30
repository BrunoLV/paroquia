export interface Organization {
  id: string;
  nome: string;
  tipo: string;
  contato: string | null;
  coordenador?: string | null;
  viceCoordenador?: string | null;
  ativo: boolean;
}

export interface User {
  id: string;
  username: string;
  name: string;
  roles: string[];
  organizations?: Organization[];
}

// ─── Eclesial Role Predicates (14 Papéis Canônicos Ortogonais) ───────────────

const PAROCO_ROLES = ['PAROCO', 'BISPO_DIOCESANO'] as const;
const CPP_ROLES = ['MEMBRO_CONSELHO_PASTORAL', 'SECRETARIO_CONSELHO'] as const;
const CAE_ROLES = ['MEMBRO_CONSELHO_ECONOMICO'] as const;
const CLERO_ROLES = ['VIGARIO', 'DIACONO_PAROQUIAL'] as const;
const SECRETARIO_ROLES = ['SECRETARIA_PAROQUIAL', 'SECRETARIO_CONSELHO', 'SECRETARIO_GRUPO'] as const;
const COORDENADOR_ROLES = ['COORDENADOR'] as const;
const MEMBRO_ROLES = ['MEMBRO_ATIVO', 'VOLUNTARIO', 'FIEL'] as const;

const hasRole = (user: User | null, ...roles: string[]): boolean => {
  if (!user) return false;
  return user.roles.some((r) =>
    roles.some((allowed) =>
      r === allowed ||
      r === `ROLE_${allowed}` ||
      r === `GLOBAL_${allowed}` ||
      r === `ROLE_GLOBAL_${allowed}` ||
      r.endsWith(`_${allowed}`)
    )
  );
};

// ─── Public Permission Functions ─────────────────────────────────────────────

/**
 * Administrador técnico de infraestrutura e IAM (`ADMIN`).
 * Isolado de decisões eclesiais (não homologa atas, não fecha ano paroquial, não aprova eventos).
 */
export const isAdmin = (user: User | null): boolean => hasRole(user, 'ADMIN');

/**
 * Papéis administrativos responsáveis pelo ciclo de vida do cadastro civil de fiéis.
 * Coordenadores mantêm somente a consulta restrita à sua própria organização.
 */
export const canManageCadastroFieis = (user: User | null): boolean => {
  if (!user) return false;
  return hasRole(user, 'ADMIN', 'PAROCO', 'BISPO_DIOCESANO', 'SECRETARIA_PAROQUIAL');
};

/**
 * Papéis com permissão para gerir o cadastro canônico e pessoal do clero.
 */
export const canManageClero = (user: User | null): boolean => {
  if (!user) return false;
  return hasRole(user, 'ADMIN', 'PAROCO', 'BISPO_DIOCESANO', 'SECRETARIA_PAROQUIAL');
};

/** Pároco (ou Administrador Paroquial) e Bispo Diocesano: autoridade executiva e homologação final (Cân. 515 §1, 536 §2) */
export const isParoco = (user: User | null): boolean => hasRole(user, ...PAROCO_ROLES);

/** Secretário de Conselho (CPP/CPC): pauta reuniões e redige minutas de atas, sem voto deliberativo */
export const isSecretarioConselho = (user: User | null): boolean => hasRole(user, 'SECRETARIO_CONSELHO');

/** Membro do Conselho Pastoral (CPP/CPC): voto estritamente consultivo */
export const isMembroConselhoPastoral = (user: User | null): boolean => hasRole(user, 'MEMBRO_CONSELHO_PASTORAL');

/** Membro do Conselho Econômico (CAE/CAEP): parecer financeiro e auditoria patrimonial */
export const isMembroConselhoEconomico = (user: User | null): boolean => hasRole(user, ...CAE_ROLES);

/** Membro ou Secretário do Conselho Pastoral (CPP/CPC) — voto estritamente consultivo */
export const isMembroCpp = (user: User | null): boolean => hasRole(user, ...CPP_ROLES);

/** Membro do Conselho Econômico (CAE/CAEP) — voto consultivo e parecer patrimonial */
export const isMembroCae = (user: User | null): boolean => isMembroConselhoEconomico(user);

/** Clero paroquial cooperador (Vigário Paroquial, Diácono Paroquial) */
export const isClero = (user: User | null): boolean => hasRole(user, ...CLERO_ROLES);

/** Secretários (Secretaria Paroquial, Secretário de Conselho, Secretário de Grupo) */
export const isSecretario = (user: User | null): boolean => hasRole(user, ...SECRETARIO_ROLES);

/** Coordenador de Pastoral ou Movimento (escopo territorial resolvido via ABAC da organização vinculada) */
export const isCoordenador = (user: User | null): boolean => hasRole(user, ...COORDENADOR_ROLES);

/**
 * ABAC de Coordenador: verifica se o coordenador possui vínculo com organização de nível paroquial/matriz (global)
 * ou se atua no escopo de uma comunidade/organização específica (local).
 */
export const hasCoordenadorScope = (user: User | null, targetOrgId?: string): boolean => {
  if (!user || !isCoordenador(user)) return false;
  if (!Array.isArray(user.organizations) || user.organizations.length === 0) return false;
  const hasParoquialScope = user.organizations.some(
    (org) => org.tipo === 'MATRIZ' || org.tipo === 'PAROQUIA' || org.tipo === 'CONSELHO'
  );
  if (hasParoquialScope) return true;
  if (!targetOrgId) return true;
  return user.organizations.some((org) => org.id === targetOrgId);
};

/** Membro Ativo, Voluntário ou Fiel comum */
export const isMembro = (user: User | null): boolean => hasRole(user, ...MEMBRO_ROLES);

/** Lideranças com poder de proposição/gestão de projetos: pároco/bispo, clero, conselho pastoral ou coordenador */
export const canManageProjetos = (user: User | null): boolean => {
  if (!user) return false;
  return isParoco(user) || isClero(user) || isMembroCpp(user) || isCoordenador(user);
};

/**
 * Autoridade canônica global para criação/edição imediata sem restrição de vínculo local:
 * exclusivamente Pároco e Bispo Diocesano.
 */
export const isGlobalCreator = (user: User | null): boolean => {
  if (!user) return false;
  return isParoco(user);
};

/**
 * Quem pode transicionar (mudar de status) um projeto: Pároco/Bispo (`isParoco`) pode
 * transicionar qualquer projeto; Coordenador (`COORDENADOR`) só pode transicionar projetos
 * da(s) organização(ões) às quais está vinculado.
 */
export const canTransitionProjeto = (user: User | null, projetoOrgId: string): boolean => {
  if (!user) return false;
  if (isParoco(user)) return true;

  if (isCoordenador(user) && Array.isArray(user.organizations)) {
    return user.organizations.some((org: Organization) => org.id === projetoOrgId);
  }
  return false;
};

/**
 * Determina se o usuário pode submeter criação/edição/cancelamento de eventos
 * (aplicado imediatamente para Pároco/Bispo ou via solicitação de aprovação para demais lideranças).
 */
export const canManageEvento = (user: User | null, _eventoOrgId?: string): boolean => {
  if (!user) return false;
  return isParoco(user) || isClero(user) || isMembroCpp(user) || isCoordenador(user) || isSecretario(user);
};

/** Quem pode gerir o cadastro de ativos patrimoniais: Pároco/Bispo, Clero, CAE, Secretaria Paroquial ou Coordenador */
export const canManageAtivos = (user: User | null): boolean => {
  if (!user) return false;
  return isParoco(user) || isClero(user) || isMembroCae(user) || isCoordenador(user) || hasRole(user, 'SECRETARIA_PAROQUIAL');
};

/**
 * Quem pode gerir (manutenção, exclusão) um ativo pertencente a uma unidade territorial:
 * Pároco/Bispo e membros do CAE podem gerir em âmbito paroquial; Coordenador gere ativos da sua unidade.
 */
export const canManageManutencaoAtivo = (user: User | null, grupoParoquialId: string): boolean => {
  if (!user) return false;
  if (isParoco(user) || isMembroCae(user)) return true;

  if (isCoordenador(user) && Array.isArray(user.organizations)) {
    return user.organizations.some((org: Organization) => org.id === grupoParoquialId);
  }
  return false;
};

/** Excluir (soft delete) um ativo segue a mesma regra de escopo de `canManageManutencaoAtivo`. */
export const canExcluirAtivo = canManageManutencaoAtivo;

/**
 * Alçada decisória final sobre solicitações de aprovação de eventos e projetos:
 * exclusivamente Pároco (ou Bispo Diocesano). Conselhos, coordenadores e ADMIN não aprovam.
 */
export const canApprove = (user: User | null): boolean => {
  if (!user) return false;
  return isParoco(user);
};

/**
 * Homologação ou veto de atas e decisões dos conselhos (CPP, CPC, CAE):
 * exclusivamente Pároco (ou Bispo Diocesano) conforme Cân. 536 §2.
 */
export const canHomologarDecisao = (user: User | null): boolean => {
  if (!user) return false;
  return isParoco(user);
};

/**
 * Trava, abertura e conclusão do Ano Paroquial:
 * exclusivamente Pároco (ou Bispo Diocesano). ADMIN, conselhos e coordenadores são impedidos.
 */
export const canManageAnoParoquial = (user: User | null): boolean => {
  if (!user) return false;
  return isParoco(user);
};

/**
 * Gestão do cadastro de locais/espaços físicos da paróquia:
 * Pároco/Bispo e Secretaria Paroquial.
 */
export const canManageLocais = (user: User | null): boolean => {
  if (!user) return false;
  return isParoco(user) || hasRole(user, 'SECRETARIA_PAROQUIAL');
};

/**
 * Gestão de organizações e vínculos de usuários:
 * Pároco/Bispo (criação de grupos/pastorais e provisão canônica) e ADMIN (gestão técnica de usuários).
 */
export const canManageOrganizacoes = (user: User | null): boolean => {
  if (!user) return false;
  return isAdmin(user) || isParoco(user);
};

/**
 * Quem pode gerir Unidades Territoriais (Matriz, Comunidades) e Governo Eclesiástico:
 * Estritamente restrito a Pároco e Bispo Diocesano conforme o Direito Canônico.
 */
export const canManageUnidadesTerritoriais = (user: User | null): boolean => {
  if (!user) return false;
  return isParoco(user);
};

/**
 * Quem pode adicionar uma observação a um registro: lideranças/clero em qualquer organização;
 * usuário com papel exclusivamente de membro/voluntário apenas dentro das organizações vinculadas.
 */
export const canAddObservation = (user: User | null, targetOrgId: string): boolean => {
  if (!user) return false;

  const isApenasMembro = user.roles.length > 0 && user.roles.every((r) => isMembro({ ...user, roles: [r] }));
  if (!isApenasMembro) return true;

  if (Array.isArray(user.organizations)) {
    return user.organizations.some((org) => org.id === targetOrgId);
  }

  return false;
};

/**
 * A listagem de eventos pendentes de aprovação é visível para lideranças, clero e secretarias;
 * usuários exclusivamente com papéis de membro/voluntário/fiel não visualizam a fila de pendências.
 */
export const canViewEventosPendentes = (user: User | null): boolean => {
  if (!user) return false;
  const isApenasMembro = user.roles.length > 0 && user.roles.every((r) => isMembro({ ...user, roles: [r] }));
  return !isApenasMembro;
};

/**
 * Identifica o usuário comum/membro sem vínculo organizacional ativo.
 */
export const isUnlinkedUser = (user: User | null): boolean => {
  if (!user) return false;

  const isLeaderOrClero =
    isAdmin(user) ||
    isParoco(user) ||
    isClero(user) ||
    isMembroCpp(user) ||
    isMembroCae(user) ||
    isCoordenador(user) ||
    isSecretario(user);

  if (isLeaderOrClero) return false;

  const isUsuarioComumOuMembro = user.roles.every((r) => isMembro({ ...user, roles: [r] }));

  if (isUsuarioComumOuMembro && Array.isArray(user.organizations) && user.organizations.length === 0) {
    return true;
  }

  return false;
};
