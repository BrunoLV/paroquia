import SearchableSelect from '@/shared/presentation/components/SearchableSelect';
import React, { useEffect, useState } from 'react';
import { Controller, useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Box, Card, CardContent, Dialog, DialogActions, DialogContent, DialogTitle, FormControl, FormControlLabel, FormHelperText, Grid, MenuItem, Stack, Switch, Tab, Tabs, TextField, Typography } from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import SaveIcon from '@mui/icons-material/Save';
import ChurchIcon from '@mui/icons-material/Church';
import LocationOnIcon from '@mui/icons-material/LocationOn';
import BusinessIcon from '@mui/icons-material/Business';
import ShieldIcon from '@mui/icons-material/Shield';
import { DIALOG_CONTENT_SCROLL_SX } from '@/shared/presentation/theme/dialogStyles';
import { confirmDiscard } from '@/shared/utils/confirmDiscard';
import { UnidadeTerritorial, Clerigo } from '@/shared/domain/types/types';
import ActionButton from '@/shared/presentation/components/ActionButton';
import {
  unidadeTerritorialSchema,
  UnidadeTerritorialFormSchema,
  UnidadeTerritorialFormInput,
  UnidadeTerritorialFormOutput,
} from '@/contextos/organizacao/presentation/schemas/unidadeTerritorialSchema';

interface UnidadeTerritorialFormModalProps {
  open: boolean;
  onClose: () => void;
  onSave: (data: UnidadeTerritorialFormSchema) => void;
  unidade?: UnidadeTerritorial | null;
  isPending: boolean;
  matrizesDisponiveis?: UnidadeTerritorial[];
  clerigos?: Clerigo[];
}

const defaultValues: UnidadeTerritorialFormSchema = {
  nome: '',
  tipo: 'COMUNIDADE',
  ativo: true,
  parentId: null,
  padroeiro: '',
  dataFestaPadroeira: '',
  dataDedicacaoAltar: '',
  possuiSacrario: true,
  possuiPiaBatismal: false,
  clerigoResponsavelId: null,
  cnpj: '',
  inscricaoMunicipal: '',
  inscricaoImobiliariaIptu: '',
  processoImunidadeIptu: '',
  cartorioRgi: '',
  matriculaImovel: '',
  tipoAquisicao: '',
  areaTerrenoM2: null,
  areaConstruidaM2: null,
  avcbNumero: '',
  avcbValidade: '',
  lotacaoMaximaBombeiros: null,
  codigoConsumidorEnergia: '',
  codigoConsumidorAgua: '',
  telefone: '',
  email: '',
  enderecoCep: '',
  enderecoLogradouro: '',
  enderecoNumero: '',
  enderecoComplemento: '',
  enderecoBairro: '',
  enderecoCidade: '',
  enderecoUf: '',
};

