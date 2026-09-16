import React, {useState} from 'react';
import {Snackbar} from '@mui/material';
import ContentCopyIcon from '@mui/icons-material/ContentCopy';
import {WeeklyEvent} from '@/api/types';
import {formatarAgendaFimDeSemana} from '@/utils/agendaFimDeSemana';
import ActionButton from '@/components/ActionButton';

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
      <ActionButton
        size="small"
        color="primary"
        title="Copiar agenda do fim de semana"
        icon={<ContentCopyIcon />}
        onClick={copiar}
      />
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
