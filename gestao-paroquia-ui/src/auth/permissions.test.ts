import { 
  canManageEvent, 
  canApprove, 
  canManageAnoParoquial, 
  canManageLocais,
  canManageOrganizacoes,
  canManageProjects, 
  isGlobalCreator, 
  canTransitionProject, 
  canAddObservation,
  canViewPendingEvents,
  isUnlinkedUser,
  User 
} from './permissions';

describe('canManageEvent', () => {
  const orgId = 'org-1';

  it('returns false if user is null', () => {
    expect(canManageEvent(null, orgId)).toBe(false);
  });

  it('allows ADMIN users to manage any event', () => {
    const user: User = { id: '1', username: 'admin', name: 'Admin', roles: ['ADMIN'] };
    expect(canManageEvent(user, orgId)).toBe(true);
  });

  it('allows ROLE_ADMIN users to manage any event', () => {
    const user: User = { id: '1', username: 'admin', name: 'Admin', roles: ['ROLE_ADMIN'] };
    expect(canManageEvent(user, orgId)).toBe(true);
  });

  it('allows COORDENADOR to manage event', () => {
    const user: User = { 
      id: '2', 
      username: 'coord', 
      name: 'Coord', 
      roles: ['ROLE_PASTORAL_COORDENADOR'] 
    };
    expect(canManageEvent(user, orgId)).toBe(true);
  });

  it('allows VICE_COORDENADOR to manage event', () => {
    const user: User = { 
      id: '2', 
      username: 'coord', 
      name: 'Coord', 
      roles: ['ROLE_MOVIMENTO_VICE_COORDENADOR'] 
    };
    expect(canManageEvent(user, orgId)).toBe(true);
  });

  it('allows PAROCO to manage event', () => {
    const user: User = { 
      id: '2', 
      username: 'padre', 
      name: 'Padre', 
      roles: ['ROLE_CLERO_PAROCO'] 
    };
    expect(canManageEvent(user, orgId)).toBe(true);
  });

  it('denies MEMBRO from managing event', () => {
    const user: User = { 
      id: '3', 
      username: 'membro', 
      name: 'Membro', 
      roles: ['ROLE_PASTORAL_MEMBRO'] 
    };
    expect(canManageEvent(user, orgId)).toBe(false);
  });

  it('denies SECRETARIO from managing event (per backend rules, they can create but not edit/patch usually, or wait...)', () => {
    const user: User = { 
      id: '4', 
      username: 'sec', 
      name: 'Secretario', 
      roles: ['ROLE_CONSELHO_SECRETARIO'] 
    };
    expect(canManageEvent(user, orgId)).toBe(false);
  });
});

describe('canApprove', () => {
  it('returns false if user is null', () => {
    expect(canApprove(null)).toBe(false);
  });

  it('allows ADMIN', () => {
    const user: User = { id: '1', username: 'admin', name: 'Admin', roles: ['ADMIN'] };
    expect(canApprove(user)).toBe(true);
  });

  it('allows ROLE_CONSELHO_COORDENADOR', () => {
    const user: User = { id: '1', username: 'c', name: 'Coord', roles: ['ROLE_CONSELHO_COORDENADOR'] };
    expect(canApprove(user)).toBe(true);
  });

  it('allows ROLE_CLERO_PAROCO', () => {
    const user: User = { id: '1', username: 'p', name: 'Paroco', roles: ['ROLE_CLERO_PAROCO'] };
    expect(canApprove(user)).toBe(true);
  });

  it('denies other roles like COORDENADOR or MEMBRO', () => {
    const user: User = { id: '1', username: 'c', name: 'Coord', roles: ['ROLE_PASTORAL_COORDENADOR', 'ROLE_PASTORAL_MEMBRO'] };
    expect(canApprove(user)).toBe(false);
  });
});

