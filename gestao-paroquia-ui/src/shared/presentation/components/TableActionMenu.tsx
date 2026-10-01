import React, { useId, useState } from 'react';
import { ListItemIcon, ListItemText, Menu, MenuItem } from '@mui/material';
import MoreVertIcon from '@mui/icons-material/MoreVert';
import IconActionButton from '@/shared/presentation/components/IconActionButton';

export interface TableAction {
  label: string;
  icon: React.ReactElement;
  onClick: () => void;
  disabled?: boolean;
}

interface TableActionMenuProps {
  actions: TableAction[];
}

/** Menu de linha com gatilho compacto ⋮; cliques e teclado não acionam a navegação da linha. */
const TableActionMenu: React.FC<TableActionMenuProps> = ({ actions }) => {
  const [anchorEl, setAnchorEl] = useState<HTMLElement | null>(null);
  const closeMenu = () => setAnchorEl(null);
  const buttonId = useId();
  const menuId = useId();

  return (
    <>
      <IconActionButton title="Ações" icon={<MoreVertIcon fontSize="small" />} size="small"
        id={buttonId} aria-haspopup="menu" aria-expanded={Boolean(anchorEl)} aria-controls={anchorEl ? menuId : undefined}
        onKeyDown={(event) => event.stopPropagation()}
        onClick={(event) => { event.stopPropagation(); setAnchorEl(event.currentTarget); }} />
      <Menu anchorEl={anchorEl} open={Boolean(anchorEl)} onClose={closeMenu}
        slotProps={{ list: { id: menuId, 'aria-labelledby': buttonId } }}
        onClick={(event) => event.stopPropagation()} onKeyDown={(event) => event.stopPropagation()}>
        {actions.map((action) => (
          <MenuItem key={action.label} disabled={action.disabled} onClick={() => { closeMenu(); action.onClick(); }}>
            <ListItemIcon>{action.icon}</ListItemIcon>
            <ListItemText>{action.label}</ListItemText>
          </MenuItem>
        ))}
      </Menu>
    </>
  );
};

export default TableActionMenu;
