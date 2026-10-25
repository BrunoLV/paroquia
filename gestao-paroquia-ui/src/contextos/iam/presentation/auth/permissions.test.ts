import {
    canAddObservation,
    canApprove,
    canRedigirAta,
    canManageAnoParoquial,
    canManageEvento,
    canManageCadastroFieis,
    canManageClero,
    canManageLocais,
    canManageOrganizacoes,
    canManageProjetos,
    canCreateUsuario,
    canApproveUsuario,
    canManageUsuarios,
    canEditOrInactivateUser,
    canTransitionProjeto,
    hasCoordenadorScope,
    isAdmin,
    isClero,
    isCoordenador,
    isGlobalCreator,
    isMembro,
    isMembroCae,
    isMembroConselhoEconomico,
    isMembroConselhoPastoral,
    isMembroCpp,
    isParoco,
    isSecretarioConselho,
    isUnlinkedUser,
    User
} from './permissions';

// ─── canManageCadastroFieis ────────────────────────────────────────────────

describe('canManageCadastroFieis', () => {
  it('permite Secretaria Paroquial administrar cadastros', () => {
    const secretaria: User = {
      id: '1', username: 'secretaria', name: 'Secretaria',
      roles: ['GLOBAL_SECRETARIA_PAROQUIAL']
    };

    expect(canManageCadastroFieis(secretaria)).toBe(true);
  });

  it('permite Administrador e Pároco administrar cadastros', () => {
    expect(canManageCadastroFieis({ id: '1', username: 'admin', name: 'Admin', roles: ['ADMIN'] })).toBe(true);
    expect(canManageCadastroFieis({ id: '2', username: 'paroco', name: 'Pároco', roles: ['GLOBAL_PAROCO'] })).toBe(true);
  });

  it('nega gestão cadastral ao coordenador pastoral', () => {
    const coordenador: User = {
      id: '1', username: 'coordenador', name: 'Coordenador',
      roles: ['COORDENADOR']
    };

    expect(canManageCadastroFieis(coordenador)).toBe(false);
  });
});

// ─── canManageClero ──────────────────────────────────────────────────────────

describe('canManageClero', () => {
  it('permite Administrador, Pároco, Secretaria e Bispo gerir o clero', () => {
    expect(canManageClero({ id: '1', username: 'admin', name: 'Admin', roles: ['ADMIN'] })).toBe(true);
    expect(canManageClero({ id: '2', username: 'paroco', name: 'Pároco', roles: ['GLOBAL_PAROCO'] })).toBe(true);
    expect(canManageClero({ id: '3', username: 'sec', name: 'Secretaria', roles: ['SECRETARIA_PAROQUIAL'] })).toBe(true);
    expect(canManageClero({ id: '4', username: 'bispo', name: 'Bispo', roles: ['ROLE_BISPO_DIOCESANO'] })).toBe(true);
  });

  it('nega gestão do clero a leigo ou coordenador pastoral', () => {
    expect(canManageClero({ id: '5', username: 'membro', name: 'Membro', roles: ['MEMBRO_ATIVO'] })).toBe(false);
    expect(canManageClero({ id: '6', username: 'coord', name: 'Coord', roles: ['COORDENADOR'] })).toBe(false);
  });
});

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
  it('allows MEMBRO_CONSELHO_PASTORAL and SECRETARIO_CONSELHO roles', () => {
    expect(isMembroCpp({ id: '1', username: 'cpp', name: 'Membro CPP', roles: ['MEMBRO_CONSELHO_PASTORAL'] })).toBe(true);
    expect(isMembroCpp({ id: '2', username: 'sec', name: 'Sec Conselho', roles: ['SECRETARIO_CONSELHO'] })).toBe(true);
  });

  it('denies MEMBRO_CONSELHO_ECONOMICO', () => {
    const user: User = { id: '1', username: 'cae', name: 'Membro CAE', roles: ['MEMBRO_CONSELHO_ECONOMICO'] };
    expect(isMembroCpp(user)).toBe(false);
  });
});

// ─── isMembroCae ─────────────────────────────────────────────────────────────

