import { useId } from 'react';
import { Box, Collapse, Divider, List, ListItemButton, Typography } from '@mui/material';
import ExpandLess from '@mui/icons-material/ExpandLess';
import ExpandMore from '@mui/icons-material/ExpandMore';
import SidebarLink from './SidebarLink';
import type { NavigationSection } from './navigation';

interface Props {
  section: NavigationSection;
  expanded: boolean;
  open: boolean;
  pathname: string;
  onToggle: () => void;
  onNavigate: () => void;
}

/** Cabeçalho e recuo tornam a relação seção/itens visível sem competir com a rota selecionada. */
export default function SidebarSection({ section, expanded, open, pathname, onToggle, onNavigate }: Props) {
  const id = useId();
  const links = section.items.map(item => <SidebarLink key={item.path} item={item} expanded={expanded}
    selected={pathname === item.path} onNavigate={onNavigate} />);
  return <Box role="group" aria-label={section.title} sx={{ mb: 1.25 }}>
    {expanded ? <>
      <ListItemButton onClick={onToggle} aria-expanded={open} aria-controls={id}
        sx={{ minHeight: 34, px: 1.25, py: 0.75, mb: 0.5, borderRadius: 1, bgcolor: 'action.hover', color: 'text.primary' }}>
        <Typography sx={{ flexGrow: 1, fontSize: '0.75rem', fontWeight: 700, lineHeight: 1.4 }}>{section.title}</Typography>
        {open ? <ExpandLess sx={{ fontSize: '1rem' }} /> : <ExpandMore sx={{ fontSize: '1rem' }} />}
      </ListItemButton>
      <Collapse in={open} timeout="auto">
        <List id={id} disablePadding sx={{ ml: 1.25, pl: 0.75, borderLeft: '1px solid', borderColor: 'divider' }}>{links}</List>
      </Collapse>
    </> : <><Divider sx={{ my: 1 }} /><List disablePadding>{links}</List></>}
  </Box>;
}
