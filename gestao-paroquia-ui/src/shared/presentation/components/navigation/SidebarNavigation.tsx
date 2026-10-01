import { useEffect, useMemo, useState } from 'react';
import { List } from '@mui/material';
import type { User } from '@/contextos/iam/presentation/auth/permissions';
import { getNavigationSections } from './navigation';
import SidebarSection from './SidebarSection';

interface Props {
  user: User | null;
  pathname: string;
  expanded: boolean;
  onNavigate: () => void;
}

/** Seções estáveis evitam reabrir o grupo ativo a cada render; a abertura automática acompanha só a navegação. */
export default function SidebarNavigation({ user, pathname, expanded, onNavigate }: Props) {
  const sections = useMemo(() => getNavigationSections(user), [user]);
  const [closed, setClosed] = useState<ReadonlySet<string>>(new Set());
  useEffect(() => {
    const active = sections.find(section => section.items.some(item => item.path === pathname));
    if (active) setClosed(previous => {
      if (!previous.has(active.id)) return previous;
      const next = new Set(previous);
      next.delete(active.id);
      return next;
    });
  }, [pathname, sections]);
  const toggle = (id: string) => setClosed(previous => {
    const next = new Set(previous);
    if (next.has(id)) next.delete(id);
    else next.add(id);
    return next;
  });
  return <List component="nav" aria-label="Navegação principal" sx={{ px: 1, py: 1.5, flexGrow: 1, overflowY: 'auto' }}>
    {sections.map(section => <SidebarSection key={section.id} section={section} pathname={pathname}
      expanded={expanded} open={!closed.has(section.id)} onToggle={() => toggle(section.id)} onNavigate={onNavigate} />)}
  </List>;
}