describe('canManageAnoParoquial', () => {
  it('returns false if user is null', () => {
    expect(canManageAnoParoquial(null)).toBe(false);
  });

  it('allows ADMIN', () => {
    const user: User = { id: '1', username: 'admin', name: 'Admin', roles: ['ADMIN'] };
    expect(canManageAnoParoquial(user)).toBe(true);
  });

  it('allows ROLE_ADMIN', () => {
    const user: User = { id: '1', username: 'admin', name: 'Admin', roles: ['ROLE_ADMIN'] };
    expect(canManageAnoParoquial(user)).toBe(true);
  });

  it('allows ROLE_CLERO_PAROCO', () => {
    const user: User = { id: '1', username: 'p', name: 'Paroco', roles: ['ROLE_CLERO_PAROCO'] };
    expect(canManageAnoParoquial(user)).toBe(true);
  });

  it('allows ROLE_CONSELHO_COORDENADOR', () => {
    const user: User = { id: '1', username: 'c', name: 'Coord', roles: ['ROLE_CONSELHO_COORDENADOR'] };
    expect(canManageAnoParoquial(user)).toBe(true);
  });

  it('allows ROLE_CONSELHO_VICE_COORDENADOR', () => {
    const user: User = { id: '1', username: 'vc', name: 'Vice Coord', roles: ['ROLE_CONSELHO_VICE_COORDENADOR'] };
    expect(canManageAnoParoquial(user)).toBe(true);
  });

  it('denies other roles like secretarios, membros or regular pastoral leaders', () => {
    const user: User = { id: '1', username: 'c', name: 'Coord', roles: ['ROLE_PASTORAL_COORDENADOR', 'ROLE_CONSELHO_SECRETARIO'] };
    expect(canManageAnoParoquial(user)).toBe(false);
  });
});

describe('canManageProjects', () => {
  it('returns false if user is null', () => {
    expect(canManageProjects(null)).toBe(false);
  });

  it('allows ADMIN, ROLE_ADMIN, PAROCO, PADRE, VIGARIO', () => {
    expect(canManageProjects({ id: '1', username: 'a', name: 'A', roles: ['ADMIN'] })).toBe(true);
    expect(canManageProjects({ id: '1', username: 'a', name: 'A', roles: ['ROLE_ADMIN'] })).toBe(true);
    expect(canManageProjects({ id: '1', username: 'a', name: 'A', roles: ['PAROCO'] })).toBe(true);
    expect(canManageProjects({ id: '1', username: 'a', name: 'A', roles: ['ROLE_CLERO_PAROCO'] })).toBe(true);
  });

  it('allows coordinators and vice-coordinators (global or membership)', () => {
    expect(canManageProjects({ id: '1', username: 'a', name: 'A', roles: ['COORDENADOR_PASTORAL'] })).toBe(true);
    expect(canManageProjects({ id: '1', username: 'a', name: 'A', roles: ['ROLE_PASTORAL_COORDENADOR'] })).toBe(true);
    expect(canManageProjects({ id: '1', username: 'a', name: 'A', roles: ['VICE_COORDENADOR_CONSELHO'] })).toBe(true);
    expect(canManageProjects({ id: '1', username: 'a', name: 'A', roles: ['ROLE_CONSELHO_VICE_COORDENADOR'] })).toBe(true);
  });

  it('denies regular members', () => {
    expect(canManageProjects({ id: '1', username: 'a', name: 'A', roles: ['ROLE_PASTORAL_MEMBRO'] })).toBe(false);
    expect(canManageProjects({ id: '1', username: 'a', name: 'A', roles: ['MEMBRO_CONSELHO'] })).toBe(false);
  });
});