describe('isMembroCae', () => {
  it('allows MEMBRO_CONSELHO_ECONOMICO role', () => {
    const user: User = { id: '1', username: 'cae', name: 'Membro CAE', roles: ['MEMBRO_CONSELHO_ECONOMICO'] };
    expect(isMembroCae(user)).toBe(true);
  });

  it('denies MEMBRO_CONSELHO_PASTORAL', () => {
    const user: User = { id: '1', username: 'cpp', name: 'Membro CPP', roles: ['MEMBRO_CONSELHO_PASTORAL'] };
    expect(isMembroCae(user)).toBe(false);
  });
});

// ─── isClero ─────────────────────────────────────────────────────────────────

describe('isClero', () => {
  it('allows VIGARIO', () => {
    expect(isClero({ id: '1', username: 'v', name: 'V', roles: ['VIGARIO'] })).toBe(true);
  });

  it('allows DIACONO_PAROQUIAL', () => {
    expect(isClero({ id: '1', username: 'd', name: 'D', roles: ['DIACONO_PAROQUIAL'] })).toBe(true);
  });

  it('denies MEMBRO_CONSELHO_PASTORAL', () => {
    expect(isClero({ id: '1', username: 'c', name: 'C', roles: ['MEMBRO_CONSELHO_PASTORAL'] })).toBe(false);
  });
});

// ─── isCoordenador ───────────────────────────────────────────────────────────

describe('isCoordenador & hasCoordenadorScope', () => {
  it('allows COORDENADOR', () => {
    expect(isCoordenador({ id: '1', username: 'c', name: 'C', roles: ['COORDENADOR'] })).toBe(true);
  });

  it('allows ROLE_PASTORAL_COORDENADOR', () => {
    expect(isCoordenador({ id: '1', username: 'c', name: 'C', roles: ['ROLE_PASTORAL_COORDENADOR'] })).toBe(true);
  });

  it('denies MEMBRO_ATIVO', () => {
    expect(isCoordenador({ id: '1', username: 'c', name: 'C', roles: ['MEMBRO_ATIVO'] })).toBe(false);
  });

  it('identifies explicit council roles (isSecretarioConselho, isMembroConselhoPastoral, isMembroConselhoEconomico)', () => {
    expect(isSecretarioConselho({ id: '1', username: 's', name: 'S', roles: ['SECRETARIO_CONSELHO'] })).toBe(true);
    expect(isMembroConselhoPastoral({ id: '2', username: 'p', name: 'P', roles: ['MEMBRO_CONSELHO_PASTORAL'] })).toBe(true);
    expect(isMembroConselhoEconomico({ id: '3', username: 'e', name: 'E', roles: ['MEMBRO_CONSELHO_ECONOMICO'] })).toBe(true);
  });

  it('validates ABAC coordinator scope (paroquial vs local)', () => {
    const coordLocal: User = {
      id: '1', username: 'cl', name: 'Coord Local', roles: ['COORDENADOR'],
      organizations: [{ id: 'org-1', nome: 'Pastoral Capela', tipo: 'PASTORAL', contato: null, ativo: true }]
    };
    const coordMatriz: User = {
      id: '2', username: 'cm', name: 'Coord Matriz', roles: ['COORDENADOR'],
      organizations: [{ id: 'org-matriz', nome: 'Matriz', tipo: 'MATRIZ', contato: null, ativo: true }]
    };
    expect(hasCoordenadorScope(coordLocal, 'org-1')).toBe(true);
    expect(hasCoordenadorScope(coordLocal, 'org-2')).toBe(false);
    expect(hasCoordenadorScope(coordMatriz, 'org-2')).toBe(true);
  });
});

// ─── isMembro ────────────────────────────────────────────────────────────────

describe('isMembro', () => {
  it('allows MEMBRO_ATIVO', () => {
    expect(isMembro({ id: '1', username: 'm', name: 'M', roles: ['MEMBRO_ATIVO'] })).toBe(true);
  });

  it('allows VOLUNTARIO', () => {
    expect(isMembro({ id: '1', username: 'm', name: 'M', roles: ['VOLUNTARIO'] })).toBe(true);
  });

  it('allows FIEL', () => {
    expect(isMembro({ id: '1', username: 'm', name: 'M', roles: ['FIEL'] })).toBe(true);
  });

  it('denies COORDENADOR', () => {
    expect(isMembro({ id: '1', username: 'c', name: 'C', roles: ['COORDENADOR'] })).toBe(false);
  });
});

