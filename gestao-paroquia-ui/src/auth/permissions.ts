export interface User {
  id: string;
  username: string;
  name: string;
  roles: string[];
}

/**
 * Determines if a user can manage (edit or cancel) an event.
 * 
 * Rules:
 * 1. ADMIN users can manage any event regardless of organization.
 * 2. Users with COORDENADOR or VICE_COORDENADOR roles for the event's 
 *    responsible organization can manage it.
 * 
 * Note: Organizational roles are formatted as ROLE_{ORG_TYPE}_{ROLE_NAME}.
 * Since we don't have the specific organization ID in the role string,
 * and the user might have multiple roles for different organizations,
 * we ideally need a way to correlate the role with the orgId.
 * 
 * Current limitation: The backend returns roles like ROLE_PASTORAL_COORDENADOR.
 * If a user is COORDENADOR of Pastoral A and MEMBRO of Pastoral B,
 * they will have ROLE_PASTORAL_COORDENADOR and ROLE_PASTORAL_MEMBRO.
 * Without orgId context in the role, we might grant too much permission
 * if we just check for presence of the role.
 * 
 * Refinement: For now, we will check if the user has an 'ADMIN' role
 * or if they have ANY role that starts with ROLE_ and ends with _COORDENADOR or _VICE_COORDENADOR.
 * This is a loose check but aligns with the 'Hide Buttons' UX goal to prevent
 * unauthorized attempts, while the backend remains the final authority.
 */
export const canManageEvent = (user: User | null, eventOrgId: string): boolean => {
  if (!user) return false;

  // 1. Global Admin
  if (user.roles.includes('ADMIN') || user.roles.includes('ROLE_ADMIN')) {
    return true;
  }

  // 2. Organizational Editor Roles
  // We look for roles that signify editor status (COORDENADOR or VICE_COORDENADOR)
  const isEditor = user.roles.some(role => 
    role.endsWith('_COORDENADOR') || 
    role.endsWith('_VICE_COORDENADOR') ||
    role.endsWith('_PAROCO') ||
    role.endsWith('_VIGARIO') ||
    role.endsWith('_PADRE')
  );

  return isEditor;
};
