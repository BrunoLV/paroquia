import SearchableSelect from '@/shared/presentation/components/SearchableSelect';
import React, {useEffect} from 'react';
import {Controller, useForm} from 'react-hook-form';
import {zodResolver} from '@hookform/resolvers/zod';
import { Alert, Card, CardContent, Dialog, DialogActions, DialogContent, DialogTitle, FormControl, FormHelperText, Grid, MenuItem, Stack, TextField, Typography } from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import ContactMailIcon from '@mui/icons-material/ContactMail';
import GroupsIcon from '@mui/icons-material/Groups';
import SaveIcon from '@mui/icons-material/Save';
import {DIALOG_CONTENT_SCROLL_SX} from '@/shared/presentation/theme/dialogStyles';
import {confirmDiscard} from '@/shared/utils/confirmDiscard';
import {
  Organizacao,
  PastoralMovimentoOficial,
  SETOR_PASTORAL_OPCOES,
  SUBTIPO_CONSELHO_LABELS,
} from '@/shared/domain/types/types';
import ActionButton from '@/shared/presentation/components/ActionButton';
import {organizacaoSchema, OrganizacaoFormInput} from '@/contextos/organizacao/presentation/schemas/organizacaoSchema';

interface OrganizacaoFormModalProps {
  open: boolean;
  onClose: () => void;
  onSave: (data: Omit<Organizacao, 'id'>) => void;
  organizacao?: Organizacao | null;
  isPending: boolean;
  igrejas?: Organizacao[];
  organizacoes?: Organizacao[];
  pastoraisOficiais?: PastoralMovimentoOficial[];
}

