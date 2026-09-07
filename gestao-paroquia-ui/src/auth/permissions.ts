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

// ─── Eclesial Role Predicates ────────────────────────────────────────────────

const PAROCO_ROLES = ['PAROCO', 'BISPO_DIOCESANO'] as const;
const CPP_ROLES = ['MEMBRO_CPP', 'MESA_CPP', 'MESA_DIRETORA_CPP'] as const;
const CAE_ROLES = ['MEMBRO_CAE'] as const;
const CLERO_ROLES = ['VIGARIO', 'DIACONO', 'PADRE'] as const;
const COORDENADOR_ROLES = [
  'COORD_PASTORAL_GLOBAL',
  'COORD_PASTORAL_LOCAL',
  'COORD_PASTORAL',
  'COORD_MOVIMENTO',
  'COORDENADOR_PASTORAL_GLOBAL',
  'COORDENADOR_PASTORAL_LOCAL',
  'COORDENADOR_PASTORAL',
  'COORDENADOR_MOVIMENTO',
  'PRESIDENTE_CPC',
  'COORDENADOR',
  'VICE_COORDENADOR',
] as const;
const MEMBRO_ROLES = [
  'VOLUNTARIO_PASTORAL_LOCAL',
  'MEMBRO_PASTORAL',
  'MEMBRO_MOVIMENTO',
  'MEMBRO_CPC',
  'MEMBRO',
] as const;

const hasRole = (user: User | null, ...roles: string[]): boolean => {
  if (!user) return false;
  return user.roles.some((r) =>
    roles.some((allowed) =>
      r === allowed ||
      r === `ROLE_${allowed}` ||
      r.endsWith(`_${allowed}`)
    )
  );
};


// ─── Public Permission Functions ─────────────────────────────────────────────

/** Administrador do sistema: acesso irrestrito, ignora qualquer regra de escopo organizacional */
export const isAdmin = (user: User | null): boolean => {
  if (!user) return false;
  return user.roles.includes('ADMIN') || user.roles.includes('ROLE_ADMIN');
};

/**
 * Papéis administrativos responsáveis pelo ciclo de vida do cadastro civil de fiéis.
 * Coordenadores mantêm somente a consulta restrita à sua própria organização.
 */
export const canManageCadastroFieis = (user: User | null): boolean => {
  if (!user) return false;
  return hasRole(
    user,
    'ADMIN',
    'GLOBAL_ADMIN',
    'PAROCO',
    'GLOBAL_PAROCO',
    'SECRETARIA_PAROQUIAL',
    'GLOBAL_SECRETARIA_PAROQUIAL'
  );
};

/**
 * Papéis com permissão para gerir o cadastro canônico e pessoal do clero.
 */
export const canManageClero = (user: User | null): boolean => {
  if (!user) return false;
  return hasRole(
    user,
    'ADMIN',
    'GLOBAL_ADMIN',
    'PAROCO',
    'GLOBAL_PAROCO',
    'SECRETARIA_PAROQUIAL',
    'GLOBAL_SECRETARIA_PAROQUIAL',
    'BISPO_DIOCESANO',
    'GLOBAL_BISPO_DIOCESANO'
  );
};

/** Pároco ou Bispo: palavra deliberativa final */
export const isParoco = (user: User | null): boolean => hasRole(user, ...PAROCO_ROLES);

/** Membro do Conselho Pastoral Paroquial (CPP) */
export const isMembroCpp = (user: User | null): boolean => hasRole(user, ...CPP_ROLES);

/** Membro do Conselho Econômico Paroquial (CAE) */
export const isMembroCae = (user: User | null): boolean => hasRole(user, ...CAE_ROLES);

/** Clero paroquial (Vigário, Diácono, Padre) */
export const isClero = (user: User | null): boolean => hasRole(user, ...CLERO_ROLES);

/** Coordenador de Pastoral ou Movimento */
export const isCoordenador = (user: User | null): boolean => hasRole(user, ...COORDENADOR_ROLES);

/** Membro de Pastoral ou Movimento */
export const isMembro = (user: User | null): boolean => hasRole(user, ...MEMBRO_ROLES);

/** Lideranças com poder de gestão de projetos: admin, pároco/bispo, clero, CPP ou coordenador de pastoral/movimento */
export const canManageProjetos = (user: User | null): boolean => {
  if (!user) return false;
  return isAdmin(user) || isParoco(user) || isClero(user) || isMembroCpp(user) || isCoordenador(user);
};