describe('isGlobalCreator', () => {
  it('returns false if user is null', () => {
    expect(isGlobalCreator(null)).toBe(false);
  });

  it('allows ADMIN, ROLE_ADMIN, PAROCO, and conselho coordinators/vice-coordinators', () => {
    expect(isGlobalCreator({ id: '1', username: 'a', name: 'A', roles: ['ADMIN'] })).toBe(true);
    expect(isGlobalCreator({ id: '1', username: 'a', name: 'A', roles: ['COORDENADOR_CONSELHO'] })).toBe(true);
    expect(isGlobalCreator({ id: '1', username: 'a', name: 'A', roles: ['ROLE_CONSELHO_COORDENADOR'] })).toBe(true);
    expect(isGlobalCreator({ id: '1', username: 'a', name: 'A', roles: ['VICE_COORDENADOR_CONSELHO'] })).toBe(true);
    expect(isGlobalCreator({ id: '1', username: 'a', name: 'A', roles: ['ROLE_CONSELHO_VICE_COORDENADOR'] })).toBe(true);
  });

  it('denies pastoral coordinators or general members', () => {
    expect(isGlobalCreator({ id: '1', username: 'a', name: 'A', roles: ['COORDENADOR_PASTORAL'] })).toBe(false);
    expect(isGlobalCreator({ id: '1', username: 'a', name: 'A', roles: ['ROLE_PASTORAL_COORDENADOR'] })).toBe(false);
  });
});

describe('canTransitionProject', () => {
  const targetOrgId = 'org-catequese';

  it('returns false if user is null', () => {
    expect(canTransitionProject(null, targetOrgId)).toBe(false);
  });

  it('allows global creators regardless of linked organization', () => {
    const adminUser = { id: '1', username: 'a', name: 'A', roles: ['ADMIN'] };
    expect(canTransitionProject(adminUser, targetOrgId)).toBe(true);
  });

  it('allows local coordinators linked to the project organization', () => {
    const localCoord = {
      id: '1',
      username: 'a',
      name: 'A',
      roles: ['ROLE_PASTORAL_COORDENADOR'],
      organizations: [{ id: targetOrgId, nome: 'Catequese', tipo: 'PASTORAL', contato: null, ativo: true }]
    };
    expect(canTransitionProject(localCoord, targetOrgId)).toBe(true);
  });

  it('denies local coordinators of a different organization', () => {
    const otherCoord = {
      id: '1',
      username: 'a',
      name: 'A',
      roles: ['ROLE_PASTORAL_COORDENADOR'],
      organizations: [{ id: 'other-org', nome: 'Liturgia', tipo: 'PASTORAL', contato: null, ativo: true }]
    };
    expect(canTransitionProject(otherCoord, targetOrgId)).toBe(false);
  });
});

describe('canManageLocais', () => {
  it('returns false if user is null', () => {
    expect(canManageLocais(null)).toBe(false);
  });

  it('allows ADMIN', () => {
    const user: User = { id: '1', username: 'admin', name: 'Admin', roles: ['ADMIN'] };
    expect(canManageLocais(user)).toBe(true);
  });

  it('allows ROLE_ADMIN', () => {
    const user: User = { id: '1', username: 'admin', name: 'Admin', roles: ['ROLE_ADMIN'] };
    expect(canManageLocais(user)).toBe(true);
  });

  it('allows ROLE_CLERO_PAROCO', () => {
    const user: User = { id: '1', username: 'p', name: 'Paroco', roles: ['ROLE_CLERO_PAROCO'] };
    expect(canManageLocais(user)).toBe(true);
  });

  it('allows ROLE_CONSELHO_COORDENADOR', () => {
    const user: User = { id: '1', username: 'c', name: 'Coord', roles: ['ROLE_CONSELHO_COORDENADOR'] };
    expect(canManageLocais(user)).toBe(true);
  });

  it('allows ROLE_CONSELHO_VICE_COORDENADOR', () => {
    const user: User = { id: '1', username: 'vc', name: 'Vice Coord', roles: ['ROLE_CONSELHO_VICE_COORDENADOR'] };
    expect(canManageLocais(user)).toBe(true);
  });

  it('denies other roles like secretarios, membros or regular pastoral leaders', () => {
    const user: User = { id: '1', username: 'c', name: 'Coord', roles: ['ROLE_PASTORAL_COORDENADOR', 'ROLE_CONSELHO_SECRETARIO'] };
    expect(canManageLocais(user)).toBe(false);
  });
});

