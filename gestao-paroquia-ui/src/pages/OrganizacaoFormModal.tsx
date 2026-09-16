import React, {useEffect} from 'react';
import {Controller, useForm} from 'react-hook-form';
import {zodResolver} from '@hookform/resolvers/zod';
import {
    Card,
    CardContent,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    FormControl,
    FormHelperText,
    Grid,
    InputLabel,
    MenuItem,
    Select,
    Stack,
    TextField,
    Typography,
} from '@mui/material';
import {useQuery} from '@tanstack/react-query';
import CloseIcon from '@mui/icons-material/Close';
import ContactMailIcon from '@mui/icons-material/ContactMail';
import GroupsIcon from '@mui/icons-material/Groups';
import SaveIcon from '@mui/icons-material/Save';
import {DIALOG_CONTENT_SCROLL_SX} from '@/theme/dialogStyles';
import {confirmDiscard} from '@/utils/confirmDiscard';
import {Organizacao, PastoralMovimentoOficial} from '@/api/types';
import {Usuario} from '@/api/usuarios';
import {getOrganizacaoMembros} from '@/api/organizacoes';
import ActionButton from '@/components/ActionButton';
import {organizacaoSchema, OrganizacaoFormInput} from '@/schemas/organizacaoSchema';

interface OrganizacaoFormModalProps {
  open: boolean;
  onClose: () => void;
  onSave: (data: Omit<Organizacao, 'id'>) => void;
  organizacao?: Organizacao | null;
  isPending: boolean;
  usuarios?: Usuario[];
  igrejas?: Organizacao[];
  organizacoes?: Organizacao[];
  pastoraisOficiais?: PastoralMovimentoOficial[];
}

const defaultValues: OrganizacaoFormInput = {
  nome: '',
  tipo: 'PASTORAL',
  contato: '',
  coordenador: '',
  viceCoordenador: '',
  parentId: '',
  pastoralOficialId: '',
};

/**
 * Modal de cadastro/edição de organismos paroquiais (Pastoral, Movimento,
 * Clero, Matriz, Comunidade, Conselhos). Validação formal via Zod +
 * React Hook Form, espelhando `organizacaoSchema` (mesmas regras antes
 * aplicadas manualmente em `handleSubmit`).
 */
