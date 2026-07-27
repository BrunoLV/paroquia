import {
    canAddObservation,
    canApprove,
    canManageAnoParoquial,
    canManageEvent,
    canManageLocais,
    canManageOrganizacoes,
    canManageProjects,
    canTransitionProject,
    isAdmin,
    isClero,
    isCoordenador,
    isGlobalCreator,
    isMembro,
    isMembroCae,
    isMembroCpp,
    isParoco,
    isUnlinkedUser,
    User
} from './permissions';

// ─── isParoco ────────────────────────────────────────────────────────────────

describe('isParoco', () => {
  it('allows PAROCO role', () => {
    const user: User = { id: '1', username: 'p', name: 'Paroco', roles: ['PAROCO'] };
    expect(isParoco(user)).toBe(true);
  });

  it('allows BISPO_DIOCESANO role', () => {
    const user: User = { id: '1', username: 'b', name: 'Bispo', roles: ['BISPO_DIOCESANO'] };
    expect(isParoco(user)).toBe(true);
  });

  it('denies VIGARIO', () => {
    const user: User = { id: '1', username: 'v', name: 'Vigario', roles: ['VIGARIO'] };
    expect(isParoco(user)).toBe(false);
  });
});

// ─── isMembroCpp ─────────────────────────────────────────────────────────────

describe('isMembroCpp', () => {
  it('allows MEMBRO_CPP role', () => {
    const user: User = { id: '1', username: 'cpp', name: 'Membro CPP', roles: ['MEMBRO_CPP'] };
    expect(isMembroCpp(user)).toBe(true);
  });

  it('denies MEMBRO_CAE', () => {
    const user: User = { id: '1', username: 'cae', name: 'Membro CAE', roles: ['MEMBRO_CAE'] };
    expect(isMembroCpp(user)).toBe(false);
  });
});

// ─── isMembroCae ─────────────────────────────────────────────────────────────

describe('isMembroCae', () => {
  it('allows MEMBRO_CAE role', () => {
    const user: User = { id: '1', username: 'cae', name: 'Membro CAE', roles: ['MEMBRO_CAE'] };
    expect(isMembroCae(user)).toBe(true);
  });

  it('denies MEMBRO_CPP', () => {
    const user: User = { id: '1', username: 'cpp', name: 'Membro CPP', roles: ['MEMBRO_CPP'] };
    expect(isMembroCae(user)).toBe(false);
  });
});

// ─── isClero ─────────────────────────────────────────────────────────────────

describe('isClero', () => {
  it('allows VIGARIO', () => {
    expect(isClero({ id: '1', username: 'v', name: 'V', roles: ['VIGARIO'] })).toBe(true);
  });

  it('allows DIACONO', () => {
    expect(isClero({ id: '1', username: 'd', name: 'D', roles: ['DIACONO'] })).toBe(true);
  });

  it('allows PADRE', () => {
    expect(isClero({ id: '1', username: 'p', name: 'P', roles: ['PADRE'] })).toBe(true);
  });

  it('denies MEMBRO_CPP', () => {
    expect(isClero({ id: '1', username: 'c', name: 'C', roles: ['MEMBRO_CPP'] })).toBe(false);
  });
});

// ─── isCoordenador ───────────────────────────────────────────────────────────

describe('isCoordenador', () => {
  it('allows COORDENADOR_PASTORAL', () => {
    expect(isCoordenador({ id: '1', username: 'c', name: 'C', roles: ['COORDENADOR_PASTORAL'] })).toBe(true);
  });

  it('allows COORDENADOR_MOVIMENTO', () => {
    expect(isCoordenador({ id: '1', username: 'c', name: 'C', roles: ['COORDENADOR_MOVIMENTO'] })).toBe(true);
  });

  it('allows COORDENADOR', () => {
    expect(isCoordenador({ id: '1', username: 'c', name: 'C', roles: ['COORDENADOR'] })).toBe(true);
  });

  it('allows VICE_COORDENADOR', () => {
    expect(isCoordenador({ id: '1', username: 'c', name: 'C', roles: ['VICE_COORDENADOR'] })).toBe(true);
  });

  it('denies MEMBRO_PASTORAL', () => {
    expect(isCoordenador({ id: '1', username: 'c', name: 'C', roles: ['MEMBRO_PASTORAL'] })).toBe(false);
  });
});

