import React, { useEffect, useState } from 'react';
import {
  Card,
  CardContent,
  Checkbox,
  Chip,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Divider,
  FormControlLabel,
  Grid,
  MenuItem,
  Paper,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import BadgeIcon from '@mui/icons-material/Badge';
import BlockIcon from '@mui/icons-material/Block';
import ChurchIcon from '@mui/icons-material/Church';
import CloseIcon from '@mui/icons-material/Close';
import ContactMailIcon from '@mui/icons-material/ContactMail';
import Diversity3Icon from '@mui/icons-material/Diversity3';
import EditIcon from '@mui/icons-material/Edit';
import FamilyRestroomIcon from '@mui/icons-material/FamilyRestroom';
import ActionButton from '@/components/ActionButton';
import {
  Endereco,
  Fiel,
  Organizacao,
  TipoDocumentoEstrangeiro,
  UpdateFielPayload,
} from '@/api/types';
import {
  RelacionamentoFamiliar,
  TipoRelacionamentoFamiliar,
} from '@/api/familiares';
import {
  formatCep,
  formatCpf,
  formatDocumentoEstrangeiro,
  formatPhone,
  formatRg,
} from '@/utils/masks';
import {
  PAISES_ORIGEM,
  TIPO_RELACAO_LABELS,
  cadastroVazio,
  paraCadastro,
} from './fielConstants';

interface FielEditModalProps {
  open: boolean;
  fiel: Fiel | null;
  unidades: Organizacao[];
  familiares: RelacionamentoFamiliar[];
  fieisDisponiveis: Fiel[];
  isPending?: boolean;
  isVinculoPending?: boolean;
  onClose: () => void;
  onSave: (payload: UpdateFielPayload) => void;
  onVincularFamiliar: (destinoId: string, tipo: TipoRelacionamentoFamiliar) => void;
  onRemoverFamiliar: (id: string) => void;
}

/**
 * Modal de edição cadastral do Fiel e gestão de vínculos familiares.
 */
export const FielEditModal: React.FC<FielEditModalProps> = ({
  open,
  fiel,
  unidades,
  familiares,
  fieisDisponiveis,
  isPending,
  isVinculoPending,
  onClose,
  onSave,
  onVincularFamiliar,
  onRemoverFamiliar,
}) => {
  const [cadastro, setCadastro] = useState<UpdateFielPayload>(cadastroVazio());
  const [destino, setDestino] = useState('');
  const [tipo, setTipo] = useState<TipoRelacionamentoFamiliar>('CONJUGE');

  useEffect(() => {
    if (fiel) {
      setCadastro(paraCadastro(fiel));
    } else {
      setCadastro(cadastroVazio());
    }
  }, [fiel, open]);

  if (!fiel) return null;

  const estrangeiro = cadastro.paisOrigem !== 'BRA';
  const endereco =
    typeof cadastro.endereco === 'object' && cadastro.endereco
      ? cadastro.endereco
      : {};

  const alterarCadastro = <K extends keyof UpdateFielPayload>(
    campo: K,
    valor: UpdateFielPayload[K],
  ) => {
    setCadastro((atual) => ({ ...atual, [campo]: valor }));
  };

  const alterarEndereco = (campo: keyof Endereco, valor: string) => {
    setCadastro((atual) => ({
      ...atual,
      endereco: {
        ...(typeof atual.endereco === 'object' && atual.endereco
          ? atual.endereco
          : {}),
        [campo]: valor,
      },
    }));
  };

  const handleVincular = () => {
    if (!destino) return;
    onVincularFamiliar(destino, tipo);
    setDestino('');
  };

  const fielId = fiel.fielId ?? fiel.id;

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>
        <Stack direction="row" sx={{ alignItems: 'center', justifyContent: 'space-between' }}>
          <Typography variant="h6" component="div">
            Editar fiel
          </Typography>
          <Chip
            size="small"
            label={(fiel.isAtivo ?? fiel.ativo) ? 'Ativo' : 'Inativo'}
            color={(fiel.isAtivo ?? fiel.ativo) ? 'success' : 'default'}
            variant="outlined"
          />
        </Stack>
      </DialogTitle>
      <DialogContent dividers>
        <Stack spacing={2.5}>
          {/* Dados Civis */}
          <Card variant="outlined" sx={{ borderRadius: 2 }}>
            <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
              <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
                <BadgeIcon color="primary" fontSize="small" />
                <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>
                  Dados civis
                </Typography>
              </Stack>
              <Grid container spacing={2}>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <TextField
                    required
                    fullWidth
                    size="small"
                    label="Nome completo"
                    value={cadastro.nome ?? ''}
                    onChange={(e) => alterarCadastro('nome', e.target.value)}
                  />
                </Grid>
                <Grid size={{ xs: 6, sm: 3 }}>
                  <TextField
                    select
                    required
                    fullWidth
                    size="small"
                    label="Sexo"
                    value={cadastro.sexo ?? 'MASCULINO'}
                    onChange={(e) =>
                      alterarCadastro('sexo', e.target.value as UpdateFielPayload['sexo'])
                    }
                  >
                    <MenuItem value="MASCULINO">Masculino</MenuItem>
                    <MenuItem value="FEMININO">Feminino</MenuItem>
                    <MenuItem value="NAO_INFORMADO">Não informado</MenuItem>
                  </TextField>
                </Grid>
                <Grid size={{ xs: 6, sm: 3 }}>
                  <TextField
                    required
                    fullWidth
                    size="small"
                    type="date"
                    label="Nascimento"
                    slotProps={{ inputLabel: { shrink: true } }}
                    value={cadastro.dataNascimento ?? ''}
                    onChange={(e) => alterarCadastro('dataNascimento', e.target.value)}
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 4 }}>
                  <TextField
                    select
                    required
                    fullWidth
                    size="small"
                    label="País de origem"
                    value={cadastro.paisOrigem ?? 'BRA'}
                    onChange={(e) => {
                      const paisOrigem = e.target.value;
                      setCadastro((atual) => ({
                        ...atual,
                        paisOrigem,
                        cpf: paisOrigem === 'BRA' ? atual.cpf : '',
                        rg: paisOrigem === 'BRA' ? atual.rg : '',
                        tipoDocumentoEstrangeiro:
                          paisOrigem === 'BRA' ? undefined : atual.tipoDocumentoEstrangeiro,
                        numeroDocumentoEstrangeiro:
                          paisOrigem === 'BRA' ? '' : atual.numeroDocumentoEstrangeiro,
                      }));
                    }}
                  >
                    {PAISES_ORIGEM.map((pais) => (
                      <MenuItem key={pais.codigo} value={pais.codigo}>
                        {pais.nome}
                      </MenuItem>
                    ))}
                  </TextField>
                </Grid>
                {estrangeiro ? (
                  <>
                    <Grid size={{ xs: 12, sm: 4 }}>
                      <TextField
                        select
                        required
                        fullWidth
                        size="small"
                        label="Documento estrangeiro"
                        value={cadastro.tipoDocumentoEstrangeiro ?? ''}
                        onChange={(e) =>
                          alterarCadastro(
                            'tipoDocumentoEstrangeiro',
                            (e.target.value || undefined) as TipoDocumentoEstrangeiro | undefined,
                          )
                        }
                      >
                        <MenuItem value="RNE_RNM">RNE / RNM</MenuItem>
                        <MenuItem value="PASSAPORTE">Passaporte</MenuItem>
                      </TextField>
                    </Grid>
                    <Grid size={{ xs: 12, sm: 4 }}>
                      <TextField
                        required
                        fullWidth
                        size="small"
                        label="Número do documento"
                        placeholder={
                          cadastro.tipoDocumentoEstrangeiro === 'RNE_RNM'
                            ? 'A000000-A'
                            : 'Número do passaporte'
                        }
                        value={cadastro.numeroDocumentoEstrangeiro ?? ''}
                        onChange={(e) =>
                          alterarCadastro(
                            'numeroDocumentoEstrangeiro',
                            formatDocumentoEstrangeiro(
                              cadastro.tipoDocumentoEstrangeiro,
                              e.target.value,
                            ),
                          )
                        }
                      />
                    </Grid>
                  </>
                ) : (
                  <>
                    <Grid size={{ xs: 12, sm: 4 }}>
                      <TextField
                        required
                        fullWidth
                        size="small"
                        label="CPF"
                        placeholder="000.000.000-00"
                        value={cadastro.cpf ?? ''}
                        onChange={(e) =>
                          alterarCadastro('cpf', formatCpf(e.target.value))
                        }
                      />
                    </Grid>
                    <Grid size={{ xs: 12, sm: 4 }}>
                      <TextField
                        fullWidth
                        size="small"
                        label="RG"
                        placeholder="00.000.000-0"
                        value={cadastro.rg ?? ''}
                        onChange={(e) =>
                          alterarCadastro('rg', formatRg(e.target.value))
                        }
                      />
                    </Grid>
                  </>
                )}
              </Grid>
            </CardContent>
          </Card>

          {/* Contato e Endereço */}
          <Card variant="outlined" sx={{ borderRadius: 2 }}>
            <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
              <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
                <ContactMailIcon color="primary" fontSize="small" />
                <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>
                  Contato e endereço
                </Typography>
              </Stack>
              <Grid container spacing={2}>
                <Grid size={{ xs: 12, sm: 4 }}>
                  <TextField
                    required
                    fullWidth
                    size="small"
                    type="email"
                    label="E-mail"
                    value={cadastro.email ?? ''}
                    onChange={(e) => alterarCadastro('email', e.target.value)}
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 4 }}>
                  <TextField
                    required
                    fullWidth
                    size="small"
                    label="Celular"
                    placeholder="(00) 00000-0000"
                    value={cadastro.celular ?? ''}
                    onChange={(e) =>
                      alterarCadastro('celular', formatPhone(e.target.value))
                    }
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 4 }}>
                  <TextField
                    fullWidth
                    size="small"
                    label="Telefone fixo"
                    placeholder="(00) 0000-0000"
                    value={cadastro.telefoneFixo ?? ''}
                    onChange={(e) =>
                      alterarCadastro('telefoneFixo', formatPhone(e.target.value))
                    }
                  />
                </Grid>
                <Grid size={{ xs: 12 }}>
                  <FormControlLabel
                    control={
                      <Checkbox
                        size="small"
                        checked={cadastro.isWhatsapp ?? true}
                        onChange={(e) =>
                          alterarCadastro('isWhatsapp', e.target.checked)
                        }
                      />
                    }
                    label="Este celular possui WhatsApp"
                  />
                </Grid>
                <Grid size={{ xs: 12 }}>
                  <Divider sx={{ my: 0.5 }} />
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <TextField
                    required
                    fullWidth
                    size="small"
                    label="Logradouro"
                    value={endereco.logradouro ?? ''}
                    onChange={(e) =>
                      alterarEndereco('logradouro', e.target.value)
                    }
                  />
                </Grid>
                <Grid size={{ xs: 6, sm: 2 }}>
                  <TextField
                    fullWidth
                    size="small"
                    label="Número"
                    value={endereco.numero ?? ''}
                    onChange={(e) => alterarEndereco('numero', e.target.value)}
                  />
                </Grid>
                <Grid size={{ xs: 6, sm: 4 }}>
                  <TextField
                    fullWidth
                    size="small"
                    label="Complemento"
                    value={endereco.complemento ?? ''}
                    onChange={(e) =>
                      alterarEndereco('complemento', e.target.value)
                    }
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 4 }}>
                  <TextField
                    required
                    fullWidth
                    size="small"
                    label="Bairro"
                    value={endereco.bairro ?? ''}
                    onChange={(e) => alterarEndereco('bairro', e.target.value)}
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 4 }}>
                  <TextField
                    required
                    fullWidth
                    size="small"
                    label="Cidade"
                    value={endereco.cidade ?? ''}
                    onChange={(e) => alterarEndereco('cidade', e.target.value)}
                  />
                </Grid>
                <Grid size={{ xs: 6, sm: 2 }}>
                  <TextField
                    required
                    fullWidth
                    size="small"
                    label="UF"
                    slotProps={{ htmlInput: { maxLength: 2 } }}
                    value={endereco.estado ?? ''}
                    onChange={(e) =>
                      alterarEndereco('estado', e.target.value.toUpperCase())
                    }
                  />
                </Grid>
                <Grid size={{ xs: 6, sm: 2 }}>
                  <TextField
                    required
                    fullWidth
                    size="small"
                    label="CEP"
                    placeholder="00000-000"
                    value={formatCep(endereco.cep ?? '')}
                    onChange={(e) =>
                      alterarEndereco('cep', formatCep(e.target.value))
                    }
                  />
                </Grid>
              </Grid>
            </CardContent>
          </Card>

          {/* Vínculos Comunitários e Pastorais */}
          <Card variant="outlined" sx={{ borderRadius: 2 }}>
            <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
              <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
                <ChurchIcon color="primary" fontSize="small" />
                <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>
                  Comunidade e vínculos pastorais
                </Typography>
              </Stack>
              <Grid container spacing={2}>
                <Grid size={{ xs: 12 }}>
                  <TextField
                    select
                    required
                    fullWidth
                    size="small"
                    label="Igreja que frequenta"
                    value={cadastro.unidadeTerritorialId ?? ''}
                    onChange={(e) =>
                      alterarCadastro('unidadeTerritorialId', e.target.value)
                    }
                  >
                    {unidades
                      .filter(
                        (u) => u.tipo === 'MATRIZ' || u.tipo === 'COMUNIDADE',
                      )
                      .map((u) => (
                        <MenuItem key={u.id} value={u.id}>
                          {u.nome}
                        </MenuItem>
                      ))}
                  </TextField>
                </Grid>
                <Grid size={{ xs: 12 }}>
                  <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
                    <FormControlLabel
                      control={
                        <Checkbox
                          size="small"
                          checked={cadastro.isDizimista ?? false}
                          onChange={(e) =>
                            alterarCadastro('isDizimista', e.target.checked)
                          }
                        />
                      }
                      label="Dizimista"
                    />
                    <FormControlLabel
                      control={
                        <Checkbox
                          size="small"
                          checked={cadastro.permitirDivulgacaoAniversario ?? false}
                          onChange={(e) =>
                            alterarCadastro(
                              'permitirDivulgacaoAniversario',
                              e.target.checked,
                            )
                          }
                        />
                      }
                      label="Autoriza divulgar dia e mês do aniversário"
                    />
                  </Stack>
                </Grid>
              </Grid>
            </CardContent>
          </Card>

          {/* Vínculos Familiares */}
          <Card variant="outlined" sx={{ borderRadius: 2 }}>
            <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
              <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 1.5 }}>
                <FamilyRestroomIcon color="primary" fontSize="small" />
                <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>
                  Família
                </Typography>
              </Stack>

              {familiares.length === 0 ? (
                <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                  Nenhum vínculo familiar registrado.
                </Typography>
              ) : (
                <Grid container spacing={1.5} sx={{ mb: 2 }}>
                  {familiares.map((r) => (
                    <Grid size={{ xs: 12, sm: 6 }} key={r.id}>
                      <Paper
                        variant="outlined"
                        sx={{
                          p: 1.5,
                          borderRadius: 1.5,
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'space-between',
                        }}
                      >
                        <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
                          <Diversity3Icon fontSize="small" color="action" />
                          <Typography variant="body2" sx={{ fontWeight: 600 }}>
                            {r.nome}
                          </Typography>
                        </Stack>
                        <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
                          <Chip
                            size="small"
                            label={TIPO_RELACAO_LABELS[r.relacao] ?? r.relacao}
                            variant="outlined"
                          />
                          <ActionButton
                            size="small"
                            color="error"
                            title="Inativar vínculo"
                            icon={<BlockIcon />}
                            onClick={() => onRemoverFamiliar(r.id)}
                          />
                        </Stack>
                      </Paper>
                    </Grid>
                  ))}
                </Grid>
              )}

              <Divider sx={{ my: 1.5 }} />

              <Typography
                variant="caption"
                color="text.secondary"
                sx={{ mb: 1, display: 'block', fontWeight: 600 }}
              >
                Adicionar vínculo familiar
              </Typography>
              <Grid container spacing={2} sx={{ alignItems: 'center' }}>
                <Grid size={{ xs: 12, sm: 5 }}>
                  <TextField
                    select
                    fullWidth
                    size="small"
                    label="Fiel relacionado"
                    value={destino}
                    onChange={(e) => setDestino(e.target.value)}
                  >
                    {fieisDisponiveis
                      .filter((f) => (f.fielId ?? f.id) !== fielId)
                      .map((f) => (
                        <MenuItem key={f.fielId ?? f.id} value={f.fielId ?? f.id}>
                          {f.nome}
                        </MenuItem>
                      ))}
                  </TextField>
                </Grid>
                <Grid size={{ xs: 12, sm: 4 }}>
                  <TextField
                    select
                    fullWidth
                    size="small"
                    label="Relação"
                    value={tipo}
                    onChange={(e) =>
                      setTipo(e.target.value as TipoRelacionamentoFamiliar)
                    }
                  >
                    <MenuItem value="CONJUGE">Cônjuge</MenuItem>
                    <MenuItem value="GENITOR">Genitor(a)</MenuItem>
                    <MenuItem value="FILHO">Filho(a)</MenuItem>
                    <MenuItem value="IRMAO">Irmão(ã)</MenuItem>
                    <MenuItem value="OUTRO">Outro</MenuItem>
                  </TextField>
                </Grid>
                <Grid size={{ xs: 12, sm: 3 }}>
                  <ActionButton
                    fullWidth
                    color="primary"
                    title="Vincular"
                    icon={<AddIcon />}
                    disabled={!destino || isVinculoPending}
                    onClick={handleVincular}
                  />
                </Grid>
              </Grid>
            </CardContent>
          </Card>
        </Stack>
      </DialogContent>
      <DialogActions sx={{ px: 3, py: 2 }}>
        <ActionButton
          title="Cancelar"
          icon={<CloseIcon />}
          onClick={onClose}
          color="secondary"
        />
        <ActionButton
          color="primary"
          title="Salvar alterações"
          icon={<EditIcon />}
          disabled={isPending}
          onClick={() => onSave(cadastro)}
        />
      </DialogActions>
    </Dialog>
  );
};