// ─── canManageEvento ──────────────────────────────────────────────────────────

describe('canManageEvento', () => {
  const orgId = 'org-1';

  it('returns false if user is null', () => {
    expect(canManageEvento(null, orgId)).toBe(false);
  });

  it('denies ADMIN users from managing ecclesiastical events', () => {
    const user: User = { id: '1', username: 'admin', name: 'Admin', roles: ['ADMIN'] };
    expect(canManageEvento(user, orgId)).toBe(false);
  });

  it('allows PAROCO to manage event', () => {
    const user: User = { id: '2', username: 'padre', name: 'Padre', roles: ['PAROCO'] };
    expect(canManageEvento(user, orgId)).toBe(true);
  });

  it('allows BISPO_DIOCESANO to manage event', () => {
    const user: User = { id: '2', username: 'bispo', name: 'Bispo', roles: ['BISPO_DIOCESANO'] };
    expect(canManageEvento(user, orgId)).toBe(true);
  });

  it('allows MEMBRO_CONSELHO_PASTORAL to manage event', () => {
    const user: User = { id: '2', username: 'cpp', name: 'CPP', roles: ['MEMBRO_CONSELHO_PASTORAL'] };
    expect(canManageEvento(user, orgId)).toBe(true);
  });

  it('allows COORDENADOR to manage event', () => {
    const user: User = { id: '2', username: 'coord', name: 'Coord', roles: ['COORDENADOR'] };
    expect(canManageEvento(user, orgId)).toBe(true);
  });

  it('denies MEMBRO_ATIVO and VOLUNTARIO from managing event', () => {
    expect(canManageEvento({ id: '3', username: 'membro', name: 'Membro', roles: ['MEMBRO_ATIVO'] }, orgId)).toBe(false);
    expect(canManageEvento({ id: '4', username: 'vol', name: 'Voluntário', roles: ['VOLUNTARIO'] }, orgId)).toBe(false);
  });
});

// ─── canApprove & canRedigirAta ──────────────────────────────────────────────

describe('canApprove', () => {
  it('returns false if user is null', () => {
    expect(canApprove(null)).toBe(false);
  });

  it('allows PAROCO and BISPO_DIOCESANO exclusively', () => {
    expect(canApprove({ id: '1', username: 'p', name: 'Paroco', roles: ['PAROCO'] })).toBe(true);
    expect(canApprove({ id: '2', username: 'b', name: 'Bispo', roles: ['BISPO_DIOCESANO'] })).toBe(true);
  });

  it('denies ADMIN, conselhos, and coordenadores', () => {
    expect(canApprove({ id: '1', username: 'admin', name: 'Admin', roles: ['ADMIN'] })).toBe(false);
    expect(canApprove({ id: '2', username: 'cpp', name: 'CPP', roles: ['MEMBRO_CONSELHO_PASTORAL'] })).toBe(false);
    expect(canApprove({ id: '3', username: 'sec', name: 'Sec', roles: ['SECRETARIO_CONSELHO'] })).toBe(false);
    expect(canApprove({ id: '4', username: 'c', name: 'Coord', roles: ['COORDENADOR'] })).toBe(false);
  });
});

describe('canRedigirAta', () => {
  it('é dica para secretários e coordenadores, não para Pároco, ADMIN, membros ou usuário nulo', () => {
    expect(canRedigirAta(null)).toBe(false);
    expect(canRedigirAta({ id: '1', username: 's', name: 'Sec', roles: ['SECRETARIO_CONSELHO'] })).toBe(true);
    expect(canRedigirAta({ id: '2', username: 'g', name: 'Grupo', roles: ['SECRETARIO_GRUPO'] })).toBe(true);
    expect(canRedigirAta({ id: '3', username: 'c', name: 'Coord', roles: ['COORDENADOR'] })).toBe(true);
    expect(canRedigirAta({ id: '4', username: 'p', name: 'Paroco', roles: ['PAROCO'] })).toBe(false);
    expect(canRedigirAta({ id: '5', username: 'a', name: 'Admin', roles: ['ADMIN'] })).toBe(false);
    expect(canRedigirAta({ id: '6', username: 'm', name: 'Membro', roles: ['MEMBRO_CONSELHO_PASTORAL'] })).toBe(false);
  });
});