// ─── isMembro ────────────────────────────────────────────────────────────────

describe('isMembro', () => {
  it('allows MEMBRO_PASTORAL', () => {
    expect(isMembro({ id: '1', username: 'm', name: 'M', roles: ['MEMBRO_PASTORAL'] })).toBe(true);
  });

  it('allows MEMBRO_MOVIMENTO', () => {
    expect(isMembro({ id: '1', username: 'm', name: 'M', roles: ['MEMBRO_MOVIMENTO'] })).toBe(true);
  });

  it('allows MEMBRO', () => {
    expect(isMembro({ id: '1', username: 'm', name: 'M', roles: ['MEMBRO'] })).toBe(true);
  });

  it('denies COORDENADOR_PASTORAL', () => {
    expect(isMembro({ id: '1', username: 'c', name: 'C', roles: ['COORDENADOR_PASTORAL'] })).toBe(false);
  });
});

// ─── canManageEvent ──────────────────────────────────────────────────────────

describe('canManageEvent', () => {
  const orgId = 'org-1';

  it('returns false if user is null', () => {
    expect(canManageEvent(null, orgId)).toBe(false);
  });

  it('allows ADMIN users to manage any event', () => {
    const user: User = { id: '1', username: 'admin', name: 'Admin', roles: ['ADMIN'] };
    expect(canManageEvent(user, orgId)).toBe(true);
  });

  it('allows PAROCO to manage event', () => {
    const user: User = { id: '2', username: 'padre', name: 'Padre', roles: ['PAROCO'] };
    expect(canManageEvent(user, orgId)).toBe(true);
  });

  it('allows BISPO_DIOCESANO to manage event', () => {
    const user: User = { id: '2', username: 'bispo', name: 'Bispo', roles: ['BISPO_DIOCESANO'] };
    expect(canManageEvent(user, orgId)).toBe(true);
  });

  it('allows MEMBRO_CPP to manage event', () => {
    const user: User = { id: '2', username: 'cpp', name: 'CPP', roles: ['MEMBRO_CPP'] };
    expect(canManageEvent(user, orgId)).toBe(true);
  });

  it('allows COORDENADOR_PASTORAL to manage event', () => {
    const user: User = { id: '2', username: 'coord', name: 'Coord', roles: ['COORDENADOR_PASTORAL'] };
    expect(canManageEvent(user, orgId)).toBe(true);
  });

  it('allows COORDENADOR_MOVIMENTO to manage event', () => {
    const user: User = { id: '2', username: 'coord', name: 'Coord', roles: ['COORDENADOR_MOVIMENTO'] };
    expect(canManageEvent(user, orgId)).toBe(true);
  });

  it('denies MEMBRO_PASTORAL from managing event', () => {
    const user: User = { id: '3', username: 'membro', name: 'Membro', roles: ['MEMBRO_PASTORAL'] };
    expect(canManageEvent(user, orgId)).toBe(false);
  });

  it('denies MEMBRO_MOVIMENTO from managing event', () => {
    const user: User = { id: '3', username: 'membro', name: 'Membro', roles: ['MEMBRO_MOVIMENTO'] };
    expect(canManageEvent(user, orgId)).toBe(false);
  });
});

// ─── canApprove ──────────────────────────────────────────────────────────────