export const UnidadeTerritorialFormModal: React.FC<UnidadeTerritorialFormModalProps> = ({
  open,
  onClose,
  onSave,
  unidade,
  isPending,
  matrizesDisponiveis = [],
  clerigos = [],
}) => {
  const [tabIndex, setTabIndex] = useState(0);

  const {
    control,
    register,
    handleSubmit,
    watch,
    setValue,
    reset,
    formState: { errors, isDirty },
  } = useForm<UnidadeTerritorialFormInput, any, UnidadeTerritorialFormOutput>({
    resolver: zodResolver(unidadeTerritorialSchema),
    defaultValues,
  });

  const tipoSelecionado = watch('tipo');

  useEffect(() => {
    if (tipoSelecionado === 'MATRIZ') {
      setValue('possuiSacrario', true);
      setValue('possuiPiaBatismal', true);
      setValue('parentId', null);
    }
  }, [tipoSelecionado, setValue]);

  useEffect(() => {
    if (unidade) {
      reset({
        nome: unidade.nome,
        tipo: unidade.tipo,
        ativo: unidade.ativo,
        parentId: unidade.parentId || null,
        padroeiro: unidade.padroeiro || '',
        dataFestaPadroeira: unidade.dataFestaPadroeira || '',
        dataDedicacaoAltar: unidade.dataDedicacaoAltar || '',
        possuiSacrario: unidade.possuiSacrario ?? true,
        possuiPiaBatismal: unidade.possuiPiaBatismal ?? false,
        clerigoResponsavelId: unidade.clerigoResponsavelId || null,
        cnpj: unidade.cnpj || '',
        inscricaoMunicipal: unidade.inscricaoMunicipal || '',
        inscricaoImobiliariaIptu: unidade.inscricaoImobiliariaIptu || '',
        processoImunidadeIptu: unidade.processoImunidadeIptu || '',
        cartorioRgi: unidade.cartorioRgi || '',
        matriculaImovel: unidade.matriculaImovel || '',
        tipoAquisicao: unidade.tipoAquisicao || '',
        areaTerrenoM2: unidade.areaTerrenoM2 || null,
        areaConstruidaM2: unidade.areaConstruidaM2 || null,
        avcbNumero: unidade.avcbNumero || '',
        avcbValidade: unidade.avcbValidade || '',
        lotacaoMaximaBombeiros: unidade.lotacaoMaximaBombeiros || null,
        codigoConsumidorEnergia: unidade.codigoConsumidorEnergia || '',
        codigoConsumidorAgua: unidade.codigoConsumidorAgua || '',
        telefone: unidade.telefone || '',
        email: unidade.email || '',
        enderecoCep: unidade.enderecoCep || '',
        enderecoLogradouro: unidade.enderecoLogradouro || '',
        enderecoNumero: unidade.enderecoNumero || '',
        enderecoComplemento: unidade.enderecoComplemento || '',
        enderecoBairro: unidade.enderecoBairro || '',
        enderecoCidade: unidade.enderecoCidade || '',
        enderecoUf: unidade.enderecoUf || '',
      });
    } else {
      reset(defaultValues);
    }
    setTabIndex(0);
  }, [unidade, open, reset]);

  const handleClose = () => confirmDiscard(isDirty, onClose);

  const onSubmit = (data: UnidadeTerritorialFormOutput) => {
    onSave(data);
  };

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="md" fullWidth>
      <DialogTitle>
        {unidade ? 'Editar unidade territorial' : 'Nova unidade territorial'}
      </DialogTitle>

      <Box sx={{ borderBottom: 1, borderColor: 'divider', px: 3 }}>
        <Tabs value={tabIndex} onChange={(_, val) => setTabIndex(val)} variant="scrollable" scrollButtons="auto">
          <Tab icon={<ChurchIcon />} iconPosition="start" label="Canônico" />
          <Tab icon={<LocationOnIcon />} iconPosition="start" label="Endereço & Contato" />
          <Tab icon={<BusinessIcon />} iconPosition="start" label="Fiscal & RGI" />
          <Tab icon={<ShieldIcon />} iconPosition="start" label="Segurança & Concessionárias" />
        </Tabs>
      </Box>

      <form onSubmit={handleSubmit(onSubmit)} noValidate>
        <DialogContent dividers sx={DIALOG_CONTENT_SCROLL_SX}>
          {/* Aba 0: Canônico */}
          {tabIndex === 0 && (
            <Stack spacing={2.5}>
              <Card variant="outlined" sx={{ borderRadius: 2 }}>
                <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
                  <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
                    <ChurchIcon color="primary" fontSize="small" />
                    <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>
                      Dimensão Canônica & Litúrgica
                    </Typography>
                  </Stack>
                  <Grid container spacing={2}>
                    <Grid size={{ xs: 12, sm: 8 }}>
                      <TextField
                      size="small"
                        {...register('nome')}
                        label="Nome da Unidade Territorial"
                        fullWidth
                        required
                        error={Boolean(errors.nome)}
                        helperText={errors.nome?.message}
                      />
                    </Grid>
                <Grid size={{ xs: 12, sm: 4 }}>
                  <Controller
                    name="tipo"
                    control={control}
                    render={({ field }) => (
                      <FormControl fullWidth size="small" error={Boolean(errors.tipo)}>

                        <SearchableSelect {...field} labelId="tipo-label" label="Tipo *">
                          <MenuItem value="MATRIZ">Igreja Matriz</MenuItem>
                          <MenuItem value="COMUNIDADE">Comunidade / Capela</MenuItem>
                        </SearchableSelect>
                        {errors.tipo && <FormHelperText>{errors.tipo.message}</FormHelperText>}
                      </FormControl>
                    )}
                  />
                </Grid>

                {tipoSelecionado === 'COMUNIDADE' && (
                  <Grid size={{ xs: 12 }}>
                    <Controller
                      name="parentId"
                      control={control}
                      render={({ field }) => (
                        <FormControl fullWidth size="small" error={Boolean(errors.parentId)}>

                          <SearchableSelect
                            {...field}
                            value={field.value || ''}
                            onChange={(e) => field.onChange(e.target.value || null)}
                            labelId="parent-label"
                            label="Igreja Matriz Responsável"
                          >
                            <MenuItem value="">Nenhuma / Independente</MenuItem>
                            {matrizesDisponiveis.map((m) => (
                              <MenuItem key={m.id} value={m.id}>
                                {m.nome}
                              </MenuItem>
                            ))}
                          </SearchableSelect>
                          <FormHelperText>Associação à paróquia sede / igreja matriz</FormHelperText>
                        </FormControl>
                      )}
                    />
                  </Grid>
                )}

                <Grid size={{ xs: 12, sm: 6 }}>
                  <TextField
                      size="small"
                    {...register('padroeiro')}
                    label="Padroeiro(a)"
                    fullWidth
                    error={Boolean(errors.padroeiro)}
                    helperText={errors.padroeiro?.message}
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <TextField
                      size="small"
                    {...register('dataFestaPadroeira')}
                    label="Festa Padroeira (MM-DD)"
                    placeholder="05-13"
                    fullWidth
                    error={Boolean(errors.dataFestaPadroeira)}
                    helperText={errors.dataFestaPadroeira?.message || 'Mês e dia da celebração patronal'}
                  />
                </Grid>

                <Grid size={{ xs: 12, sm: 6 }}>
                  <TextField
                      size="small"
                    {...register('dataDedicacaoAltar')}
                    label="Data de Dedicação do Altar"
                    type="date"
                    fullWidth
                    slotProps={{ inputLabel: { shrink: true } }}
                    error={Boolean(errors.dataDedicacaoAltar)}
                    helperText={errors.dataDedicacaoAltar?.message}
                  />
                </Grid>

                <Grid size={{ xs: 12, sm: 6 }}>
                  <Controller
                    name="clerigoResponsavelId"
                    control={control}
                    render={({ field }) => (
                      <FormControl fullWidth size="small">

                        <SearchableSelect
                          {...field}
                          value={field.value || ''}
                          onChange={(e) => field.onChange(e.target.value || null)}
                          labelId="clerigo-label"
                          label="Clérigo Responsável"
                        >
                          <MenuItem value="">Nenhum designado</MenuItem>
                          {clerigos.map((c) => (
                            <MenuItem key={c.clerigoId} value={c.clerigoId}>
                              {c.nomeEclesiastico} ({c.grauOrdem})
                            </MenuItem>
                          ))}
                        </SearchableSelect>
                      </FormControl>
                    )}
                  />
                </Grid>

                <Grid size={{ xs: 12, sm: 4 }}>
                  <Controller
                    name="possuiSacrario"
                    control={control}
                    render={({ field }) => (
                      <FormControlLabel
                        control={<Switch checked={field.value} onChange={(e) => field.onChange(e.target.checked)} disabled={tipoSelecionado === 'MATRIZ'} />}
                        label="Possui Sacrário"
                      />
                    )}
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 4 }}>
                  <Controller
                    name="possuiPiaBatismal"
                    control={control}
                    render={({ field }) => (
                      <FormControlLabel
                        control={<Switch checked={field.value} onChange={(e) => field.onChange(e.target.checked)} disabled={tipoSelecionado === 'MATRIZ'} />}
                        label="Possui Pia Batismal"
                      />
                    )}
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 4 }}>
                  <Controller
                    name="ativo"
                    control={control}
                    render={({ field }) => (
                      <FormControlLabel
                        control={<Switch checked={field.value} onChange={(e) => field.onChange(e.target.checked)} />}
                        label="Ativo no Sistema"
                      />
                    )}
                  />
                </Grid>
              </Grid>
            </CardContent>
          </Card>
        </Stack>
      )}

          {/* Aba 1: Endereço & Contato */}
          {tabIndex === 1 && (
            <Stack spacing={2.5}>
              <Card variant="outlined" sx={{ borderRadius: 2 }}>
                <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
                  <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
                    <LocationOnIcon color="primary" fontSize="small" />
                    <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>
                      Endereço & Contatos (Protegido por Criptografia - LGPD)
                    </Typography>
                  </Stack>
                  <Grid container spacing={2}>
                <Grid size={{ xs: 12, sm: 4 }}>
                  <TextField
                      size="small"
                    {...register('enderecoCep')}
                    label="CEP"
                    fullWidth
                    placeholder="00000-000"
                    error={Boolean(errors.enderecoCep)}
                    helperText={errors.enderecoCep?.message}
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 8 }}>
                  <TextField
                      size="small"
                    {...register('enderecoLogradouro')}
                    label="Logradouro / Rua"
                    fullWidth
                    error={Boolean(errors.enderecoLogradouro)}
                    helperText={errors.enderecoLogradouro?.message}
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 4 }}>
                  <TextField
                      size="small"
                    {...register('enderecoNumero')}
                    label="Número"
                    fullWidth
                    error={Boolean(errors.enderecoNumero)}
                    helperText={errors.enderecoNumero?.message}
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 8 }}>
                  <TextField
                      size="small"
                    {...register('enderecoComplemento')}
                    label="Complemento"
                    fullWidth
                    error={Boolean(errors.enderecoComplemento)}
                    helperText={errors.enderecoComplemento?.message}
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 4 }}>
                  <TextField
                      size="small"
                    {...register('enderecoBairro')}
                    label="Bairro"
                    fullWidth
                    error={Boolean(errors.enderecoBairro)}
                    helperText={errors.enderecoBairro?.message}
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 5 }}>
                  <TextField
                      size="small"
                    {...register('enderecoCidade')}
                    label="Cidade"
                    fullWidth
                    error={Boolean(errors.enderecoCidade)}
                    helperText={errors.enderecoCidade?.message}
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 3 }}>
                  <TextField
                      size="small"
                    {...register('enderecoUf')}
                    label="UF"
                    fullWidth
                    slotProps={{ htmlInput: { maxLength: 2 } }}
                    error={Boolean(errors.enderecoUf)}
                    helperText={errors.enderecoUf?.message}
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <TextField
                      size="small"
                    {...register('telefone')}
                    label="Telefone Institucional"
                    fullWidth
                    placeholder="(11) 99999-9999"
                    error={Boolean(errors.telefone)}
                    helperText={errors.telefone?.message}
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <TextField
                      size="small"
                    {...register('email')}
                    label="E-mail Institucional"
                    type="email"
                    fullWidth
                    error={Boolean(errors.email)}
                    helperText={errors.email?.message}
                  />
                </Grid>
              </Grid>
            </CardContent>
          </Card>
        </Stack>
      )}

          {/* Aba 2: Fiscal & RGI */}
          {tabIndex === 2 && (
            <Stack spacing={2.5}>
              <Card variant="outlined" sx={{ borderRadius: 2 }}>
                <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
                  <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
                    <BusinessIcon color="primary" fontSize="small" />
                    <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>
                      Identificação Fiscal & Registro de Imóveis (RGI)
                    </Typography>
                  </Stack>
                  <Grid container spacing={2}>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <TextField
                      size="small"
                    {...register('cnpj')}
                    label="CNPJ (Mitra/Filial)"
                    fullWidth
                    placeholder="00.000.000/0000-00"
                    error={Boolean(errors.cnpj)}
                    helperText={errors.cnpj?.message}
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <TextField
                      size="small"
                    {...register('inscricaoMunicipal')}
                    label="Inscrição Municipal"
                    fullWidth
                    error={Boolean(errors.inscricaoMunicipal)}
                    helperText={errors.inscricaoMunicipal?.message}
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <TextField
                      size="small"
                    {...register('inscricaoImobiliariaIptu')}
                    label="Inscrição Imobiliária (IPTU)"
                    fullWidth
                    error={Boolean(errors.inscricaoImobiliariaIptu)}
                    helperText={errors.inscricaoImobiliariaIptu?.message}
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <TextField
                      size="small"
                    {...register('processoImunidadeIptu')}
                    label="Nº Processo Imunidade Tributária"
                    fullWidth
                    error={Boolean(errors.processoImunidadeIptu)}
                    helperText={errors.processoImunidadeIptu?.message}
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <TextField
                      size="small"
                    {...register('cartorioRgi')}
                    label="Cartório de Registro de Imóveis (RGI)"
                    fullWidth
                    error={Boolean(errors.cartorioRgi)}
                    helperText={errors.cartorioRgi?.message}
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <TextField
                      size="small"
                    {...register('matriculaImovel')}
                    label="Matrícula do Imóvel"
                    fullWidth
                    error={Boolean(errors.matriculaImovel)}
                    helperText={errors.matriculaImovel?.message}
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 4 }}>
                  <TextField
                      size="small"
                    {...register('tipoAquisicao')}
                    label="Tipo de Aquisição (Doação/Compra)"
                    fullWidth
                    error={Boolean(errors.tipoAquisicao)}
                    helperText={errors.tipoAquisicao?.message}
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 4 }}>
                  <TextField
                      size="small"
                    {...register('areaTerrenoM2')}
                    label="Área do Terreno (m²)"
                    type="number"
                    fullWidth
                    error={Boolean(errors.areaTerrenoM2)}
                    helperText={errors.areaTerrenoM2?.message}
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 4 }}>
                  <TextField
                      size="small"
                    {...register('areaConstruidaM2')}
                    label="Área Construída (m²)"
                    type="number"
                    fullWidth
                    error={Boolean(errors.areaConstruidaM2)}
                    helperText={errors.areaConstruidaM2?.message}
                  />
                </Grid>
              </Grid>
            </CardContent>
          </Card>
        </Stack>
      )}

          {/* Aba 3: Segurança & Concessionárias */}
          {tabIndex === 3 && (
            <Stack spacing={2.5}>
              <Card variant="outlined" sx={{ borderRadius: 2 }}>
                <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
                  <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
                    <ShieldIcon color="primary" fontSize="small" />
                    <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>
                      Segurança (Bombeiros/AVCB) & Concessionárias de Serviços Públicos
                    </Typography>
                  </Stack>
                  <Grid container spacing={2}>
                <Grid size={{ xs: 12, sm: 4 }}>
                  <TextField
                      size="small"
                    {...register('avcbNumero')}
                    label="Nº do AVCB (Bombeiros)"
                    fullWidth
                    error={Boolean(errors.avcbNumero)}
                    helperText={errors.avcbNumero?.message}
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 4 }}>
                  <TextField
                      size="small"
                    {...register('avcbValidade')}
                    label="Validade do AVCB"
                    type="date"
                    fullWidth
                    slotProps={{ inputLabel: { shrink: true } }}
                    error={Boolean(errors.avcbValidade)}
                    helperText={errors.avcbValidade?.message}
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 4 }}>
                  <TextField
                      size="small"
                    {...register('lotacaoMaximaBombeiros')}
                    label="Lotação Máxima (Bombeiros)"
                    type="number"
                    fullWidth
                    error={Boolean(errors.lotacaoMaximaBombeiros)}
                    helperText={errors.lotacaoMaximaBombeiros?.message}
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <TextField
                      size="small"
                    {...register('codigoConsumidorEnergia')}
                    label="Código Consumidor Energia (CPFL/Enel)"
                    fullWidth
                    error={Boolean(errors.codigoConsumidorEnergia)}
                    helperText={errors.codigoConsumidorEnergia?.message}
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <TextField
                      size="small"
                    {...register('codigoConsumidorAgua')}
                    label="Código Consumidor Água (Sabesp)"
                    fullWidth
                    error={Boolean(errors.codigoConsumidorAgua)}
                    helperText={errors.codigoConsumidorAgua?.message}
                  />
                </Grid>
              </Grid>
            </CardContent>
          </Card>
        </Stack>
      )}
    </DialogContent>

        <DialogActions>
          <ActionButton title="Cancelar" icon={<CloseIcon />} onClick={handleClose} color="secondary" disabled={isPending} />
          <ActionButton type="submit" title="Salvar" icon={<SaveIcon />} color="primary" disabled={isPending} />
        </DialogActions>
      </form>
    </Dialog>
  );
};

export default UnidadeTerritorialFormModal;
