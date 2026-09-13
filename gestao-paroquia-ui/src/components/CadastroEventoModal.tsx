import React from 'react';
import { Alert, Dialog, DialogActions, DialogContent, DialogTitle, Stack } from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import SaveIcon from '@mui/icons-material/Save';
import { AnoParoquial, AtivoPatrimonial, Evento, Local, Organizacao, Projeto } from '../api/types';
import ActionButton from './ActionButton';
import EventoDadosCard from './eventos/EventoDadosCard';
import EventoOrganizacaoCard from './eventos/EventoOrganizacaoCard';
import EventoLocalCard from './eventos/EventoLocalCard';
import EventoDataRecorrenciaCard from './eventos/EventoDataRecorrenciaCard';
import { useCadastroEventoForm } from '../hooks/useCadastroEventoForm';

interface CadastroEventoModalProps {
  open: boolean;
  onClose: () => void;
  onSubmit: (data: Record<string, unknown>) => void;
  isSubmitting?: boolean;
  organizations: Organizacao[];
  locais?: Local[];
  ativos?: AtivoPatrimonial[];
  projetos?: Projeto[];
  anosParoquiais?: AnoParoquial[];
  initialDate?: Date;
  initialEvent?: Evento | null;
}

/**
 * Diálogo de cadastro/edição de evento da agenda eclesial. Camada de renderização fina — toda
 * a orquestração (busca de dados derivados, opções filtradas de cada campo, validação Zod
 * estrutural, regras dependentes do ano paroquial/projeto vinculado e montagem do payload) vive
 * em `useCadastroEventoForm`.
 */
const CadastroEventoModal: React.FC<CadastroEventoModalProps> = ({
  open,
  onClose,
  onSubmit,
  isSubmitting = false,
  organizations = [],
  locais = [],
  ativos = [],
  projetos = [],
  anosParoquiais = [],
  initialDate,
  initialEvent,
}) => {
  const {
    form,
    handleFormSubmit,
    selectedYear,
    isClosedYear,
    isParoco,
    churches,
    filteredOrganizations,
    localOptions,
    projetoOptions,
    selectedProjectInfo,
    projectValidationError,
    isRecurring,
  } = useCadastroEventoForm({ open, onSubmit, organizations, locais, ativos, projetos, anosParoquiais, initialDate, initialEvent });

  return (
    <Dialog open={open} onClose={() => !isSubmitting && onClose()} maxWidth="md" fullWidth>
      <form onSubmit={handleFormSubmit} noValidate>
        <DialogTitle>{initialEvent ? 'Editar evento' : 'Novo evento'}</DialogTitle>
        <DialogContent dividers>
          <Stack spacing={2.5} sx={{ mt: 0.5 }}>
            {isClosedYear && (
              <Alert severity="warning">
                <strong>Ano Paroquial Fechado ({selectedYear}):</strong> Este ano administrativo está travado para auditoria. Inclusões serão registradas como <strong>ADICIONADO_EXTRA</strong> (requer justificativa obrigatória abaixo).
              </Alert>
            )}
            <EventoDadosCard form={form} disabled={isSubmitting} />
            <EventoOrganizacaoCard
              form={form}
              disabled={isSubmitting}
              organizations={organizations}
              filteredOrganizations={filteredOrganizations}
              churches={churches}
              projetoOptions={projetoOptions}
              selectedProjectInfo={selectedProjectInfo}
              projectValidationError={projectValidationError}
            />
            <EventoLocalCard form={form} disabled={isSubmitting} localOptions={localOptions} ativos={ativos} />
            <EventoDataRecorrenciaCard form={form} disabled={isSubmitting} isRecurring={isRecurring} isEditing={!!initialEvent} isParoco={isParoco} />
          </Stack>
        </DialogContent>
        <DialogActions>
          <ActionButton title="Cancelar" icon={<CloseIcon />} onClick={onClose} disabled={isSubmitting} color="secondary" />
          <ActionButton
            title={initialEvent ? 'Salvar Alterações' : 'Salvar Evento'}
            icon={<SaveIcon />}
            type="submit"
            color="primary"
            disabled={isSubmitting || !!projectValidationError}
          />
        </DialogActions>
      </form>
    </Dialog>
  );
};

export default CadastroEventoModal;