describe('canApprove', () => {
  it('returns false if user is null', () => {
    expect(canApprove(null)).toBe(false);
  });

  it('allows ADMIN', () => {
    const user: User = { id: '1', username: 'admin', name: 'Admin', roles: ['ADMIN'] };
    expect(canApprove(user)).toBe(true);
  });

  it('allows PAROCO', () => {
    const user: User = { id: '1', username: 'p', name: 'Paroco', roles: ['PAROCO'] };
    expect(canApprove(user)).toBe(true);
  });

  it('allows BISPO_DIOCESANO', () => {
    const user: User = { id: '1', username: 'b', name: 'Bispo', roles: ['BISPO_DIOCESANO'] };
    expect(canApprove(user)).toBe(true);
  });

  it('allows MEMBRO_CPP', () => {
    const user: User = { id: '1', username: 'cpp', name: 'CPP', roles: ['MEMBRO_CPP'] };
    expect(canApprove(user)).toBe(true);
  });

  it('allows ROLE_CONSELHO_COORDENADOR', () => {
    const user: User = { id: '1', username: 'c', name: 'Coord', roles: ['ROLE_CONSELHO_COORDENADOR'] };
    expect(canApprove(user)).toBe(true);
  });

  it('denies COORDENADOR_PASTORAL without CPP role', () => {
    const user: User = { id: '1', username: 'c', name: 'Coord', roles: ['COORDENADOR_PASTORAL'] };
    expect(canApprove(user)).toBe(false);
  });
});

// ─── canManageAnoParoquial ───────────────────────────────────────────────────

describe('canManageAnoParoquial', () => {
  it('returns false if user is null', () => {
    expect(canManageAnoParoquial(null)).toBe(false);
  });

  it('allows ADMIN', () => {
    const user: User = { id: '1', username: 'admin', name: 'Admin', roles: ['ADMIN'] };
    expect(canManageAnoParoquial(user)).toBe(true);
  });

  it('allows PAROCO', () => {
    const user: User = { id: '1', username: 'p', name: 'Paroco', roles: ['PAROCO'] };
    expect(canManageAnoParoquial(user)).toBe(true);
  });

  it('allows MEMBRO_CPP', () => {
    const user: User = { id: '1', username: 'cpp', name: 'CPP', roles: ['MEMBRO_CPP'] };
    expect(canManageAnoParoquial(user)).toBe(true);
  });

  it('allows COORDENADOR_PASTORAL', () => {
    const user: User = { id: '1', username: 'c', name: 'Coord', roles: ['COORDENADOR_PASTORAL'] };
    expect(canManageAnoParoquial(user)).toBe(true);
  });

  it('denies MEMBRO_PASTORAL', () => {
    const user: User = { id: '1', username: 'c', name: 'Coord', roles: ['MEMBRO_PASTORAL'] };
    expect(canManageAnoParoquial(user)).toBe(false);
  });
});

// ─── canManageProjects ───────────────────────────────────────────────────────

describe('canManageProjects', () => {
  it('returns false if user is null', () => {
    expect(canManageProjects(null)).toBe(false);
  });

  it('allows ADMIN', () => {
    expect(canManageProjects({ id: '1', username: 'a', name: 'A', roles: ['ADMIN'] })).toBe(true);
  });

  it('allows PAROCO', () => {
    expect(canManageProjects({ id: '1', username: 'a', name: 'A', roles: ['PAROCO'] })).toBe(true);
  });

  it('allows BISPO_DIOCESANO', () => {
    expect(canManageProjects({ id: '1', username: 'a', name: 'A', roles: ['BISPO_DIOCESANO'] })).toBe(true);
  });

  it('allows MEMBRO_CPP', () => {
    expect(canManageProjects({ id: '1', username: 'a', name: 'A', roles: ['MEMBRO_CPP'] })).toBe(true);
  });

  it('allows COORDENADOR_PASTORAL', () => {
    expect(canManageProjects({ id: '1', username: 'a', name: 'A', roles: ['COORDENADOR_PASTORAL'] })).toBe(true);
  });

  it('allows COORDENADOR_MOVIMENTO', () => {
    expect(canManageProjects({ id: '1', username: 'a', name: 'A', roles: ['COORDENADOR_MOVIMENTO'] })).toBe(true);
  });

  it('denies MEMBRO_PASTORAL', () => {
    expect(canManageProjects({ id: '1', username: 'a', name: 'A', roles: ['MEMBRO_PASTORAL'] })).toBe(false);
  });

  it('denies MEMBRO_MOVIMENTO', () => {
    expect(canManageProjects({ id: '1', username: 'a', name: 'A', roles: ['MEMBRO_MOVIMENTO'] })).toBe(false);
  });
});

