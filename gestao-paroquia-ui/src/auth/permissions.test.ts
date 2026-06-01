import { canManageEvent, canApprove, canManageAnoParoquial, User } from './permissions';

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