const defaultValues: OrganizacaoFormInput = {
  nome: '',
  tipo: 'PASTORAL',
  email: '',
  telefone: '',
  parentId: '',
  pastoralOficialId: '',
  setorPastoral: '',
  subtipoConselho: '',
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
        email: organizacao.email || (organizacao.contato?.includes('@') ? organizacao.contato : '') || '',
        telefone: organizacao.telefone || (organizacao.contato && !organizacao.contato.includes('@') ? organizacao.contato : '') || '',
        parentId: organizacao.parentId || '',
        pastoralOficialId: organizacao.pastoralOficialId || '',
        setorPastoral: organizacao.setorPastoral || '',
        subtipoConselho: organizacao.subtipoConselho || '',
      });
    } else {
      reset(defaultValues);
    }
  }, [organizacao, open, reset]);

  const tipo = watch('tipo');
  const exigeSetor = tipo === 'PASTORAL' || tipo === 'SERVICO_PAROQUIAL';

  const handleClose = () => confirmDiscard(isDirty, onClose);

  const onSubmit = (data: OrganizacaoFormInput) => {
    onSave({
      nome: data.nome.trim(),
      tipo: data.tipo,
      contato: null,
      email: data.email?.trim() || null,
      telefone: data.telefone?.trim() || null,
      ativo: organizacao ? organizacao.ativo : true,
      parentId: data.parentId || null,
      pastoralOficialId: (data.tipo === 'PASTORAL' || data.tipo === 'MOVIMENTO') ? data.pastoralOficialId || null : null,
      setorPastoral: exigeSetor ? data.setorPastoral || null : null,
      subtipoConselho: data.tipo === 'CONSELHO' ? data.subtipoConselho || null : null,
    });
  };

  /**
   * Conselho nasce em uma Matriz/Comunidade; pastoral, movimento e serviço articulam-se a um conselho
   * pastoral (CPP/CPC) — o CAEP é econômico e não acolhe organismos pastorais.
   */
  const parentOptions = organizacoes
    .filter(org => org.ativo)
    .filter(org => !organizacao || org.id !== organizacao.id)
    .filter(org => {
      if (tipo === 'CONSELHO') {
        return org.tipo === 'MATRIZ' || org.tipo === 'COMUNIDADE';
      }
      return org.tipo === 'CONSELHO' && org.subtipoConselho !== 'CAEP';
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
          {organizacao ? 'Editar Organismo Eclesial' : 'Novo Organismo Eclesial'}
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

            <Grid container spacing={2} sx={{ mb: 2.5 }}>
              <Grid size={{ xs: (tipo === 'PASTORAL' || tipo === 'MOVIMENTO') ? 6 : 12 }}>
                <FormControl fullWidth required size="small">

                  <Controller
                    name="tipo"
                    control={control}
                    render={({ field }) => (
                      <SearchableSelect
                        labelId="tipo-org-label"
                        label="Tipo"
                        disabled={isPending}
                        {...field}
                      >
                        <MenuItem value="PASTORAL">Pastoral Orgânica</MenuItem>
                        <MenuItem value="MOVIMENTO">Movimento Apostólico</MenuItem>
                        <MenuItem value="SERVICO_PAROQUIAL">Serviço Paroquial</MenuItem>
                        <MenuItem value="CONSELHO">Conselho</MenuItem>
                      </SearchableSelect>
                    )}
                  />
                </FormControl>
              </Grid>

              {(tipo === 'PASTORAL' || tipo === 'MOVIMENTO') && (
                <Grid size={{ xs: 6 }}>
                  <FormControl fullWidth required size="small" error={!!errors.pastoralOficialId}>

                    <Controller
                      name="pastoralOficialId"
                      control={control}
                      render={({ field }) => (
                        <SearchableSelect
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
                              <MenuItem
                                key={po.id}
                                value={po.id}
                                disabled={po.tipo === 'MOVIMENTO' && po.estatutoReconhecido === false}
                              >
                                {po.nome}
                                {po.tipo === 'MOVIMENTO' && po.estatutoReconhecido === false
                                  ? ' (sem estatuto reconhecido)'
                                  : ''}
                              </MenuItem>
                            ))}
                        </SearchableSelect>
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

            <Grid container spacing={2} sx={{ mb: (tipo === 'CONSELHO' || exigeSetor) ? 2.5 : 0 }}>
              {tipo === 'CONSELHO' && (
                <Grid size={{ xs: 12 }}>
                  <FormControl fullWidth required size="small" error={!!errors.subtipoConselho}>
                    <Controller
                      name="subtipoConselho"
                      control={control}
                      render={({ field }) => (
                        <SearchableSelect
                          labelId="subtipo-conselho-label"
                          label="Natureza do Conselho"
                          disabled={isPending}
                          {...field}
                          value={field.value ?? ''}
                        >
                          {Object.entries(SUBTIPO_CONSELHO_LABELS).map(([valor, rotulo]) => (
                            <MenuItem key={valor} value={valor}>{rotulo}</MenuItem>
                          ))}
                        </SearchableSelect>
                      )}
                    />
                    {errors.subtipoConselho && <FormHelperText error>{errors.subtipoConselho.message}</FormHelperText>}
                  </FormControl>
                </Grid>
              )}
              {exigeSetor && (
                <Grid size={{ xs: 12 }}>
                  <FormControl fullWidth required size="small" error={!!errors.setorPastoral}>
                    <Controller
                      name="setorPastoral"
                      control={control}
                      render={({ field }) => (
                        <SearchableSelect
                          labelId="setor-pastoral-label"
                          label="Múnus (Setor Pastoral)"
                          disabled={isPending}
                          {...field}
                          value={field.value ?? ''}
                        >
                          {Object.entries(SETOR_PASTORAL_OPCOES).map(([valor, rotulo]) => (
                            <MenuItem key={valor} value={valor}>{rotulo}</MenuItem>
                          ))}
                        </SearchableSelect>
                      )}
                    />
                    {errors.setorPastoral && <FormHelperText error>{errors.setorPastoral.message}</FormHelperText>}
                  </FormControl>
                </Grid>
              )}
            </Grid>

            {(
              <FormControl fullWidth required size="small" error={!!errors.parentId}>

                <Controller
                  name="parentId"
                  control={control}
                  render={({ field }) => (
                    <SearchableSelect
                      labelId="parent-org-label"
                      label={tipo === 'CONSELHO' ? 'Matriz / Comunidade' : 'Conselho Pastoral (CPP/CPC)'}
                      disabled={isPending}
                      {...field}
                      value={field.value ?? ''}
                    >
                      <MenuItem value=""><em>Selecione...</em></MenuItem>
                      {parentOptions.map(org => (
                        <MenuItem key={org.id} value={org.id}>{org.nome}</MenuItem>
                      ))}
                    </SearchableSelect>
                  )}
                />
                {errors.parentId && <FormHelperText error>{errors.parentId.message}</FormHelperText>}
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

            <Grid container spacing={2} sx={{ mb: 2.5 }}>
              <Grid size={{ xs: 12, sm: 6 }}>
                <TextField
                  label="E-mail"
                  type="email"
                  variant="outlined"
                  fullWidth
                  size="small"
                  disabled={isPending}
                  error={!!errors.email}
                  helperText={errors.email?.message}
                  {...register('email')}
                />
              </Grid>
              <Grid size={{ xs: 12, sm: 6 }}>
                <TextField
                  label="Telefone"
                  variant="outlined"
                  fullWidth
                  size="small"
                  placeholder="(11) 99999-9999"
                  disabled={isPending}
                  error={!!errors.telefone}
                  helperText={errors.telefone?.message}
                  {...register('telefone')}
                />
              </Grid>
            </Grid>

            <Alert severity="info" sx={{ mt: 0.5 }}>
              {organizacao?.coordenador
                ? `Coordenação atual: ${organizacao.coordenador}${organizacao.viceCoordenador ? ` e ${organizacao.viceCoordenador}` : ''}. `
                : ''}
              A coordenação é um mandato temporário proposto pelo grupo e homologado pelo Pároco; gerencie-o na ação
              &quot;Coordenação&quot; da lista.
            </Alert>
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