// ─── canManageAnoParoquial ───────────────────────────────────────────────────

describe('canManageAnoParoquial', () => {
  it('returns false if user is null', () => {
    expect(canManageAnoParoquial(null)).toBe(false);
  });

  it('allows PAROCO and BISPO_DIOCESANO exclusively', () => {
    expect(canManageAnoParoquial({ id: '1', username: 'p', name: 'Paroco', roles: ['PAROCO'] })).toBe(true);
    expect(canManageAnoParoquial({ id: '2', username: 'b', name: 'Bispo', roles: ['BISPO_DIOCESANO'] })).toBe(true);
  });

  it('denies ADMIN, conselhos, coordenadores and membros', () => {
    expect(canManageAnoParoquial({ id: '1', username: 'admin', name: 'Admin', roles: ['ADMIN'] })).toBe(false);
    expect(canManageAnoParoquial({ id: '2', username: 'cpp', name: 'CPP', roles: ['MEMBRO_CONSELHO_PASTORAL'] })).toBe(false);
    expect(canManageAnoParoquial({ id: '3', username: 'c', name: 'Coord', roles: ['COORDENADOR'] })).toBe(false);
    expect(canManageAnoParoquial({ id: '4', username: 'm', name: 'Membro', roles: ['MEMBRO_ATIVO'] })).toBe(false);
  });
});

// ─── canManageProjetos ───────────────────────────────────────────────────────

describe('canManageProjetos', () => {
  it('returns false if user is null', () => {
    expect(canManageProjetos(null)).toBe(false);
  });

  it('allows PAROCO, BISPO_DIOCESANO, MEMBRO_CONSELHO_PASTORAL and COORDENADOR', () => {
    expect(canManageProjetos({ id: '1', username: 'a', name: 'A', roles: ['PAROCO'] })).toBe(true);
    expect(canManageProjetos({ id: '2', username: 'a', name: 'A', roles: ['BISPO_DIOCESANO'] })).toBe(true);
    expect(canManageProjetos({ id: '3', username: 'a', name: 'A', roles: ['MEMBRO_CONSELHO_PASTORAL'] })).toBe(true);
    expect(canManageProjetos({ id: '4', username: 'a', name: 'A', roles: ['COORDENADOR'] })).toBe(true);
  });

  it('denies ADMIN, MEMBRO_ATIVO and VOLUNTARIO', () => {
    expect(canManageProjetos({ id: '1', username: 'a', name: 'A', roles: ['ADMIN'] })).toBe(false);
    expect(canManageProjetos({ id: '2', username: 'a', name: 'A', roles: ['MEMBRO_ATIVO'] })).toBe(false);
    expect(canManageProjetos({ id: '3', username: 'a', name: 'A', roles: ['VOLUNTARIO'] })).toBe(false);
  });
});

// ─── isGlobalCreator ─────────────────────────────────────────────────────────

describe('isGlobalCreator', () => {
  it('returns false if user is null', () => {
    expect(isGlobalCreator(null)).toBe(false);
  });

  it('allows PAROCO and BISPO_DIOCESANO', () => {
    expect(isGlobalCreator({ id: '1', username: 'a', name: 'A', roles: ['PAROCO'] })).toBe(true);
    expect(isGlobalCreator({ id: '2', username: 'a', name: 'A', roles: ['BISPO_DIOCESANO'] })).toBe(true);
  });

  it('denies ADMIN, MEMBRO_CONSELHO_PASTORAL and COORDENADOR', () => {
    expect(isGlobalCreator({ id: '1', username: 'a', name: 'A', roles: ['ADMIN'] })).toBe(false);
    expect(isGlobalCreator({ id: '2', username: 'a', name: 'A', roles: ['MEMBRO_CONSELHO_PASTORAL'] })).toBe(false);
    expect(isGlobalCreator({ id: '3', username: 'a', name: 'A', roles: ['COORDENADOR'] })).toBe(false);
  });
});

