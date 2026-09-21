import React from 'react';
import { Alert, Dialog, DialogActions, DialogContent, DialogTitle, Grid, Stack } from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import SaveIcon from '@mui/icons-material/Save';
import { AnoParoquial, AtivoPatrimonial, Evento, Local, Organizacao, Projeto } from '@/shared/domain/types/types';
import ActionButton from '@/shared/presentation/components/ActionButton';
import { DIALOG_CONTENT_SCROLL_SX } from '@/shared/presentation/theme/dialogStyles';
import EventoDadosCard from './eventos/EventoDadosCard';
import EventoOrganizacaoCard from './eventos/EventoOrganizacaoCard';
import EventoLocalCard from './eventos/EventoLocalCard';
import EventoDataRecorrenciaCard from './eventos/EventoDataRecorrenciaCard';
import { useCadastroEventoForm } from '@/contextos/calendario/presentation/hooks/useCadastroEventoForm';

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
    isSecretarioUser,
    churches,
    filteredOrganizations,
    localOptions,
    projetoOptions,
    selectedProjectInfo,
    projectValidationError,
    isRecurring,
  } = useCadastroEventoForm({ open, onSubmit, organizations, locais, ativos, projetos, anosParoquiais, initialDate, initialEvent });

  return (
    <Dialog open={open} onClose={() => !isSubmitting && onClose()} maxWidth="lg" fullWidth>
      <form onSubmit={handleFormSubmit} noValidate>
        <DialogTitle sx={{ py: 2.5, px: { xs: 2.5, sm: 3.5 }, fontWeight: 700 }}>
          {initialEvent ? 'Editar Evento' : 'Criar Novo Evento'}
        </DialogTitle>
        <DialogContent dividers sx={{ ...DIALOG_CONTENT_SCROLL_SX, p: { xs: 2.5, sm: 3.5 } }}>
          {(isClosedYear || isSecretarioUser) && (
            <Stack spacing={1.5} sx={{ mb: 3 }}>
              {isClosedYear && (
                <Alert severity="warning">
                  <strong>Ano Paroquial Fechado ({selectedYear}):</strong> Este ano administrativo está travado para auditoria. Inclusões serão registradas como <strong>ADICIONADO_EXTRA</strong> (requer justificativa obrigatória abaixo).
                </Alert>
              )}
              {isSecretarioUser && (
                <Alert severity="info">
                  <strong>Perfil de Secretário(a):</strong> Você possui escopo global paroquial. Suas solicitações entram em deliberação e exigem <strong>justificativa obrigatória</strong>.
                </Alert>
              )}
            </Stack>
          )}
          <Grid container spacing={3}>
            <Grid size={{ xs: 12, md: 6 }}>
              <Stack spacing={3}>
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
              </Stack>
            </Grid>
            <Grid size={{ xs: 12, md: 6 }}>
              <Stack spacing={3}>
                <EventoLocalCard form={form} disabled={isSubmitting} localOptions={localOptions} ativos={ativos} />
                <EventoDataRecorrenciaCard
                  form={form}
                  disabled={isSubmitting}
                  isRecurring={isRecurring}
                  isEditing={!!initialEvent}
                  isParoco={isParoco}
                  isSecretario={isSecretarioUser}
                  isClosedYear={isClosedYear}
                />
              </Stack>
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions sx={{ p: { xs: 2, sm: 2.5 }, gap: 1 }}>
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
