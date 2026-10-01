import { alpha, ListItem, ListItemButton, ListItemIcon, ListItemText, Tooltip } from '@mui/material';
import { Link } from 'react-router-dom';
import type { NavigationItem } from './navigation';

interface Props {
  item: NavigationItem;
  selected: boolean;
  expanded: boolean;
  onNavigate: () => void;
}

/** Links reais preservam semântica de navegação; o modo compacto conserva o nome acessível. */
export default function SidebarLink({ item, selected, expanded, onNavigate }: Props) {
  const Icon = item.icon;
  return <ListItem disablePadding sx={{ display: 'block' }}>
    <Tooltip title={expanded ? '' : item.label} placement="right" describeChild>
      <ListItemButton component={Link} to={item.path} onClick={onNavigate} selected={selected}
        aria-label={item.label} aria-current={selected ? 'page' : undefined}
        sx={{ minHeight: 36, my: 0.25, px: 1.25, py: 0.5, borderRadius: 1,
          justifyContent: expanded ? 'initial' : 'center',
          '&.Mui-selected': { bgcolor: theme => theme.custom?.navigationSelectedBg ?? alpha(theme.palette.primary.main, 0.1), color: 'primary.main' },
          '&:focus-visible': { outline: '2px solid', outlineColor: 'primary.main', outlineOffset: -2 } }}>
        <ListItemIcon sx={{ minWidth: 0, mr: expanded ? 1.25 : 0, color: selected ? 'primary.main' : 'text.secondary' }}>
          <Icon sx={{ fontSize: '1.25rem' }} />
        </ListItemIcon>
        {expanded && <ListItemText primary={item.label} slotProps={{ primary: {
          sx: { fontSize: '0.8125rem', fontWeight: selected ? 600 : 500, lineHeight: 1.3 },
        } }} />}
      </ListItemButton>
    </Tooltip>
  </ListItem>;
}
