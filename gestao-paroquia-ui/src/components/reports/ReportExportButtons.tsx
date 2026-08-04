import React from 'react';
import {Button, Stack} from '@mui/material';
import FileDownloadIcon from '@mui/icons-material/FileDownload';
import PictureAsPdfIcon from '@mui/icons-material/PictureAsPdf';

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
      <Button
        variant="contained"
        color="info"
        size="small"
        disabled={isExporting}
        onClick={onExportCSV}
        startIcon={<FileDownloadIcon />}
        sx={{ textTransform: 'none', fontWeight: 600, px: 2 }}
      >
        Exportar CSV
      </Button>

      <Button
        variant="outlined"
        color="info"
        size="small"
        disabled={isExporting}
        onClick={onExportPDF}
        startIcon={<PictureAsPdfIcon />}
        sx={{ textTransform: 'none', fontWeight: 600, px: 2 }}
      >
        Exportar PDF
      </Button>
    </Stack>
  );
};

export default ReportExportButtons;
