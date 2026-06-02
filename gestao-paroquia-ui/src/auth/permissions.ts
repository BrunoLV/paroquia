export interface Organization {
  id: string;
  nome: string;
  tipo: string;
  contato: string | null;
  ativo: boolean;
}

export interface User {
  id: string;
  username: string;
  name: string;
  roles: string[];
  organizations?: Organization[];
}

export const isAdmin = (user: User | null): boolean => {
  if (!user) return false;
  return user.roles.includes('ADMIN') || user.roles.includes('ROLE_ADMIN');
};

export const canManageProjects = (user: User | null): boolean => {
  if (!user) return false;
  return user.roles.some((role: string) => 
    role === 'ADMIN' || 
    role === 'ROLE_ADMIN' ||
    role === 'PAROCO' ||
    role === 'PADRE' ||
    role === 'VIGARIO' ||
    role.endsWith('_COORDENADOR') || 
    role.endsWith('_VICE_COORDENADOR') ||
    role.endsWith('_PAROCO') || 
    role.endsWith('_VIGARIO') ||
    role.endsWith('_PADRE') ||
    role.startsWith('COORDENADOR_') ||
    role.startsWith('VICE_COORDENADOR_')
  );
};

export const isGlobalCreator = (user: User | null): boolean => {
  if (!user) return false;
  return user.roles.some((role: string) => 
    role === 'ADMIN' || 
    role === 'ROLE_ADMIN' ||
    role === 'PAROCO' ||
    role.endsWith('_PAROCO') ||
    role.includes('CONSELHO_COORDENADOR') || 
    role.includes('CONSELHO_VICE_COORDENADOR') ||
    role === 'COORDENADOR_CONSELHO' ||
    role === 'VICE_COORDENADOR_CONSELHO'
  );
};

export const canTransitionProject = (user: User | null, projectOrgId: string): boolean => {
  if (!user) return false;
  
  if (isGlobalCreator(user)) return true;

  const isOrgLeader = user.roles.some((role: string) => 
    role.endsWith('_COORDENADOR') || 
    role.endsWith('_VICE_COORDENADOR') ||
    role.startsWith('COORDENADOR_') ||
    role.startsWith('VICE_COORDENADOR_')
  );
  if (isOrgLeader && Array.isArray(user.organizations)) {
    return user.organizations.some((org: any) => org.id === projectOrgId);
  }
  return false;
};

/**
 * Determines if a user can manage (edit or cancel) an event.
 */
export const canManageEvent = (user: User | null, eventOrgId: string): boolean => {
  if (!user) return false;

  // 1. Global Admin
  if (isAdmin(user)) {
    return true;
  }

  // 2. Organizational Editor Roles
  // We look for roles that signify editor status (COORDENADOR or VICE_COORDENADOR)
  const isEditor = user.roles.some(role => 
    role === 'PAROCO' ||
    role === 'PADRE' ||
    role === 'VIGARIO' ||
    role.endsWith('_COORDENADOR') || 
    role.endsWith('_VICE_COORDENADOR') ||
    role.endsWith('_PAROCO') ||
    role.endsWith('_VIGARIO') ||
    role.endsWith('_PADRE') ||
    role.startsWith('COORDENADOR_') ||
    role.startsWith('VICE_COORDENADOR_')
  );

  return isEditor;
};

export const canApprove = (user: User | null): boolean => {
  if (!user) return false;

  return user.roles.some(role => 
    role === 'ADMIN' || 
    role === 'ROLE_ADMIN' ||
    role.includes('CONSELHO_COORDENADOR') || 
    role.includes('PAROCO') ||
    role === 'COORDENADOR_CONSELHO' ||
    role === 'VICE_COORDENADOR_CONSELHO' ||
    role === 'PAROCO'
  );
};

export const canManageAnoParoquial = (user: User | null): boolean => {
  if (!user) return false;

  return user.roles.some(role =>
    role === 'ADMIN' ||
    role === 'ROLE_ADMIN' ||
    role === 'ROLE_CLERO_PAROCO' ||
    role === 'ROLE_CONSELHO_COORDENADOR' ||
    role === 'ROLE_CONSELHO_VICE_COORDENADOR' ||
    role === 'PAROCO' ||
    role === 'COORDENADOR_CONSELHO' ||
    role === 'VICE_COORDENADOR_CONSELHO'
  );
};
