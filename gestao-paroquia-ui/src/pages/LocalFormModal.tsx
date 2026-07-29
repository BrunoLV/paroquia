import React, {useEffect, useState} from 'react';
import {
    Button,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    FormControl,
    Grid,
    InputLabel,
    MenuItem,
    Select,
    TextField,
} from '@mui/material';
import {Local} from '../api/types';

interface LocalFormModalProps {
  open: boolean;
  onClose: () => void;
  onSave: (data: Omit<Local, 'id'>) => void;
  local?: Local | null;
  isPending: boolean;
}

const LocalFormModal: React.FC<LocalFormModalProps> = ({
  open,
  onClose,
  onSave,
  local,
  isPending,
}) => {
  const [nome, setNome] = useState('');
  const [tipo, setTipo] = useState('OUTRO');
  const [endereco, setEndereco] = useState('');
  const [capacidade, setCapacidade] = useState<number | ''>('');
  const [caracteristicas, setCaracteristicas] = useState('');
  const [errors, setErrors] = useState<{ nome?: string }>({});

  useEffect(() => {
    if (local) {
      setNome(local.nome);
      setTipo(local.tipo || 'OUTRO');
      setEndereco(local.endereco || '');
      setCapacidade(local.capacidade !== null ? local.capacidade : '');
      setCaracteristicas(local.caracteristicas || '');
    } else {
      setNome('');
      setTipo('OUTRO');
      setEndereco('');
      setCapacidade('');
      setCaracteristicas('');
    }
    setErrors({});
  }, [local, open]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    if (!nome.trim()) {
      setErrors({ nome: 'O nome é obrigatório' });
      return;
    }

    onSave({
      nome: nome.trim(),
      tipo,
      endereco: endereco.trim() || null,
      capacidade: capacidade === '' ? null : Number(capacidade),
      caracteristicas: caracteristicas.trim() || null,
      ativo: local ? local.ativo : true,
    });
  };

  return (
    <Dialog
      open={open}
      onClose={onClose}
      fullWidth
      maxWidth="md"
      PaperProps={{ sx: { borderRadius: 0 } }}
    >
      <form onSubmit={handleSubmit}>
        <DialogTitle sx={{ fontWeight: 700, px: 3, pt: 3, pb: 1.5, color: 'primary.main' }}>
          {local ? 'Editar Local' : 'Novo Local'}
        </DialogTitle>
        <DialogContent dividers sx={{ p: 3 }}>
          <Grid container spacing={2.5}>
            <Grid size={{ xs: 12 }}>
              <TextField
                label="Nome do Local"
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
              <FormControl fullWidth required sx={{ '& .MuiOutlinedInput-root': { borderRadius: 0 } }}>
                <InputLabel id="tipo-local-label">Tipo</InputLabel>
                <Select
                  labelId="tipo-local-label"
                  label="Tipo"
                  value={tipo}
                  onChange={(e) => setTipo(e.target.value as string)}
                  disabled={isPending}
                >
                  <MenuItem value="IGREJA">Igreja</MenuItem>
                  <MenuItem value="SALAO">Salão</MenuItem>
                  <MenuItem value="SALA">Sala</MenuItem>
                  <MenuItem value="OUTRO">Outro</MenuItem>
                </Select>
              </FormControl>
            </Grid>

            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                label="Capacidade"
                type="number"
                variant="outlined"
                fullWidth
                value={capacidade}
                onChange={(e) => {
                  const val = e.target.value;
                  setCapacidade(val === '' ? '' : Math.max(0, parseInt(val, 10)));
                }}
                disabled={isPending}
                sx={{ '& .MuiOutlinedInput-root': { borderRadius: 0 } }}
              />
            </Grid>

            <Grid size={{ xs: 12 }}>
              <TextField
                label="Endereço"
                variant="outlined"
                fullWidth
                value={endereco}
                onChange={(e) => setEndereco(e.target.value)}
                disabled={isPending}
                sx={{ '& .MuiOutlinedInput-root': { borderRadius: 0 } }}
              />
            </Grid>

            <Grid size={{ xs: 12 }}>
              <TextField
                label="Características / Recursos"
                variant="outlined"
                fullWidth
                multiline
                rows={3}
                placeholder="Ex: Ar condicionado, Projetor, Som, etc."
                value={caracteristicas}
                onChange={(e) => setCaracteristicas(e.target.value)}
                disabled={isPending}
                sx={{ '& .MuiOutlinedInput-root': { borderRadius: 0 } }}
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions sx={{ px: 3, py: 2, borderTop: '1px solid', borderColor: 'divider', gap: 1 }}>
          <Button onClick={onClose} color="inherit" disabled={isPending} sx={{ borderRadius: 0, textTransform: 'none', fontWeight: 600 }}>
            Cancelar
          </Button>
          <Button
            type="submit"
            variant="contained"
            color="primary"
            disabled={isPending}
            sx={{ borderRadius: 0, fontWeight: 600, textTransform: 'none', px: 3, py: 1 }}
          >
            {local ? 'Salvar Alterações' : 'Cadastrar'}
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  );
};

export default LocalFormModal;
