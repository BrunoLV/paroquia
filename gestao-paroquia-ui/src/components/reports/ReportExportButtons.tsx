import React from 'react';
import {Stack} from '@mui/material';
import DescriptionIcon from '@mui/icons-material/Description';
import FileDownloadIcon from '@mui/icons-material/FileDownload';
import PictureAsPdfIcon from '@mui/icons-material/PictureAsPdf';
import ActionButton from '../ActionButton';

interface ReportExportButtonsProps {
  onExportCSV: () => void;
  onExportPDF: () => void;
  onExportWord?: () => void;
  isExporting?: boolean;
}

const ReportExportButtons: React.FC<ReportExportButtonsProps> = ({
  onExportCSV,
  onExportPDF,
  onExportWord,
  isExporting = false,
}) => {
  return (
    <Stack direction="row" spacing={1.5} sx={{ alignItems: 'center' }}>
      <ActionButton
        title="Exportar CSV"
        icon={<FileDownloadIcon />}
        color="info"
        size="small"
        disabled={isExporting}
        onClick={onExportCSV}
      />

      <ActionButton
        title="Exportar PDF"
        icon={<PictureAsPdfIcon />}
        color="info"
        size="small"
        disabled={isExporting}
        onClick={onExportPDF}
      />

      {onExportWord && (
        <ActionButton
          title="Exportar Word"
          icon={<DescriptionIcon />}
          color="info"
          size="small"
          disabled={isExporting}
          onClick={onExportWord}
        />
      )}
    </Stack>
  );
};

export default ReportExportButtons;
