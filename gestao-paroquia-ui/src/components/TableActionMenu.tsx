import React, { useState } from 'react';
import { ListItemIcon, ListItemText, Menu, MenuItem } from '@mui/material';
import { MoreHoriz as MoreHorizIcon } from '@mui/icons-material';
import ActionButton from './ActionButton';

export interface TableAction {
  label: string;
  icon: React.ReactElement;
  onClick: () => void;
  disabled?: boolean;
}

interface TableActionMenuProps {
  actions: TableAction[];
}

/** Agrupa ações da linha em um botão textual para manter tabelas legíveis. */
const TableActionMenu: React.FC<TableActionMenuProps> = ({ actions }) => {
  const [anchorEl, setAnchorEl] = useState<HTMLElement | null>(null);
  const closeMenu = () => setAnchorEl(null);

  return (
    <>
      <ActionButton label="Ações" title="Ações" icon={<MoreHorizIcon />} size="small" onClick={(event) => setAnchorEl(event.currentTarget)} />
      <Menu anchorEl={anchorEl} open={Boolean(anchorEl)} onClose={closeMenu}>
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