/**
 * Papéis com autoridade paroquial (não apenas local/setorial): admin, pároco/bispo, CPP,
 * ou coordenador/vice-coordenador de conselho (CONSELHO_COORDENADOR, COORD_CONSELHO e variantes).
 * Diferente de `isCoordenador`, que também cobre coordenadores de pastoral/movimento restritos
 * à própria organização — aqui o escopo é sempre global, sem necessidade de checar `organizations`.
 */
export const isGlobalCreator = (user: User | null): boolean => {
  if (!user) return false;
  return (
    isAdmin(user) ||
    isParoco(user) ||
    isMembroCpp(user) ||
    user.roles.some((r) =>
      r.includes('CONSELHO_COORDENADOR') ||
      r.includes('CONSELHO_VICE_COORDENADOR') ||
      r.includes('COORD_CONSELHO') ||
      r.includes('VICE_COORD_CONSELHO') ||
      r === 'COORDENADOR_CONSELHO' ||
      r === 'VICE_COORDENADOR_CONSELHO'
    )
  );
};

/**
 * Quem pode transicionar (mudar de status) um projeto: papéis com autoridade global
 * (`isGlobalCreator`) podem transicionar qualquer projeto; um coordenador de pastoral/movimento
 * só pode transicionar projetos da(s) organização(ões) às quais está vinculado, comparando
 * `projetoOrgId` com `user.organizations` — evita que um coordenador de uma pastoral altere
 * o status de projeto de outra pastoral/movimento sem vínculo.
 */
export const canTransitionProjeto = (user: User | null, projetoOrgId: string): boolean => {
  if (!user) return false;
  if (isGlobalCreator(user)) return true;

  if (isCoordenador(user) && Array.isArray(user.organizations)) {
    return user.organizations.some((org: Organization) => org.id === projetoOrgId);
  }
  return false;
};

/**
 * Determines if a user can manage (edit or cancel) an event.
 */
export const canManageEvento = (user: User | null, _eventoOrgId: string): boolean => {
  if (!user) return false;
  if (isAdmin(user)) return true;
  return isParoco(user) || isClero(user) || isMembroCpp(user) || isCoordenador(user);
};

/** Quem pode gerir o cadastro de ativos patrimoniais: admin, pároco/bispo, clero ou coordenador de pastoral/movimento */
export const canManageAtivos = (user: User | null): boolean => {
  if (!user) return false;
  return isAdmin(user) || isParoco(user) || isClero(user) || isCoordenador(user);
};

/**
 * Quem pode transferir a posse de um ativo patrimonial entre organizações: papéis com
 * autoridade global (`isGlobalCreator`) podem transferir qualquer ativo; um coordenador de
 * pastoral/movimento só pode transferir ativos que já pertencem à(s) organização(ões) às
 * quais está vinculado (`ativoOrgId` deve casar com `user.organizations`), evitando que
 * um coordenador movimente patrimônio de outra pastoral sem vínculo.
 */
export const canTransferAtivo = (user: User | null, ativoOrgId: string): boolean => {
  if (!user) return false;
  if (isGlobalCreator(user)) return true;

  if (isCoordenador(user) && Array.isArray(user.organizations)) {
    return user.organizations.some((org: Organization) => org.id === ativoOrgId);
  }
  return false;
};

/** Alocar um ativo a um evento segue a mesma regra de escopo organizacional de `canTransferAtivo` */
export const canAlocarAtivo = canTransferAtivo;

/** Registrar/gerir manutenção de um ativo segue a mesma regra de escopo organizacional de `canTransferAtivo` */
export const canManageManutencaoAtivo = canTransferAtivo;

/**
 * Quem pode decidir (aprovar/rejeitar) uma solicitação de aprovação pendente: admin, pároco/bispo,
 * CPP, ou coordenador/vice-coordenador de conselho (CONSELHO_COORDENADOR, COORD_CONSELHO e
 * variantes) — as mesmas instâncias com autoridade deliberativa final sobre eventos/projetos.
 */
export const canApprove = (user: User | null): boolean => {
  if (!user) return false;
  return (
    isAdmin(user) ||
    isParoco(user) ||
    isMembroCpp(user) ||
    user.roles.some((r) =>
      r.includes('CONSELHO_COORDENADOR') ||
      r.includes('COORD_CONSELHO') ||
      r === 'COORDENADOR_CONSELHO' ||
      r === 'VICE_COORDENADOR_CONSELHO'
    )
  );
};

/** Quem pode configurar o ano paroquial (calendário/período operacional): admin, pároco/bispo, CPP ou coordenador */
export const canManageAnoParoquial = (user: User | null): boolean => {
  if (!user) return false;
  return isAdmin(user) || isParoco(user) || isMembroCpp(user) || isCoordenador(user);
};

