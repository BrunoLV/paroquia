import React, {useState} from 'react';
import {Button, Snackbar} from '@mui/material';
import ContentCopyIcon from '@mui/icons-material/ContentCopy';
import {WeeklyEvent} from '../../api/types';
import {formatarAgendaFimDeSemana} from '../../utils/agendaFimDeSemana';

interface ExportarAgendaButtonProps {
  eventosSemana?: WeeklyEvent[];
}

const ExportarAgendaButton: React.FC<ExportarAgendaButtonProps> = ({eventosSemana = []}) => {
  const [aviso, setAviso] = useState('');

  const copiar = async () => {
    const texto = formatarAgendaFimDeSemana(eventosSemana);
    try {
      await navigator.clipboard.writeText(texto);
      setAviso('Agenda do fim de semana copiada.');
    } catch {
      setAviso('Não foi possível copiar a agenda.');
    }
  };

  return (
    <>
      <Button size="small" variant="outlined" startIcon={<ContentCopyIcon />} onClick={copiar}>
        Copiar agenda do fim de semana
      </Button>
      <Snackbar
        open={!!aviso}
        autoHideDuration={3000}
        onClose={() => setAviso('')}
        message={aviso}
      />
    </>
  );
};

export default ExportarAgendaButton;
