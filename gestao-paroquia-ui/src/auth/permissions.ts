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

export const isAdmin = (user: User | null): boolean => {
  if (!user) return false;
  return user.roles.includes('ADMIN') || user.roles.includes('ROLE_ADMIN');
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

export const canManageProjects = (user: User | null): boolean => {
  if (!user) return false;
  return isAdmin(user) || isParoco(user) || isClero(user) || isMembroCpp(user) || isCoordenador(user);
};

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

export const canTransitionProject = (user: User | null, projectOrgId: string): boolean => {
  if (!user) return false;
  if (isGlobalCreator(user)) return true;

  if (isCoordenador(user) && Array.isArray(user.organizations)) {
    return user.organizations.some((org: any) => org.id === projectOrgId);
  }
  return false;
};

/**
 * Determines if a user can manage (edit or cancel) an event.
 */
export const canManageEvent = (user: User | null, _eventOrgId: string): boolean => {
  if (!user) return false;
  if (isAdmin(user)) return true;
  return isParoco(user) || isClero(user) || isMembroCpp(user) || isCoordenador(user);
};

export const canManageAtivos = (user: User | null): boolean => {
  if (!user) return false;
  return isAdmin(user) || isParoco(user) || isClero(user) || isCoordenador(user);
};

export const canTransferAtivo = (user: User | null, ativoOrgId: string): boolean => {
  if (!user) return false;
  if (isGlobalCreator(user)) return true;

  if (isCoordenador(user) && Array.isArray(user.organizations)) {
    return user.organizations.some((org: any) => org.id === ativoOrgId);
  }
  return false;
};

export const canAlocarAtivo = canTransferAtivo;

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

export const canManageAnoParoquial = (user: User | null): boolean => {
  if (!user) return false;
  return isAdmin(user) || isParoco(user) || isMembroCpp(user) || isCoordenador(user);
};

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

export const canAddObservation = (user: User | null, targetOrgId: string): boolean => {
  if (!user) return false;

  const isOnlyMember = user.roles.length > 0 && user.roles.every((r) => isMembro({ ...user, roles: [r] }));
  if (!isOnlyMember) return true;

  if (Array.isArray(user.organizations)) {
    return user.organizations.some((org) => org.id === targetOrgId);
  }

  return false;
};

export const canViewPendingEvents = (user: User | null): boolean => {
  if (!user) return false;
  const isOnlyMember = user.roles.length > 0 && user.roles.every((r) => r === 'MEMBRO' || r === 'ROLE_PASTORAL_MEMBRO');
  return !isOnlyMember;
};

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

  const isRegularUserOrMember = user.roles.every(
    (r) => r === 'MEMBRO' || r === 'MEMBRO_PASTORAL' || r === 'MEMBRO_MOVIMENTO' || r === 'ROLE_PASTORAL_MEMBRO' || r === 'ROLE_USER' || r === 'USER'
  );

  if (isRegularUserOrMember && Array.isArray(user.organizations) && user.organizations.length === 0) {
    return true;
  }

  return false;
};