// ─── canTransitionProjeto ────────────────────────────────────────────────────

describe('canTransitionProjeto', () => {
  const targetOrgId = 'org-catequese';

  it('returns false if user is null', () => {
    expect(canTransitionProjeto(null, targetOrgId)).toBe(false);
  });

  it('allows PAROCO regardless of linked organization', () => {
    const parocoUser = { id: '1', username: 'p', name: 'Paroco', roles: ['PAROCO'] };
    expect(canTransitionProjeto(parocoUser, targetOrgId)).toBe(true);
  });

  it('allows COORDENADOR linked to the project organization', () => {
    const localCoord = {
      id: '1',
      username: 'a',
      name: 'A',
      roles: ['COORDENADOR'],
      organizations: [{ id: targetOrgId, nome: 'Catequese', tipo: 'PASTORAL', contato: null, ativo: true }]
    };
    expect(canTransitionProjeto(localCoord, targetOrgId)).toBe(true);
  });

  it('denies COORDENADOR linked to a different organization', () => {
    const otherCoord = {
      id: '1',
      username: 'a',
      name: 'A',
      roles: ['COORDENADOR'],
      organizations: [{ id: 'other-org', nome: 'Liturgia', tipo: 'PASTORAL', contato: null, ativo: true }]
    };
    expect(canTransitionProjeto(otherCoord, targetOrgId)).toBe(false);
  });
});

// ─── canManageLocais ─────────────────────────────────────────────────────────

describe('canManageLocais', () => {
  it('returns false if user is null', () => {
    expect(canManageLocais(null)).toBe(false);
  });

  it('allows PAROCO and SECRETARIA_PAROQUIAL', () => {
    expect(canManageLocais({ id: '1', username: 'p', name: 'Paroco', roles: ['PAROCO'] })).toBe(true);
    expect(canManageLocais({ id: '2', username: 's', name: 'Secretaria', roles: ['SECRETARIA_PAROQUIAL'] })).toBe(true);
  });

  it('denies ADMIN and COORDENADOR alone', () => {
    expect(canManageLocais({ id: '1', username: 'admin', name: 'Admin', roles: ['ADMIN'] })).toBe(false);
    expect(canManageLocais({ id: '2', username: 'c', name: 'Coord', roles: ['COORDENADOR'] })).toBe(false);
  });
});

// ─── canManageOrganizacoes ───────────────────────────────────────────────────

describe('canManageOrganizacoes', () => {
  it('returns false if user is null', () => {
    expect(canManageOrganizacoes(null)).toBe(false);
  });

  it('allows ADMIN and PAROCO', () => {
    expect(canManageOrganizacoes({ id: '1', username: 'admin', name: 'Admin', roles: ['ADMIN'] })).toBe(true);
    expect(canManageOrganizacoes({ id: '2', username: 'p', name: 'Paroco', roles: ['PAROCO'] })).toBe(true);
  });

  it('denies MEMBRO_CONSELHO_PASTORAL and COORDENADOR alone', () => {
    expect(canManageOrganizacoes({ id: '1', username: 'cpp', name: 'CPP', roles: ['MEMBRO_CONSELHO_PASTORAL'] })).toBe(false);
    expect(canManageOrganizacoes({ id: '2', username: 'c', name: 'Coord', roles: ['COORDENADOR'] })).toBe(false);
  });
});

// ─── canAddObservation ───────────────────────────────────────────────────────

