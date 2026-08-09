import React from 'react';
import {Stack} from '@mui/material';
import FileDownloadIcon from '@mui/icons-material/FileDownload';
import PictureAsPdfIcon from '@mui/icons-material/PictureAsPdf';
import ActionButton from '../ActionButton';

interface ReportExportButtonsProps {
  onExportCSV: () => void;
  onExportPDF: () => void;
  isExporting?: boolean;
}

const ReportExportButtons: React.FC<ReportExportButtonsProps> = ({
  onExportCSV,
  onExportPDF,
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
    </Stack>
  );
};

export default ReportExportButtons;
