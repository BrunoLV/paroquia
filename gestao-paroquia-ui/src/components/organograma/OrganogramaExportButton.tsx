import React, {useRef, useState} from 'react';
import {CircularProgress, ListItemIcon, ListItemText, Menu, MenuItem} from '@mui/material';
import FileDownloadIcon from '@mui/icons-material/FileDownload';
import KeyboardArrowDownIcon from '@mui/icons-material/KeyboardArrowDown';
import ImageIcon from '@mui/icons-material/Image';
import CodeIcon from '@mui/icons-material/Code';
import {ExportFormat} from '../../hooks/useOrganogramaExport';
import ActionButton from '../ActionButton';

export interface OrganogramaExportButtonProps {
  onExport: (format: ExportFormat) => void;
  isExporting?: boolean;
}

export const OrganogramaExportButton: React.FC<OrganogramaExportButtonProps> = ({
  onExport,
  isExporting = false,
}) => {
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const open = Boolean(anchorEl);
  const anchorRef = useRef<HTMLSpanElement>(null);

  const handleClick = () => {
    setAnchorEl(anchorRef.current);
  };

  const handleClose = () => {
    setAnchorEl(null);
  };

  const handleSelectFormat = (format: ExportFormat) => {
    handleClose();
    onExport(format);
  };

  const buttonTitle = isExporting ? 'Exportando...' : 'Exportar Organograma';

  return (
    <>
      <span ref={anchorRef} style={{ display: 'inline-block' }}>
        <ActionButton
          title={buttonTitle}
          icon={
            isExporting ? (
              <CircularProgress
                size={20}
                color="inherit"
                data-testid="export-loading-indicator"
              />
            ) : (
              <>
                <FileDownloadIcon />
                <KeyboardArrowDownIcon fontSize="small" />
              </>
            )
          }
          color="primary"
          disabled={isExporting}
          onClick={handleClick}
        />
      </span>

      <Menu
        anchorEl={anchorEl}
        open={open}
        onClose={handleClose}
        anchorOrigin={{
          vertical: 'bottom',
          horizontal: 'right',
        }}
        transformOrigin={{
          vertical: 'top',
          horizontal: 'right',
        }}
        slotProps={{
          paper: {
            elevation: 3,
            sx: {
              minWidth: 200,
              mt: 0.5,
            },
          },
        }}
      >
        <MenuItem onClick={() => handleSelectFormat('png')}>
          <ListItemIcon>
            <ImageIcon fontSize="small" />
          </ListItemIcon>
          <ListItemText primary="Exportar como PNG" secondary="Imagem de alta resolução" />
        </MenuItem>

        <MenuItem onClick={() => handleSelectFormat('svg')}>
          <ListItemIcon>
            <CodeIcon fontSize="small" />
          </ListItemIcon>
          <ListItemText primary="Exportar como SVG" secondary="Vetor escalável" />
        </MenuItem>
      </Menu>
    </>
  );
};
