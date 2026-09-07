import React, { useEffect, useState } from 'react';
import {
  Card,
  CardContent,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControl,
  FormControlLabel,
  Grid,
  InputLabel,
  MenuItem,
  Select,
  Stack,
  Switch,
  TextField,
  Typography,
} from '@mui/material';
import BadgeIcon from '@mui/icons-material/Badge';
import CalendarMonthIcon from '@mui/icons-material/CalendarMonth';
import CloseIcon from '@mui/icons-material/Close';
import ContactMailIcon from '@mui/icons-material/ContactMail';
import SaveIcon from '@mui/icons-material/Save';
import ActionButton from '@/components/ActionButton';
import type { Clerigo, ClerigoPayload, FuncaoEclesiastica, GrauOrdem } from '@/api/types';
import { formatCpf, formatPhone, formatRg } from '@/utils/masks';
import {
  FUNCOES_ECLESIASTICAS,
  GRAUS_ORDEM,
  clerigoParaPayload,
  estadoInicialForm,
} from './clerigoConstants';

interface ClerigoFormModalProps {
  open: boolean;
  isPending: boolean;
  initialData?: Clerigo | null;
  onClose: () => void;
  onSubmit: (payload: ClerigoPayload) => void;
}

/**
 * Modal formal de cadastro e edição de Clérigo.
 * Aplica validação dos campos canônicos e civis com máscaras automáticas.
 */