const OrganizacaoFormModal: React.FC<OrganizacaoFormModalProps> = ({
  open,
  onClose,
  onSave,
  organizacao,
  isPending,
  usuarios = [],
  igrejas = [],
  organizacoes = [],
  pastoraisOficiais = [],
}) => {
  const {
    control,
    register,
    handleSubmit,
    watch,
    reset,
    formState: { errors, isDirty },
  } = useForm<OrganizacaoFormInput>({
    resolver: zodResolver(organizacaoSchema),
    defaultValues,
  });

  useEffect(() => {
    if (organizacao) {
      reset({
        nome: organizacao.nome,
        tipo: (organizacao.tipo || 'PASTORAL') as OrganizacaoFormInput['tipo'],
        contato: organizacao.contato || '',
        coordenador: organizacao.coordenador || '',
        viceCoordenador: organizacao.viceCoordenador || '',
        parentId: organizacao.parentId || '',
        pastoralOficialId: organizacao.pastoralOficialId || '',
      });
    } else {
      reset(defaultValues);
    }
  }, [organizacao, open, reset]);

  const tipo = watch('tipo');
  const parentId = watch('parentId');

  const getSelectedChurchId = () => {
    if (!parentId) return null;
    const parentOrg = organizacoes.find(o => o.id === parentId);
    if (!parentOrg) return null;
    if (parentOrg.tipo === 'MATRIZ' || parentOrg.tipo === 'COMUNIDADE') {
      return parentOrg.id;
    }
    if (parentOrg.parentId) {
      const grandParentOrg = organizacoes.find(o => o.id === parentOrg.parentId);
      if (grandParentOrg && (grandParentOrg.tipo === 'MATRIZ' || grandParentOrg.tipo === 'COMUNIDADE')) {
        return grandParentOrg.id;
      }
    }
    return null;
  };

  const churchId = getSelectedChurchId();

  const { data: churchMembers = [] } = useQuery({
    queryKey: ['organizacaoMembros', churchId],
    queryFn: () => getOrganizacaoMembros(churchId!),
    enabled: !!churchId,
  });

  const getCoordinatorOptions = () => {
    if (churchId) {
      if (churchMembers.length > 0) {
        return churchMembers.map(m => ({
          id: m.usuarioNome,
          nome: m.usuarioNome
        }));
      }
      const filteredByChurch = usuarios.filter(u => !u.igrejaId || u.igrejaId === churchId);
      return filteredByChurch.map(u => ({
        id: u.nome,
        nome: u.nome
      }));
    }
    return usuarios.map(u => ({
      id: u.nome,
      nome: u.nome
    }));
  };

  const coordinatorOptions = getCoordinatorOptions();

  const handleClose = () => confirmDiscard(isDirty, onClose);

  const onSubmit = (data: OrganizacaoFormInput) => {
    onSave({
      nome: data.nome.trim(),
      tipo: data.tipo,
      contato: data.contato?.trim() || null,
      coordenador: data.coordenador?.trim() || null,
      viceCoordenador: data.viceCoordenador?.trim() || null,
      ativo: organizacao ? organizacao.ativo : true,
      parentId: (data.tipo === 'CLERO') ? null : (data.parentId || null),
      pastoralOficialId: (data.tipo === 'PASTORAL' || data.tipo === 'MOVIMENTO') ? data.pastoralOficialId || null : null,
    });
  };

  const parentOptions = organizacoes
    .filter(org => org.ativo)
    .filter(org => !organizacao || org.id !== organizacao.id)
    .filter(org => {
      if (tipo === 'MATRIZ' || tipo === 'COMUNIDADE') {
        return org.tipo === 'CLERO';
      }
      if (tipo === 'CONSELHO_PASTORAL' || tipo === 'CONSELHO_ECONOMICO') {
        return org.tipo === 'MATRIZ' || org.tipo === 'COMUNIDADE';
      }
      if (tipo === 'PASTORAL' || tipo === 'MOVIMENTO') {
        return org.tipo === 'CONSELHO_PASTORAL';
      }
      return true;
    });

  return (
    <Dialog
      open={open}
      onClose={handleClose}
      fullWidth
      maxWidth="sm"
    >
      <form onSubmit={handleSubmit(onSubmit)} noValidate>
        <DialogTitle>
          {organizacao ? 'Editar Pastoral / Movimento' : 'Nova Pastoral / Movimento'}
        </DialogTitle>
        <DialogContent dividers sx={DIALOG_CONTENT_SCROLL_SX}>
          <Stack spacing={2.5}>
          <Card variant="outlined" sx={{ borderRadius: 2 }}>
            <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
              <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
                <GroupsIcon color="primary" fontSize="small" />
                <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>
                  Identificação do Organismo
                </Typography>
              </Stack>
            <TextField
              label="Nome"
              variant="outlined"
              fullWidth
              required
              size="small"
              error={!!errors.nome}
              helperText={errors.nome?.message}
              disabled={isPending}
              {...register('nome')}
              sx={{ mb: 2.5 }}
            />

            <Grid container spacing={2}>
              <Grid size={{ xs: (tipo === 'PASTORAL' || tipo === 'MOVIMENTO') ? 6 : 12 }}>
                <FormControl fullWidth required size="small">
                  <InputLabel id="tipo-org-label">Tipo</InputLabel>
                  <Controller
                    name="tipo"
                    control={control}
                    render={({ field }) => (
                      <Select
                        labelId="tipo-org-label"
                        label="Tipo"
                        disabled={isPending}
                        {...field}
                      >
                        <MenuItem value="PASTORAL">Pastoral</MenuItem>
                        <MenuItem value="MOVIMENTO">Movimento</MenuItem>
                        <MenuItem value="CLERO">Clero</MenuItem>
                        <MenuItem value="MATRIZ">Matriz</MenuItem>
                        <MenuItem value="COMUNIDADE">Comunidade</MenuItem>
                        <MenuItem value="CONSELHO_PASTORAL">Conselho Pastoral Paroquial (CPP)</MenuItem>
                        <MenuItem value="CONSELHO_ECONOMICO">Conselho Econômico Paroquial (CAE)</MenuItem>
                        <MenuItem value="OUTRO">Outro</MenuItem>
                      </Select>
                    )}
                  />
                </FormControl>
              </Grid>

              {(tipo === 'PASTORAL' || tipo === 'MOVIMENTO') && (
                <Grid size={{ xs: 6 }}>
                  <FormControl fullWidth required size="small" error={!!errors.pastoralOficialId}>
                    <InputLabel id="pastoral-oficial-label">Tipo Oficial / Movimento</InputLabel>
                    <Controller
                      name="pastoralOficialId"
                      control={control}
                      render={({ field }) => (
                        <Select
                          labelId="pastoral-oficial-label"
                          label="Tipo Oficial / Movimento"
                          disabled={isPending}
                          {...field}
                          value={field.value ?? ''}
                        >
                          <MenuItem value=""><em>Selecione...</em></MenuItem>
                          {pastoraisOficiais
                            .filter(po => po.tipo === tipo)
                            .map(po => (
                              <MenuItem key={po.id} value={po.id}>{po.nome}</MenuItem>
                            ))}
                        </Select>
                      )}
                    />
                    {errors.pastoralOficialId && (
                      <FormHelperText error>
                        {errors.pastoralOficialId.message}
                      </FormHelperText>
                    )}
                  </FormControl>
                </Grid>
              )}
            </Grid>

            {tipo !== 'CLERO' && (
              <FormControl fullWidth size="small">
                <InputLabel id="parent-org-label">Organização Pai</InputLabel>
                <Controller
                  name="parentId"
                  control={control}
                  render={({ field }) => (
                    <Select
                      labelId="parent-org-label"
                      label="Organização Pai"
                      disabled={isPending}
                      {...field}
                      value={field.value ?? ''}
                    >
                      <MenuItem value=""><em>Nenhuma</em></MenuItem>
                      {parentOptions.map(org => (
                        <MenuItem key={org.id} value={org.id}>{org.nome}</MenuItem>
                      ))}
                    </Select>
                  )}
                />
              </FormControl>
            )}
            </CardContent>
          </Card>

          <Card variant="outlined" sx={{ borderRadius: 2 }}>
            <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
              <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
                <ContactMailIcon color="primary" fontSize="small" />
                <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>
                  Contato e Coordenação
                </Typography>
              </Stack>

            <TextField
              label="Contato (E-mail/Tel)"
              variant="outlined"
              fullWidth
              size="small"
              disabled={isPending}
              {...register('contato')}
              sx={{ mb: 2.5 }}
            />

            <Grid container spacing={2}>
              <Grid size={{ xs: 6 }}>
                <FormControl fullWidth size="small">
                  <InputLabel id="coordenador-select-label">Coordenador</InputLabel>
                  <Controller
                    name="coordenador"
                    control={control}
                    render={({ field }) => (
                      <Select
                        labelId="coordenador-select-label"
                        label="Coordenador"
                        disabled={isPending}
                        {...field}
                        value={field.value ?? ''}
                      >
                        <MenuItem value=""><em>Nenhum</em></MenuItem>
                        {coordinatorOptions.map(opt => (
                          <MenuItem key={opt.id} value={opt.nome}>{opt.nome}</MenuItem>
                        ))}
                      </Select>
                    )}
                  />
                </FormControl>
              </Grid>

              <Grid size={{ xs: 6 }}>
                <FormControl fullWidth size="small">
                  <InputLabel id="vice-coordenador-select-label">Vice-Coordenador</InputLabel>
                  <Controller
                    name="viceCoordenador"
                    control={control}
                    render={({ field }) => (
                      <Select
                        labelId="vice-coordenador-select-label"
                        label="Vice-Coordenador"
                        disabled={isPending}
                        {...field}
                        value={field.value ?? ''}
                      >
                        <MenuItem value=""><em>Nenhum</em></MenuItem>
                        {coordinatorOptions.map(opt => (
                          <MenuItem key={opt.id} value={opt.nome}>{opt.nome}</MenuItem>
                        ))}
                      </Select>
                    )}
                  />
                </FormControl>
              </Grid>
            </Grid>
            </CardContent>
          </Card>
          </Stack>
        </DialogContent>
        <DialogActions>
          <ActionButton title="Cancelar" icon={<CloseIcon />} onClick={handleClose} disabled={isPending} color="secondary" />
          <ActionButton
            title={organizacao ? 'Salvar Alterações' : 'Salvar'}
            icon={<SaveIcon />}
            type="submit"
            color="primary"
            disabled={isPending}
          />
        </DialogActions>
      </form>
    </Dialog>
  );
};

export default OrganizacaoFormModal;