/**
 * Quem pode gerir o cadastro de locais/espaços físicos: admin, pároco/bispo, CPP, ou
 * coordenador/vice-coordenador de conselho — mesmo conjunto de autoridade global usado em
 * `canManageOrganizacoes`, pois locais são um recurso compartilhado da paróquia como um todo,
 * não escopado por organização individual.
 */
export const canManageLocais = (user: User | null): boolean => {
  if (!user) return false;
  return (
    isAdmin(user) ||
    isParoco(user) ||
    isMembroCpp(user) ||
    user.roles.some((r) =>
      r === 'ROLE_CONSELHO_COORDENADOR' ||
      r === 'ROLE_CONSELHO_VICE_COORDENADOR' ||
      r === 'ROLE_COORD_CONSELHO' ||
      r === 'ROLE_VICE_COORD_CONSELHO' ||
      r === 'COORDENADOR_CONSELHO' ||
      r === 'VICE_COORDENADOR_CONSELHO'
    )
  );
};

/**
 * Quem pode gerir a própria hierarquia organizacional (Matriz, Comunidades, movimentos):
 * admin, pároco/bispo, CPP, ou coordenador/vice-coordenador de conselho. Restrito de propósito
 * a esse conjunto restrito de papéis, já que alterar o organograma afeta o escopo de autorização
 * de todas as demais permissões baseadas em organização.
 */
export const canManageOrganizacoes = (user: User | null): boolean => {
  if (!user) return false;
  return (
    isAdmin(user) ||
    isParoco(user) ||
    isMembroCpp(user) ||
    user.roles.some((r) =>
      r === 'ROLE_CONSELHO_COORDENADOR' ||
      r === 'ROLE_CONSELHO_VICE_COORDENADOR' ||
      r === 'ROLE_COORD_CONSELHO' ||
      r === 'ROLE_VICE_COORD_CONSELHO' ||
      r === 'COORDENADOR_CONSELHO' ||
      r === 'VICE_COORDENADOR_CONSELHO'
    )
  );
};

/**
 * Quem pode adicionar uma observação a um registro: qualquer usuário que tenha ao menos um
 * papel além de "membro comum" (verificado remapeando cada papel individualmente por
 * `isMembro`) pode observar em qualquer organização. Um usuário cujos papéis são
 * exclusivamente de membro fica restrito a observar apenas dentro da(s) organização(ões) às
 * quais pertence (`targetOrgId` deve casar com `user.organizations`), evitando que um membro
 * comum registre observações sobre pastorais/movimentos alheios.
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
 * A aba/listagem de eventos pendentes de aprovação só faz sentido para quem tem alguma
 * responsabilidade de gestão ou aprovação; um usuário cujos papéis são exclusivamente
 * "membro comum" (MEMBRO/ROLE_PASTORAL_MEMBRO) não participa do fluxo de aprovação e não
 * deve enxergar a fila de pendências.
 */
export const canViewEventosPendentes = (user: User | null): boolean => {
  if (!user) return false;
  const isApenasMembro = user.roles.length > 0 && user.roles.every((r) => r === 'MEMBRO' || r === 'ROLE_PASTORAL_MEMBRO');
  return !isApenasMembro;
};

/**
 * Identifica o "usuário não vinculado": um membro/usuário comum sem nenhuma organização
 * associada, cenário usado pela UI para solicitar que o usuário seja vinculado a uma
 * pastoral/movimento antes de liberar funcionalidades dependentes de organização. Lideranças
 * e clero (admin, pároco/bispo, clero, CPP, CAE, coordenador, secretário) são sempre
 * excluídos dessa checagem, pois sua atuação não depende de vínculo com uma organização
 * específica. Só retorna `true` quando os papéis do usuário são exclusivamente de
 * membro/usuário comum E a lista de organizações está vazia.
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
    user.roles.some((r) => r === 'SECRETARIO' || r === 'ADMIN');

  if (isLeaderOrClero) return false;

  const isUsuarioComumOuMembro = user.roles.every(
    (r) => r === 'MEMBRO' || r === 'MEMBRO_PASTORAL' || r === 'MEMBRO_MOVIMENTO' || r === 'ROLE_PASTORAL_MEMBRO' || r === 'ROLE_USER' || r === 'USER'
  );

  if (isUsuarioComumOuMembro && Array.isArray(user.organizations) && user.organizations.length === 0) {
    return true;
  }

  return false;
};