describe('canManageOrganizacoes', () => {
  it('returns false if user is null', () => {
    expect(canManageOrganizacoes(null)).toBe(false);
  });

  it('allows ADMIN', () => {
    const user: User = { id: '1', username: 'admin', name: 'Admin', roles: ['ADMIN'] };
    expect(canManageOrganizacoes(user)).toBe(true);
  });

  it('allows ROLE_ADMIN', () => {
    const user: User = { id: '1', username: 'admin', name: 'Admin', roles: ['ROLE_ADMIN'] };
    expect(canManageOrganizacoes(user)).toBe(true);
  });

  it('allows ROLE_CLERO_PAROCO', () => {
    const user: User = { id: '1', username: 'p', name: 'Paroco', roles: ['ROLE_CLERO_PAROCO'] };
    expect(canManageOrganizacoes(user)).toBe(true);
  });

  it('allows ROLE_CONSELHO_COORDENADOR', () => {
    const user: User = { id: '1', username: 'c', name: 'Coord', roles: ['ROLE_CONSELHO_COORDENADOR'] };
    expect(canManageOrganizacoes(user)).toBe(true);
  });

  it('allows ROLE_CONSELHO_VICE_COORDENADOR', () => {
    const user: User = { id: '1', username: 'vc', name: 'Vice Coord', roles: ['ROLE_CONSELHO_VICE_COORDENADOR'] };
    expect(canManageOrganizacoes(user)).toBe(true);
  });

  it('denies other roles like secretarios, membros or regular pastoral leaders', () => {
    const user: User = { id: '1', username: 'c', name: 'Coord', roles: ['ROLE_PASTORAL_COORDENADOR', 'ROLE_CONSELHO_SECRETARIO'] };
    expect(canManageOrganizacoes(user)).toBe(false);
  });
});

describe('canAddObservation', () => {
  const targetOrgId = 'org-music';

  it('returns false if user is null', () => {
    expect(canAddObservation(null, targetOrgId)).toBe(false);
  });

  it('allows non-MEMBRO roles to add observation to any organization', () => {
    const user: User = { id: '1', username: 'p', name: 'Padre', roles: ['PADRE'] };
    expect(canAddObservation(user, targetOrgId)).toBe(true);
  });

  it('allows MEMBRO if linked to the event organization', () => {
    const user: User = {
      id: '2',
      username: 'membro',
      name: 'Membro',
      roles: ['MEMBRO'],
      organizations: [{ id: targetOrgId, nome: 'Musica', tipo: 'PASTORAL', contato: null, ativo: true }]
    };
    expect(canAddObservation(user, targetOrgId)).toBe(true);
  });

  it('denies MEMBRO if not linked to the target organization', () => {
    const user: User = {
      id: '2',
      username: 'membro',
      name: 'Membro',
      roles: ['MEMBRO'],
      organizations: [{ id: 'other-org', nome: 'Outra', tipo: 'PASTORAL', contato: null, ativo: true }]
    };
    expect(canAddObservation(user, targetOrgId)).toBe(false);
  });
});

describe('isUnlinkedUser', () => {
  it('returns false if user is null', () => {
    expect(isUnlinkedUser(null)).toBe(false);
  });

  it('returns false for admin or clero roles regardless of organization memberships', () => {
    const user: User = { id: '1', username: 'admin', name: 'Admin', roles: ['ADMIN'], organizations: [] };
    expect(isUnlinkedUser(user)).toBe(false);
  });

  it('returns true for member users with no active organizations', () => {
    const user: User = { id: '2', username: 'membro', name: 'Membro', roles: ['MEMBRO'], organizations: [] };
    expect(isUnlinkedUser(user)).toBe(true);
  });

  it('returns false for member users with at least one active organization', () => {
    const user: User = {
      id: '2',
      username: 'membro',
      name: 'Membro',
      roles: ['MEMBRO'],
      organizations: [{ id: 'org-1', nome: 'Catequese', tipo: 'PASTORAL', contato: null, ativo: true }]
    };
    expect(isUnlinkedUser(user)).toBe(false);
  });
});