describe('canAddObservation', () => {
  const targetOrgId = 'org-music';

  it('returns false if user is null', () => {
    expect(canAddObservation(null, targetOrgId)).toBe(false);
  });

  it('allows non-MEMBRO roles to add observation to any organization', () => {
    const user: User = { id: '1', username: 'v', name: 'Vigario', roles: ['VIGARIO'] };
    expect(canAddObservation(user, targetOrgId)).toBe(true);
  });

  it('allows MEMBRO_ATIVO if linked to the event organization', () => {
    const user: User = {
      id: '2',
      username: 'membro',
      name: 'Membro',
      roles: ['MEMBRO_ATIVO'],
      organizations: [{ id: targetOrgId, nome: 'Musica', tipo: 'PASTORAL', contato: null, ativo: true }]
    };
    expect(canAddObservation(user, targetOrgId)).toBe(true);
  });

  it('denies MEMBRO_ATIVO if not linked to the target organization', () => {
    const user: User = {
      id: '2',
      username: 'membro',
      name: 'Membro',
      roles: ['MEMBRO_ATIVO'],
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

  it('returns false for ADMIN regardless of organization vinculos', () => {
    const user: User = { id: '1', username: 'admin', name: 'Admin', roles: ['ADMIN'], organizations: [] };
    expect(isUnlinkedUser(user)).toBe(false);
  });

  it('returns false for PAROCO', () => {
    const user: User = { id: '1', username: 'p', name: 'Paroco', roles: ['PAROCO'], organizations: [] };
    expect(isUnlinkedUser(user)).toBe(false);
  });

  it('returns false for MEMBRO_CONSELHO_PASTORAL', () => {
    const user: User = { id: '1', username: 'cpp', name: 'CPP', roles: ['MEMBRO_CONSELHO_PASTORAL'], organizations: [] };
    expect(isUnlinkedUser(user)).toBe(false);
  });

  it('returns true for MEMBRO_ATIVO users with no active organizations', () => {
    const user: User = { id: '2', username: 'membro', name: 'Membro', roles: ['MEMBRO_ATIVO'], organizations: [] };
    expect(isUnlinkedUser(user)).toBe(true);
  });

  it('returns true for VOLUNTARIO users with no active organizations', () => {
    const user: User = { id: '2', username: 'membro', name: 'Membro', roles: ['VOLUNTARIO'], organizations: [] };
    expect(isUnlinkedUser(user)).toBe(true);
  });

  it('returns false for MEMBRO_ATIVO users with at least one organization', () => {
    const user: User = {
      id: '2',
      username: 'membro',
      name: 'Membro',
      roles: ['MEMBRO_ATIVO'],
      organizations: [{ id: 'org-1', nome: 'Catequese', tipo: 'PASTORAL', contato: null, ativo: true }]
    };
    expect(isUnlinkedUser(user)).toBe(false);
  });
});

describe('canCreateUsuario, canApproveUsuario, canManageUsuarios', () => {
  const admin: User = { id: '1', username: 'admin', name: 'Admin', roles: ['ADMIN'] };
  const paroco: User = { id: '2', username: 'paroco', name: 'Pároco', roles: ['GLOBAL_PAROCO'] };
  const secretaria: User = { id: '3', username: 'sec', name: 'Secretaria', roles: ['SECRETARIA_PAROQUIAL'] };
  const fiel: User = { id: '4', username: 'fiel', name: 'Fiel', roles: ['FIEL'] };

  it('permite Admin, Pároco e Secretaria criarem usuários', () => {
    expect(canCreateUsuario(admin)).toBe(true);
    expect(canCreateUsuario(paroco)).toBe(true);
    expect(canCreateUsuario(secretaria)).toBe(true);
    expect(canCreateUsuario(fiel)).toBe(false);
  });

  it('restringe aprovação de usuários estritamente a Admin e Pároco', () => {
    expect(canApproveUsuario(admin)).toBe(true);
    expect(canApproveUsuario(paroco)).toBe(true);
    expect(canApproveUsuario(secretaria)).toBe(false);
    expect(canApproveUsuario(fiel)).toBe(false);
  });

  it('permite Admin, Pároco e Secretaria gerenciarem lista de usuários', () => {
    expect(canManageUsuarios(admin)).toBe(true);
    expect(canManageUsuarios(paroco)).toBe(true);
    expect(canManageUsuarios(secretaria)).toBe(true);
    expect(canManageUsuarios(fiel)).toBe(false);
  });

  it('restringe edição e inativação de usuários estritamente a Admin e Pároco (vedado à Secretaria)', () => {
    expect(canEditOrInactivateUser(admin)).toBe(true);
    expect(canEditOrInactivateUser(paroco)).toBe(true);
    expect(canEditOrInactivateUser(secretaria)).toBe(false);
    expect(canEditOrInactivateUser(fiel)).toBe(false);
  });
});

