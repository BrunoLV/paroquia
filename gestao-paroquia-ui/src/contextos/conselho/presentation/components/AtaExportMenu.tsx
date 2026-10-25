import React, { useState } from 'react';
import { ListItemIcon, ListItemText, Menu, MenuItem } from '@mui/material';
import DescriptionIcon from '@mui/icons-material/Description';
import FileDownloadIcon from '@mui/icons-material/FileDownload';
import PictureAsPdfIcon from '@mui/icons-material/PictureAsPdf';
import ActionButton from '@/shared/presentation/components/ActionButton';
import { OpcoesExportacao } from '@/contextos/conselho/domain/ata';

interface AtaExportMenuProps {
  /** Rótulo do botão: "Exportar" numa ata, "Exportar livro de atas" na compilação. */
  rotulo: string;
  disabled?: boolean;
  onExportar: (opcoes: OpcoesExportacao) => void;
}

const OPCOES: { rotulo: string; opcoes: OpcoesExportacao }[] = [
  { rotulo: 'PDF — ata estruturada', opcoes: { formato: 'PDF', layout: 'ESTRUTURADA', conteudo: 'ATA_COMPLETA' } },
  { rotulo: 'PDF — texto de Livro de Atas', opcoes: { formato: 'PDF', layout: 'NARRATIVA', conteudo: 'ATA_COMPLETA' } },
  { rotulo: 'PDF — somente as decisões', opcoes: { formato: 'PDF', layout: 'ESTRUTURADA', conteudo: 'SOMENTE_DECISOES' } },
  { rotulo: 'Word — ata estruturada', opcoes: { formato: 'DOCX', layout: 'ESTRUTURADA', conteudo: 'ATA_COMPLETA' } },
  { rotulo: 'Word — texto de Livro de Atas', opcoes: { formato: 'DOCX', layout: 'NARRATIVA', conteudo: 'ATA_COMPLETA' } },
  { rotulo: 'Word — somente as decisões', opcoes: { formato: 'DOCX', layout: 'ESTRUTURADA', conteudo: 'SOMENTE_DECISOES' } },
];

/**
 * Menu de exportação em PDF e Word, nos dois layouts (estruturado ou texto de Livro de Atas) e com a opção de
 * levar só o quadro de decisões. Ata não selada sai como minuta, o que o servidor sinaliza no próprio documento.
 */
export const AtaExportMenu: React.FC<AtaExportMenuProps> = ({ rotulo, disabled, onExportar }) => {
  const [ancora, setAncora] = useState<HTMLElement | null>(null);

  return (
    <>
      <ActionButton
        title={rotulo}
        icon={<FileDownloadIcon />}
        color="primary"
        variant="outlined"
        disabled={disabled}
        onClick={(e) => setAncora(e.currentTarget)}
      />
      <Menu anchorEl={ancora} open={Boolean(ancora)} onClose={() => setAncora(null)}>
        {OPCOES.map(({ rotulo: nome, opcoes }) => (
          <MenuItem key={nome} onClick={() => { setAncora(null); onExportar(opcoes); }}>
            <ListItemIcon>
              {opcoes.formato === 'PDF' ? <PictureAsPdfIcon fontSize="small" color="error" />
                : <DescriptionIcon fontSize="small" color="primary" />}
            </ListItemIcon>
            <ListItemText>{nome}</ListItemText>
          </MenuItem>
        ))}
      </Menu>
    </>
  );
};

export default AtaExportMenu;
