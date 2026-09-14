import React, { useEffect, useRef } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useQuery } from '@tanstack/react-query';
import {
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Stack,
} from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import SaveIcon from '@mui/icons-material/Save';
import { useAuth } from '@/auth/AuthContext';
import { isGlobalCreator } from '@/auth/permissions';
import { Local } from '@/api/types';
import { getOrganizacoes } from '@/api/organizacoes';
import ActionButton from '@/components/ActionButton';
import { DIALOG_CONTENT_SCROLL_SX } from '@/theme/dialogStyles';
import { confirmDiscard } from '@/utils/confirmDiscard';
import { LocalFormInput, localSchema } from '@/schemas/localSchema';
import { LocalInformacoesGeraisCard } from './LocalInformacoesGeraisCard';
import { LocalEnderecoCard } from './LocalEnderecoCard';

interface LocalFormModalProps {
  open: boolean;
  onClose: () => void;
  onSave: (data: Omit<Local, 'id'>) => void;
  local?: Local | null;
  isPending: boolean;
}

export const LocalFormModal: React.FC<LocalFormModalProps> = ({
  open,
  onClose,
  onSave,
  local,
  isPending,
}) => {
  const { user } = useAuth();
  const numeroInputRef = useRef<HTMLInputElement | null>(null);

  const { data: orgs = [] } = useQuery({
    queryKey: ['organizacoes'],
    queryFn: getOrganizacoes,
    enabled: open,
  });

  const getSelectableOrgs = () => {
    if (isGlobalCreator(user)) {
      return orgs;
    }
    if (user && Array.isArray(user.organizations)) {
      const userOrgIds = user.organizations.map((o: { id: string }) => o.id);
      return orgs.filter((o) => userOrgIds.includes(o.id));
    }
    return orgs;
  };

  const matrizComunidades = getSelectableOrgs().filter(
    (o) => (o.tipo === 'MATRIZ' || o.tipo === 'COMUNIDADE') && o.ativo !== false
  );

  const {
    control,
    handleSubmit,
    setValue,
    reset,
    formState: { errors, isDirty },
  } = useForm<LocalFormInput>({
    resolver: zodResolver(localSchema),
    defaultValues: {
      nome: '',
      tipo: 'SALAO_PAROQUIAL',
      igrejaId: '',
      capacidadeMaxima: 50,
      descricao: '',
      cep: '',
      logradouro: '',
      numero: '',
      complemento: '',
      bairro: '',
      cidade: '',
      uf: 'SP',
      ativo: true,
    },
  });

  useEffect(() => {
    if (open) {
      if (local) {
        reset({
          nome: local.nome || '',
          tipo: local.tipo || 'SALAO_PAROQUIAL',
          igrejaId: local.igrejaId || (matrizComunidades[0]?.id || ''),
          capacidadeMaxima: local.capacidadeMaxima ?? 50,
          descricao: local.descricao || '',
          cep: local.cep || '',
          logradouro: local.logradouro || '',
          numero: local.numero || '',
          complemento: local.complemento || '',
          bairro: local.bairro || '',
          cidade: local.cidade || '',
          uf: local.uf || 'SP',
          ativo: local.ativo ?? true,
        });
      } else {
        reset({
          nome: '',
          tipo: 'SALAO_PAROQUIAL',
          igrejaId: matrizComunidades[0]?.id || '',
          capacidadeMaxima: 50,
          descricao: '',
          cep: '',
          logradouro: '',
          numero: '',
          complemento: '',
          bairro: '',
          cidade: '',
          uf: 'SP',
          ativo: true,
        });
      }
    }
  }, [open, local, reset, matrizComunidades.length]);

  const onSubmit = (formData: LocalFormInput) => {
    const payload: Omit<Local, 'id'> = {
      nome: formData.nome.trim(),
      tipo: formData.tipo,
      igrejaId: formData.igrejaId || local?.igrejaId || null,
      capacidadeMaxima: formData.capacidadeMaxima,
      descricao: formData.descricao?.trim() || null,
      cep: formData.cep ? formData.cep.trim() : null,
      logradouro: formData.logradouro ? formData.logradouro.trim() : null,
      numero: formData.numero ? formData.numero.trim() : null,
      complemento: formData.complemento?.trim() || null,
      bairro: formData.bairro ? formData.bairro.trim() : null,
      cidade: formData.cidade ? formData.cidade.trim() : null,
      uf: formData.uf ? formData.uf.trim().toUpperCase() : null,
      ativo: formData.ativo,
    };

    onSave(payload);
  };

  const handleClose = () => confirmDiscard(isDirty, onClose);

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="md" fullWidth>
      <form onSubmit={handleSubmit(onSubmit)} noValidate>
        <DialogTitle>
          {local ? 'Editar Local' : 'Novo Local'}
        </DialogTitle>

        <DialogContent dividers sx={DIALOG_CONTENT_SCROLL_SX}>
          <Stack spacing={2.5}>
            <LocalInformacoesGeraisCard
              control={control}
              errors={errors}
              igrejas={matrizComunidades}
            />

            <LocalEnderecoCard
              control={control}
              setValue={setValue}
              errors={errors}
              numeroInputRef={numeroInputRef}
            />
          </Stack>
        </DialogContent>

        <DialogActions sx={{ px: 3, py: 2 }}>
          <ActionButton
            title="Cancelar"
            icon={<CloseIcon />}
            onClick={handleClose}
            color="secondary"
          />
          <ActionButton
            type="submit"
            title={local ? 'Salvar Alterações' : 'Salvar'}
            icon={<SaveIcon />}
            color="primary"
            disabled={isPending}
          />
        </DialogActions>
      </form>
    </Dialog>
  );
};

export default LocalFormModal;
