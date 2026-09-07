import React, { useState } from 'react';
import {
  Card,
  CardContent,
  Checkbox,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Divider,
  FormControlLabel,
  Grid,
  MenuItem,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import BadgeIcon from '@mui/icons-material/Badge';
import ChurchIcon from '@mui/icons-material/Church';
import CloseIcon from '@mui/icons-material/Close';
import ContactMailIcon from '@mui/icons-material/ContactMail';
import ActionButton from '@/components/ActionButton';
import {
  CreateFielPayload,
  Endereco,
  Organizacao,
  TipoDocumentoEstrangeiro,
} from '@/api/types';
import {
  formatCep,
  formatCpf,
  formatDocumentoEstrangeiro,
  formatPhone,
  formatRg,
} from '@/utils/masks';
import { PAISES_ORIGEM, vazio } from './fielConstants';

interface FielCreateModalProps {
  open: boolean;
  unidades: Organizacao[];
  isPending?: boolean;
  onClose: () => void;
  onSave: (payload: CreateFielPayload) => void;
}

/**
 * Modal de cadastro de novo fiel paroquial.
 * Contempla dados civis, identificação estrangeira (RNE/RNM/Passaporte), contato,
 * endereço e vinculação comunitária.
 */
export const FielCreateModal: React.FC<FielCreateModalProps> = ({
  open,
  unidades,
  isPending,
  onClose,
  onSave,
}) => {
  const [form, setForm] = useState<CreateFielPayload>(vazio());
  const estrangeiro = form.paisOrigem !== 'BRA';
  const enderecoNovo =
    typeof form.endereco === 'object' && form.endereco ? form.endereco : {};

  const alterar = <K extends keyof CreateFielPayload>(
    campo: K,
    valor: CreateFielPayload[K],
  ) => {
    setForm((atual) => ({ ...atual, [campo]: valor }));
  };

  const alterarEnderecoNovo = (campo: keyof Endereco, valor: string) => {
    setForm((atual) => ({
      ...atual,
      endereco: {
        ...(typeof atual.endereco === 'object' && atual.endereco
          ? atual.endereco
          : {}),
        [campo]: valor,
      },
    }));
  };

  const podeSalvarNovo =
    Boolean(form.nome?.trim()) &&
    Boolean(form.dataNascimento) &&
    Boolean(form.email?.trim()) &&
    Boolean(form.celular?.trim()) &&
    Boolean(enderecoNovo.logradouro?.trim()) &&
    Boolean(enderecoNovo.bairro?.trim()) &&
    Boolean(enderecoNovo.cidade?.trim()) &&
    Boolean(enderecoNovo.estado?.trim()) &&
    Boolean(enderecoNovo.cep?.trim()) &&
    Boolean(form.unidadeTerritorialId) &&
    (estrangeiro
      ? Boolean(form.tipoDocumentoEstrangeiro) &&
        Boolean(form.numeroDocumentoEstrangeiro?.trim())
      : Boolean(form.cpf?.trim()));

  const handleSalvar = () => {
    onSave(form);
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>Novo fiel</DialogTitle>
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
                    value={form.nome}
                    onChange={(e) => alterar('nome', e.target.value)}
                  />
                </Grid>
                <Grid size={{ xs: 6, sm: 3 }}>
                  <TextField
                    select
                    required
                    fullWidth
                    size="small"
                    label="Sexo"
                    value={form.sexo}
                    onChange={(e) =>
                      alterar('sexo', e.target.value as CreateFielPayload['sexo'])
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
                    value={form.dataNascimento}
                    onChange={(e) => alterar('dataNascimento', e.target.value)}
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 4 }}>
                  <TextField
                    select
                    required
                    fullWidth
                    size="small"
                    label="País de origem"
                    value={form.paisOrigem}
                    onChange={(e) => {
                      const paisOrigem = e.target.value;
                      setForm((atual) => ({
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
                        value={form.tipoDocumentoEstrangeiro ?? ''}
                        onChange={(e) =>
                          alterar(
                            'tipoDocumentoEstrangeiro',
                            (e.target.value || undefined) as
                              | TipoDocumentoEstrangeiro
                              | undefined,
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
                          form.tipoDocumentoEstrangeiro === 'RNE_RNM'
                            ? 'A000000-A'
                            : 'Número do passaporte'
                        }
                        value={form.numeroDocumentoEstrangeiro ?? ''}
                        onChange={(e) =>
                          alterar(
                            'numeroDocumentoEstrangeiro',
                            formatDocumentoEstrangeiro(
                              form.tipoDocumentoEstrangeiro,
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
                        value={form.cpf}
                        onChange={(e) => alterar('cpf', formatCpf(e.target.value))}
                        helperText="Informe os 11 dígitos."
                      />
                    </Grid>
                    <Grid size={{ xs: 12, sm: 4 }}>
                      <TextField
                        fullWidth
                        size="small"
                        label="RG"
                        placeholder="00.000.000-0"
                        value={form.rg ?? ''}
                        onChange={(e) => alterar('rg', formatRg(e.target.value))}
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
                    value={form.email}
                    onChange={(e) => alterar('email', e.target.value)}
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 4 }}>
                  <TextField
                    required
                    fullWidth
                    size="small"
                    label="Celular"
                    placeholder="(00) 00000-0000"
                    value={form.celular}
                    onChange={(e) => alterar('celular', formatPhone(e.target.value))}
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 4 }}>
                  <TextField
                    fullWidth
                    size="small"
                    label="Telefone fixo"
                    placeholder="(00) 0000-0000"
                    value={form.telefoneFixo ?? ''}
                    onChange={(e) =>
                      alterar('telefoneFixo', formatPhone(e.target.value))
                    }
                  />
                </Grid>
                <Grid size={{ xs: 12 }}>
                  <FormControlLabel
                    control={
                      <Checkbox
                        size="small"
                        checked={form.isWhatsapp}
                        onChange={(e) => alterar('isWhatsapp', e.target.checked)}
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
                    value={enderecoNovo.logradouro ?? ''}
                    onChange={(e) =>
                      alterarEnderecoNovo('logradouro', e.target.value)
                    }
                  />
                </Grid>
                <Grid size={{ xs: 6, sm: 2 }}>
                  <TextField
                    fullWidth
                    size="small"
                    label="Número"
                    value={enderecoNovo.numero ?? ''}
                    onChange={(e) =>
                      alterarEnderecoNovo('numero', e.target.value)
                    }
                  />
                </Grid>
                <Grid size={{ xs: 6, sm: 4 }}>
                  <TextField
                    fullWidth
                    size="small"
                    label="Complemento"
                    value={enderecoNovo.complemento ?? ''}
                    onChange={(e) =>
                      alterarEnderecoNovo('complemento', e.target.value)
                    }
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 4 }}>
                  <TextField
                    required
                    fullWidth
                    size="small"
                    label="Bairro"
                    value={enderecoNovo.bairro ?? ''}
                    onChange={(e) =>
                      alterarEnderecoNovo('bairro', e.target.value)
                    }
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 4 }}>
                  <TextField
                    required
                    fullWidth
                    size="small"
                    label="Cidade"
                    value={enderecoNovo.cidade ?? ''}
                    onChange={(e) =>
                      alterarEnderecoNovo('cidade', e.target.value)
                    }
                  />
                </Grid>
                <Grid size={{ xs: 6, sm: 2 }}>
                  <TextField
                    required
                    fullWidth
                    size="small"
                    label="UF"
                    value={enderecoNovo.estado ?? ''}
                    onChange={(e) =>
                      alterarEnderecoNovo('estado', e.target.value)
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
                    value={enderecoNovo.cep ?? ''}
                    onChange={(e) =>
                      alterarEnderecoNovo('cep', formatCep(e.target.value))
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
                    value={form.unidadeTerritorialId}
                    onChange={(e) =>
                      alterar('unidadeTerritorialId', e.target.value)
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
                          checked={form.isDizimista}
                          onChange={(e) =>
                            alterar('isDizimista', e.target.checked)
                          }
                        />
                      }
                      label="Dizimista"
                    />
                    <FormControlLabel
                      control={
                        <Checkbox
                          size="small"
                          checked={form.permitirDivulgacaoAniversario}
                          onChange={(e) =>
                            alterar(
                              'permitirDivulgacaoAniversario',
                              e.target.checked,
                            )
                          }
                        />
                      }
                      label="Autoriza divulgar somente dia e mês do aniversário"
                    />
                  </Stack>
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
          title="Salvar"
          icon={<AddIcon />}
          color="primary"
          disabled={!podeSalvarNovo || isPending}
          onClick={handleSalvar}
        />
      </DialogActions>
    </Dialog>
  );
};
