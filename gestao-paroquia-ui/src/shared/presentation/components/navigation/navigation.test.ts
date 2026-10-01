import { describe, expect, it } from 'vitest';
import { canApprove, canManageAnoParoquial, canManageLocais, canManageOrganizacoes, canManageUnidadesTerritoriais, canViewSacramentos, type User } from '@/contextos/iam/presentation/auth/permissions';
import { getNavigationSections } from './navigation';

/** Reagrupamento não redefine autorização: cada papel conserva exatamente os predicados anteriores. */
describe('paridade de autorização do menu', () => {
  it.each(['ADMIN', 'PAROCO', 'BISPO_DIOCESANO', 'VIGARIO', 'DIACONO_PAROQUIAL', 'SECRETARIA_PAROQUIAL',
    'SECRETARIO_CONSELHO', 'MEMBRO_CONSELHO_PASTORAL', 'MEMBRO_CONSELHO_ECONOMICO', 'COORDENADOR',
    'SECRETARIO_GRUPO', 'MEMBRO_ATIVO', 'VOLUNTARIO', 'FIEL'])('preserva visibilidade para %s', role => {
    const user: User = { id: 'teste', username: 'teste', name: 'Pessoa de teste', roles: [role], organizations: [] };
    const expected = ['/painel', '/calendario', '/clerigos', '/conselhos', '/fieis', '/notificacoes', '/projetos', '/reservas', '/organograma', '/ativos', '/relatorios'];
    if (canManageOrganizacoes(user)) expected.push('/organizacoes');
    if (canViewSacramentos(user)) expected.push('/sacramentos');
    if (canApprove(user)) expected.push('/ajustes-cadastrais', '/aprovacoes');
    if (canManageAnoParoquial(user) || canManageOrganizacoes(user)) expected.push('/configuracoes');
    if (canManageLocais(user)) expected.push('/locais');
    if (canManageUnidadesTerritoriais(user)) expected.push('/governo-territorio');
    const sections = getNavigationSections(user);
    expect(sections.every(section => section.items.length > 0)).toBe(true);
    expect(sections.flatMap(section => section.items.map(item => item.path)).sort()).toEqual(expected.sort());
  });
});