// ─── isGlobalCreator ─────────────────────────────────────────────────────────

describe('isGlobalCreator', () => {
  it('returns false if user is null', () => {
    expect(isGlobalCreator(null)).toBe(false);
  });

  it('allows ADMIN', () => {
    expect(isGlobalCreator({ id: '1', username: 'a', name: 'A', roles: ['ADMIN'] })).toBe(true);
  });

  it('allows PAROCO', () => {
    expect(isGlobalCreator({ id: '1', username: 'a', name: 'A', roles: ['PAROCO'] })).toBe(true);
  });

  it('allows MEMBRO_CPP', () => {
    expect(isGlobalCreator({ id: '1', username: 'a', name: 'A', roles: ['MEMBRO_CPP'] })).toBe(true);
  });

  it('allows COORDENADOR_CONSELHO', () => {
    expect(isGlobalCreator({ id: '1', username: 'a', name: 'A', roles: ['COORDENADOR_CONSELHO'] })).toBe(true);
  });

  it('denies COORDENADOR_PASTORAL alone', () => {
    expect(isGlobalCreator({ id: '1', username: 'a', name: 'A', roles: ['COORDENADOR_PASTORAL'] })).toBe(false);
  });
});

// ─── canTransitionProject ────────────────────────────────────────────────────

describe('canTransitionProject', () => {
  const targetOrgId = 'org-catequese';

  it('returns false if user is null', () => {
    expect(canTransitionProject(null, targetOrgId)).toBe(false);
  });

  it('allows global creators regardless of linked organization', () => {
    const adminUser = { id: '1', username: 'a', name: 'A', roles: ['ADMIN'] };
    expect(canTransitionProject(adminUser, targetOrgId)).toBe(true);
  });

  it('allows COORDENADOR_PASTORAL linked to the project organization', () => {
    const localCoord = {
      id: '1',
      username: 'a',
      name: 'A',
      roles: ['COORDENADOR_PASTORAL'],
      organizations: [{ id: targetOrgId, nome: 'Catequese', tipo: 'PASTORAL', contato: null, ativo: true }]
    };
    expect(canTransitionProject(localCoord, targetOrgId)).toBe(true);
  });

  it('denies COORDENADOR_PASTORAL linked to a different organization', () => {
    const otherCoord = {
      id: '1',
      username: 'a',
      name: 'A',
      roles: ['COORDENADOR_PASTORAL'],
      organizations: [{ id: 'other-org', nome: 'Liturgia', tipo: 'PASTORAL', contato: null, ativo: true }]
    };
    expect(canTransitionProject(otherCoord, targetOrgId)).toBe(false);
  });
});

// ─── canManageLocais ─────────────────────────────────────────────────────────

describe('canManageLocais', () => {
  it('returns false if user is null', () => {
    expect(canManageLocais(null)).toBe(false);
  });

  it('allows ADMIN', () => {
    const user: User = { id: '1', username: 'admin', name: 'Admin', roles: ['ADMIN'] };
    expect(canManageLocais(user)).toBe(true);
  });

  it('allows PAROCO', () => {
    const user: User = { id: '1', username: 'p', name: 'Paroco', roles: ['PAROCO'] };
    expect(canManageLocais(user)).toBe(true);
  });

  it('allows MEMBRO_CPP', () => {
    const user: User = { id: '1', username: 'cpp', name: 'CPP', roles: ['MEMBRO_CPP'] };
    expect(canManageLocais(user)).toBe(true);
  });

  it('allows ROLE_CONSELHO_COORDENADOR', () => {
    const user: User = { id: '1', username: 'c', name: 'Coord', roles: ['ROLE_CONSELHO_COORDENADOR'] };
    expect(canManageLocais(user)).toBe(true);
  });

  it('denies COORDENADOR_PASTORAL alone', () => {
    const user: User = { id: '1', username: 'c', name: 'Coord', roles: ['COORDENADOR_PASTORAL'] };
    expect(canManageLocais(user)).toBe(false);
  });
});

