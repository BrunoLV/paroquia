import { canManageEvent, User } from './permissions';

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
    // Re-checking backend rules: ORG_EDITOR_ROLES = Set.of(COORDENADOR, VICE_COORDENADOR);
    const user: User = { 
      id: '4', 
      username: 'sec', 
      name: 'Secretario', 
      roles: ['ROLE_CONSELHO_SECRETARIO'] 
    };
    expect(canManageEvent(user, orgId)).toBe(false);
  });
});
