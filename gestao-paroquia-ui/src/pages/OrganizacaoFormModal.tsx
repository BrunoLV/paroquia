import React, { useEffect, useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Grid,
} from '@mui/material';
import { Organizacao } from '../api/types';

interface OrganizacaoFormModalProps {
  open: boolean;
  onClose: () => void;
  onSave: (data: Omit<Organizacao, 'id'>) => void;
  organizacao?: Organizacao | null;
  isPending: boolean;
}

const OrganizacaoFormModal: React.FC<OrganizacaoFormModalProps> = ({
  open,
  onClose,
  onSave,
  organizacao,
  isPending,
}) => {
  const [nome, setNome] = useState('');
  const [tipo, setTipo] = useState('PASTORAL');
  const [contato, setContato] = useState('');
  const [coordenador, setCoordenador] = useState('');
  const [viceCoordenador, setViceCoordenador] = useState('');
  const [errors, setErrors] = useState<{ nome?: string }>({});

  useEffect(() => {
    if (organizacao) {
      setNome(organizacao.nome);
      setTipo(organizacao.tipo || 'PASTORAL');
      setContato(organizacao.contato || '');
      setCoordenador(organizacao.coordenador || '');
      setViceCoordenador(organizacao.viceCoordenador || '');
    } else {
      setNome('');
      setTipo('PASTORAL');
      setContato('');
      setCoordenador('');
      setViceCoordenador('');
    }
    setErrors({});
  }, [organizacao, open]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    if (!nome.trim()) {
      setErrors({ nome: 'O nome é obrigatório' });
      return;
    }

    onSave({
      nome: nome.trim(),
      tipo,
      contato: contato.trim() || null,
      coordenador: coordenador.trim() || null,
      viceCoordenador: viceCoordenador.trim() || null,
      ativo: organizacao ? organizacao.ativo : true,
    });
  };

  return (
    <Dialog
      open={open}
      onClose={onClose}
      fullWidth
      maxWidth="sm"
      sx={{ '& .MuiPaper-root': { borderRadius: 0 } }}
    >
      <form onSubmit={handleSubmit}>
        <DialogTitle sx={{ fontWeight: 600, color: 'primary.main' }}>
          {organizacao ? 'Editar Pastoral / Movimento' : 'Nova Pastoral / Movimento'}
        </DialogTitle>
        <DialogContent dividers>
          <Grid container spacing={2} sx={{ mt: 0.5 }}>
            <Grid size={{ xs: 12 }}>
              <TextField
                label="Nome"
                variant="outlined"
                fullWidth
                required
                value={nome}
                onChange={(e) => {
                  setNome(e.target.value);
                  if (e.target.value.trim()) setErrors({});
                }}
                error={!!errors.nome}
                helperText={errors.nome}
                disabled={isPending}
                sx={{ '& .MuiOutlinedInput-root': { borderRadius: 0 } }}
              />
            </Grid>

            <Grid size={{ xs: 12, sm: 6 }}>
              <FormControl fullWidth required>
                <InputLabel id="tipo-org-label">Tipo</InputLabel>
                <Select
                  labelId="tipo-org-label"
                  label="Tipo"
                  value={tipo}
                  onChange={(e) => setTipo(e.target.value as string)}
                  disabled={isPending}
                  sx={{ borderRadius: 0 }}
                >
                  <MenuItem value="PASTORAL">Pastoral</MenuItem>
                  <MenuItem value="MOVIMENTO">Movimento</MenuItem>
                  <MenuItem value="LAICATO">Laicato</MenuItem>
                  <MenuItem value="CLERO">Clero</MenuItem>
                  <MenuItem value="CONSELHO">Conselho</MenuItem>
                  <MenuItem value="OUTRO">Outro</MenuItem>
                </Select>
              </FormControl>
            </Grid>

            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                label="Contato (E-mail/Tel)"
                variant="outlined"
                fullWidth
                value={contato}
                onChange={(e) => setContato(e.target.value)}
                disabled={isPending}
                sx={{ '& .MuiOutlinedInput-root': { borderRadius: 0 } }}
              />
            </Grid>

            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                label="Coordenador"
                variant="outlined"
                fullWidth
                value={coordenador}
                onChange={(e) => setCoordenador(e.target.value)}
                disabled={isPending}
                sx={{ '& .MuiOutlinedInput-root': { borderRadius: 0 } }}
              />
            </Grid>

            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                label="Vice-Coordenador"
                variant="outlined"
                fullWidth
                value={viceCoordenador}
                onChange={(e) => setViceCoordenador(e.target.value)}
                disabled={isPending}
                sx={{ '& .MuiOutlinedInput-root': { borderRadius: 0 } }}
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button onClick={onClose} color="inherit" disabled={isPending} sx={{ borderRadius: 0 }}>
            Cancelar
          </Button>
          <Button
            type="submit"
            variant="contained"
            color="primary"
            disabled={isPending}
            sx={{ borderRadius: 0, fontWeight: 600 }}
          >
            {organizacao ? 'Salvar Alterações' : 'Cadastrar'}
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  );
};

export default OrganizacaoFormModal;
