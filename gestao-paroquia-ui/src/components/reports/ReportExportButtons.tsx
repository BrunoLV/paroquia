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
        size="small"
        disabled={isExporting}
        onClick={onExportCSV}
        startIcon={<FileDownloadIcon />}
        sx={{
          borderRadius: 0,
          backgroundColor: '#1a237e',
          '&:hover': { backgroundColor: '#0d1757' },
          textTransform: 'none',
          fontWeight: 600,
          px: 2,
        }}
      >
        Exportar CSV
      </Button>

      <Button
        variant="outlined"
        size="small"
        disabled={isExporting}
        onClick={onExportPDF}
        startIcon={<PictureAsPdfIcon />}
        sx={{
          borderRadius: 0,
          color: '#1a237e',
          borderColor: '#1a237e',
          '&:hover': { borderColor: '#0d1757', backgroundColor: 'rgba(26,35,126,0.04)' },
          textTransform: 'none',
          fontWeight: 600,
          px: 2,
        }}
      >
        Exportar PDF
      </Button>
    </Stack>
  );
};

export default ReportExportButtons;
