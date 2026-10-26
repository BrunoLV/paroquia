import React, { useEffect, useMemo, useState } from 'react';
import { FormProvider, useForm, useWatch } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Dialog, DialogActions, DialogContent, DialogTitle, Stack } from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import SaveIcon from '@mui/icons-material/Save';
import ActionButton from '@/shared/presentation/components/ActionButton';
import { DIALOG_CONTENT_SCROLL_SX } from '@/shared/presentation/theme/dialogStyles';
import { confirmDiscard } from '@/shared/utils/confirmDiscard';
import { Organizacao } from '@/shared/domain/types/types';
import { Ata, TipoReuniaoInfo } from '@/contextos/conselho/domain/ata';
import { useAtas, useCandidatosPresenca } from '@/contextos/conselho/presentation/hooks/useAtas';
import { AtaFormInput, ataParaFormulario, ataSchema, ataVazia } from '@/contextos/conselho/presentation/schemas/ataSchema';
import { Candidato, citadosNaAta, mesclarCandidatos, organismosDoConvocante } from '@/contextos/conselho/presentation/components/ataFormTipos';
import AtaAnteriorCard from '@/contextos/conselho/presentation/components/AtaAnteriorCard';
import AtaDadosCard from '@/contextos/conselho/presentation/components/AtaDadosCard';
import AtaItensCard from '@/contextos/conselho/presentation/components/AtaItensCard';
import AtaPresencaCard from '@/contextos/conselho/presentation/components/AtaPresencaCard';

interface AtaFormModalProps {
  open: boolean;
  /** Rascunho em edição, ou `null` para uma ata nova. */
  edicao: Ata | null;
  tipos: TipoReuniaoInfo[];
  organizacoes: Organizacao[];
  isPending: boolean;
  onClose: () => void;
  onSubmit: (dados: AtaFormInput, regras: TipoReuniaoInfo) => void;
}

/**
 * Redação da ata em rascunho. Tudo que depende do tipo de reunião (órgãos aceitos, presença nominal ou por total,
 * votação) vem das regras do servidor; o formulário só as apresenta. Gravar o rascunho não submete a ata: a
 * submissão é um passo separado, depois de conferir o conteúdo.
 */
export const AtaFormModal: React.FC<AtaFormModalProps> = ({ open, edicao, tipos, organizacoes, isPending, onClose, onSubmit }) => {
  const inicial = useMemo(() => (edicao ? ataParaFormulario(edicao) : ataVazia()), [edicao]);
  const metodos = useForm<AtaFormInput>({ resolver: zodResolver(ataSchema), defaultValues: inicial });
  const { control, handleSubmit, reset, formState: { isDirty } } = metodos;

  const [buscados, setBuscados] = useState<Candidato[]>([]);

  useEffect(() => {
    if (!open) return;
    reset(inicial);
    setBuscados([]);
  }, [open, inicial, reset]);

  const tipo = useWatch({ control, name: 'tipo' });
  const convocante = useWatch({ control, name: 'organizacaoConvocanteId' });
  const associada = useWatch({ control, name: 'organizacaoAssociadaId' });
  const regras = tipos.find((t) => t.codigo === tipo);
  const doOrgao = useCandidatosPresenca(
    regras?.exigeOrganizacaoAssociada ? [convocante, associada] : [convocante], organizacoes,
    organismosDoConvocante(organizacoes, regras, convocante),
  );
  const candidatos = mesclarCandidatos(doOrgao, [...citadosNaAta(edicao), ...buscados]);
  const { data: vizinhas } = useAtas({ organizacaoId: convocante || undefined, tamanho: 100 });

  const handleClose = () => confirmDiscard(isDirty, onClose);

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="md" fullWidth>
      <FormProvider {...metodos}>
        <form onSubmit={handleSubmit((dados) => regras && onSubmit(dados, regras))} noValidate>
          <DialogTitle>{edicao ? 'Editar rascunho da ata' : 'Nova ata de reunião'}</DialogTitle>
          <DialogContent dividers sx={DIALOG_CONTENT_SCROLL_SX}>
            <Stack spacing={2.5}>
              <AtaDadosCard tipos={tipos} regras={regras} organizacoes={organizacoes} edicao={edicao !== null} />
              <AtaPresencaCard regras={regras} candidatos={candidatos}
                onFielBuscado={(p) => setBuscados((atuais) => [...atuais, { id: p.id, nome: p.nome }])} />
              <AtaAnteriorCard atasDoOrgao={vizinhas?.itens ?? []} ataAtualId={edicao?.id ?? null} />
              <AtaItensCard permiteVotacao={regras?.permiteVotacao ?? false} candidatos={candidatos} />
            </Stack>
          </DialogContent>
          <DialogActions sx={{ px: 3, py: 2 }}>
            <ActionButton title="Cancelar" icon={<CloseIcon />} color="secondary" onClick={handleClose} disabled={isPending} />
            <ActionButton title="Salvar rascunho" icon={<SaveIcon />} type="submit" disabled={isPending || !regras} />
          </DialogActions>
        </form>
      </FormProvider>
    </Dialog>
  );
};

export default AtaFormModal;