export const ClerigoFormModal: React.FC<ClerigoFormModalProps> = ({
  open,
  isPending,
  initialData,
  onClose,
  onSubmit,
}) => {
  const isEditing = Boolean(initialData);
  const [form, setForm] = useState<ClerigoPayload>(estadoInicialForm());

  useEffect(() => {
    if (initialData) {
      setForm(clerigoParaPayload(initialData));
    } else {
      setForm(estadoInicialForm());
    }
  }, [initialData, open]);

  const handleSubmit = () => {
    onSubmit(form);
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>{isEditing ? 'Editar clérigo' : 'Novo clérigo'}</DialogTitle>
      <DialogContent dividers>
        <Stack spacing={2.5}>
          {/* Identificação e Dados Canônicos */}
          <Card variant="outlined" sx={{ borderRadius: 2 }}>
            <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
              <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
                <BadgeIcon color="primary" fontSize="small" />
                <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>
                  Identificação e dados canônicos
                </Typography>
              </Stack>
              <Grid container spacing={2}>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <TextField
                    fullWidth
                    size="small"
                    label="Nome eclesiástico"
                    required
                    value={form.nomeEclesiastico}
                    onChange={(e) => setForm({ ...form, nomeEclesiastico: e.target.value })}
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <TextField
                    fullWidth
                    size="small"
                    label="Nome civil"
                    value={form.nomeCivil}
                    onChange={(e) => setForm({ ...form, nomeCivil: e.target.value })}
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <FormControl fullWidth size="small" required>
                    <InputLabel>Grau da ordem</InputLabel>
                    <Select
                      value={form.grauOrdem}
                      label="Grau da ordem"
                      onChange={(e) => setForm({ ...form, grauOrdem: e.target.value as GrauOrdem })}
                    >
                      {GRAUS_ORDEM.map((item) => (
                        <MenuItem key={item.value} value={item.value}>
                          {item.label}
                        </MenuItem>
                      ))}
                    </Select>
                  </FormControl>
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <FormControl fullWidth size="small" required>
                    <InputLabel>Encargo atual</InputLabel>
                    <Select
                      value={form.funcaoAtual}
                      label="Encargo atual"
                      onChange={(e) =>
                        setForm({ ...form, funcaoAtual: e.target.value as FuncaoEclesiastica })
                      }
                    >
                      {FUNCOES_ECLESIASTICAS.map((item) => (
                        <MenuItem key={item.value} value={item.value}>
                          {item.label}
                        </MenuItem>
                      ))}
                    </Select>
                  </FormControl>
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <TextField
                    fullWidth
                    size="small"
                    label="Diocese de incardinação"
                    required
                    value={form.dioceseIncardinacao}
                    onChange={(e) => setForm({ ...form, dioceseIncardinacao: e.target.value })}
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 3 }}>
                  <TextField
                    fullWidth
                    size="small"
                    label="CPF"
                    value={form.cpf || ''}
                    onChange={(e) => setForm({ ...form, cpf: formatCpf(e.target.value) })}
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 3 }}>
                  <TextField
                    fullWidth
                    size="small"
                    label="RG"
                    value={form.rg || ''}
                    onChange={(e) => setForm({ ...form, rg: formatRg(e.target.value) })}
                  />
                </Grid>
              </Grid>
            </CardContent>
          </Card>

          {/* Datas Canônicas e Nascimento */}
          <Card variant="outlined" sx={{ borderRadius: 2 }}>
            <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
              <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
                <CalendarMonthIcon color="primary" fontSize="small" />
                <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>
                  Datas canônicas e nascimento
                </Typography>
              </Stack>
              <Grid container spacing={2}>
                <Grid size={{ xs: 12, sm: 6, md: 3 }}>
                  <TextField
                    fullWidth
                    size="small"
                    type="date"
                    label="Data de nascimento"
                    slotProps={{ inputLabel: { shrink: true } }}
                    value={form.dataNascimento}
                    onChange={(e) => setForm({ ...form, dataNascimento: e.target.value })}
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 6, md: 3 }}>
                  <TextField
                    fullWidth
                    size="small"
                    type="date"
                    label="Ordenação diaconal"
                    slotProps={{ inputLabel: { shrink: true } }}
                    value={form.dataOrdenacaoDiaconal || ''}
                    onChange={(e) =>
                      setForm({ ...form, dataOrdenacaoDiaconal: e.target.value || null })
                    }
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 6, md: 3 }}>
                  <TextField
                    fullWidth
                    size="small"
                    type="date"
                    label="Ordenação presbiteral"
                    slotProps={{ inputLabel: { shrink: true } }}
                    value={form.dataOrdenacaoPresbiteral || ''}
                    onChange={(e) =>
                      setForm({ ...form, dataOrdenacaoPresbiteral: e.target.value || null })
                    }
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 6, md: 3 }}>
                  <TextField
                    fullWidth
                    size="small"
                    type="date"
                    label="Ordenação episcopal"
                    slotProps={{ inputLabel: { shrink: true } }}
                    value={form.dataOrdenacaoEpiscopal || ''}
                    onChange={(e) =>
                      setForm({ ...form, dataOrdenacaoEpiscopal: e.target.value || null })
                    }
                  />
                </Grid>
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
                <Grid size={{ xs: 12, sm: 6 }}>
                  <TextField
                    fullWidth
                    size="small"
                    label="E-mail"
                    type="email"
                    required
                    value={form.email}
                    onChange={(e) => setForm({ ...form, email: e.target.value })}
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 3 }}>
                  <TextField
                    fullWidth
                    size="small"
                    label="Celular"
                    placeholder="(00) 00000-0000"
                    required
                    value={form.celular}
                    onChange={(e) => setForm({ ...form, celular: formatPhone(e.target.value) })}
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 3 }}>
                  <TextField
                    fullWidth
                    size="small"
                    label="Telefone fixo"
                    placeholder="(00) 0000-0000"
                    value={form.telefoneFixo}
                    onChange={(e) => setForm({ ...form, telefoneFixo: formatPhone(e.target.value) })}
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 9 }}>
                  <TextField
                    fullWidth
                    size="small"
                    label="Endereço residencial"
                    value={form.endereco}
                    onChange={(e) => setForm({ ...form, endereco: e.target.value })}
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 3 }}>
                  <FormControlLabel
                    control={
                      <Switch
                        checked={form.isWhatsapp}
                        onChange={(e) => setForm({ ...form, isWhatsapp: e.target.checked })}
                      />
                    }
                    label="Celular é WhatsApp"
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
          title={isEditing ? 'Salvar alterações' : 'Salvar clérigo'}
          icon={<SaveIcon />}
          color="primary"
          onClick={handleSubmit}
          disabled={isPending}
        />
      </DialogActions>
    </Dialog>
  );
};