// ─── canManageOrganizacoes ───────────────────────────────────────────────────

describe('canManageOrganizacoes', () => {
  it('returns false if user is null', () => {
    expect(canManageOrganizacoes(null)).toBe(false);
  });

  it('allows ADMIN', () => {
    const user: User = { id: '1', username: 'admin', name: 'Admin', roles: ['ADMIN'] };
    expect(canManageOrganizacoes(user)).toBe(true);
  });

  it('allows PAROCO', () => {
    const user: User = { id: '1', username: 'p', name: 'Paroco', roles: ['PAROCO'] };
    expect(canManageOrganizacoes(user)).toBe(true);
  });

  it('allows MEMBRO_CPP', () => {
    const user: User = { id: '1', username: 'cpp', name: 'CPP', roles: ['MEMBRO_CPP'] };
    expect(canManageOrganizacoes(user)).toBe(true);
  });

  it('denies COORDENADOR_PASTORAL alone', () => {
    const user: User = { id: '1', username: 'c', name: 'Coord', roles: ['COORDENADOR_PASTORAL'] };
    expect(canManageOrganizacoes(user)).toBe(false);
  });
});

// ─── canAddObservation ───────────────────────────────────────────────────────

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

// ─── isAdmin ─────────────────────────────────────────────────────────────────

describe('isAdmin', () => {
  it('allows ADMIN role', () => {
    expect(isAdmin({ id: '1', username: 'a', name: 'A', roles: ['ADMIN'] })).toBe(true);
  });

  it('allows ROLE_ADMIN role', () => {
    expect(isAdmin({ id: '1', username: 'a', name: 'A', roles: ['ROLE_ADMIN'] })).toBe(true);
  });

  it('denies PAROCO', () => {
    expect(isAdmin({ id: '1', username: 'p', name: 'P', roles: ['PAROCO'] })).toBe(false);
  });
});

// ─── isUnlinkedUser ──────────────────────────────────────────────────────────

describe('isUnlinkedUser', () => {
  it('returns false if user is null', () => {
    expect(isUnlinkedUser(null)).toBe(false);
  });

  it('returns false for ADMIN regardless of organization memberships', () => {
    const user: User = { id: '1', username: 'admin', name: 'Admin', roles: ['ADMIN'], organizations: [] };
    expect(isUnlinkedUser(user)).toBe(false);
  });

  it('returns false for PAROCO', () => {
    const user: User = { id: '1', username: 'p', name: 'Paroco', roles: ['PAROCO'], organizations: [] };
    expect(isUnlinkedUser(user)).toBe(false);
  });

  it('returns false for MEMBRO_CPP', () => {
    const user: User = { id: '1', username: 'cpp', name: 'CPP', roles: ['MEMBRO_CPP'], organizations: [] };
    expect(isUnlinkedUser(user)).toBe(false);
  });

  it('returns true for MEMBRO_PASTORAL users with no active organizations', () => {
    const user: User = { id: '2', username: 'membro', name: 'Membro', roles: ['MEMBRO_PASTORAL'], organizations: [] };
    expect(isUnlinkedUser(user)).toBe(true);
  });

  it('returns true for MEMBRO users with no active organizations', () => {
    const user: User = { id: '2', username: 'membro', name: 'Membro', roles: ['MEMBRO'], organizations: [] };
    expect(isUnlinkedUser(user)).toBe(true);
  });

  it('returns false for MEMBRO users with at least one organization', () => {
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
