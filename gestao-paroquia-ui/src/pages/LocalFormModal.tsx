import React, {useEffect, useState} from 'react';
import {useQuery} from '@tanstack/react-query';
import {
    Box,
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
import {getOrganizacoes} from '../api/organizacoes';

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
  const [organizacaoId, setOrganizacaoId] = useState('');
  const [errors, setErrors] = useState<{ nome?: string }>({});

  const { data: orgs = [] } = useQuery({
    queryKey: ['organizacoes'],
    queryFn: getOrganizacoes,
    enabled: open,
  });

  const matrizComunidades = orgs.filter(
    (o) => o.tipo === 'MATRIZ' || o.tipo === 'COMUNIDADE' || o.tipo === 'CLERO'
  );

  useEffect(() => {
    if (local) {
      setNome(local.nome);
      setTipo(local.tipo || 'OUTRO');
      setEndereco(local.endereco || '');
      setCapacidade(local.capacidade !== null ? local.capacidade : '');
      setCaracteristicas(local.caracteristicas || '');
      setOrganizacaoId(local.organizacaoId || '');
    } else {
      setNome('');
      setTipo('OUTRO');
      setEndereco('');
      setCapacidade('');
      setCaracteristicas('');
      setOrganizacaoId('');
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
      organizacaoId: organizacaoId || null,
    });
  };

  return (
    <Dialog
      open={open}
      onClose={onClose}
      fullWidth
      maxWidth="sm"
      sx={{ '& .MuiDialog-paper': { borderRadius: 0 } }}
    >
      <form onSubmit={handleSubmit} noValidate>
        <DialogTitle sx={{ borderBottom: '1px solid #eee', pb: 2, fontWeight: 700, color: '#37474f', fontSize: '1.25rem' }}>
          {local ? 'Editar Local' : 'Novo Local'}
        </DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2.5, mt: 1 }}>
            <TextField
              label="Nome do Local"
              variant="outlined"
              fullWidth
              required
              size="small"
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

            <FormControl fullWidth size="small" sx={{ '& .MuiOutlinedInput-root': { borderRadius: 0 } }}>
              <InputLabel id="organizacao-local-label">Matriz / Comunidade (Subordinação)</InputLabel>
              <Select
                labelId="organizacao-local-label"
                label="Matriz / Comunidade (Subordinação)"
                value={organizacaoId}
                onChange={(e) => setOrganizacaoId(e.target.value as string)}
                disabled={isPending}
              >
                <MenuItem value="">
                  <em>Sem vínculo específico</em>
                </MenuItem>
                {matrizComunidades.map((org) => (
                  <MenuItem key={org.id} value={org.id}>
                    {org.nome} ({org.tipo === 'MATRIZ' ? 'Matriz' : org.tipo === 'COMUNIDADE' ? 'Comunidade' : 'Paróquia'})
                  </MenuItem>
                ))}
              </Select>
            </FormControl>

            <Grid container spacing={2}>
              <Grid size={{ xs: 6 }}>
                <FormControl fullWidth required size="small" sx={{ '& .MuiOutlinedInput-root': { borderRadius: 0 } }}>
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

              <Grid size={{ xs: 6 }}>
                <TextField
                  label="Capacidade"
                  type="number"
                  variant="outlined"
                  fullWidth
                  size="small"
                  value={capacidade}
                  onChange={(e) => {
                    const val = e.target.value;
                    setCapacidade(val === '' ? '' : Math.max(0, parseInt(val, 10)));
                  }}
                  disabled={isPending}
                  sx={{ '& .MuiOutlinedInput-root': { borderRadius: 0 } }}
                />
              </Grid>
            </Grid>

            <TextField
              label="Endereço"
              variant="outlined"
              fullWidth
              size="small"
              value={endereco}
              onChange={(e) => setEndereco(e.target.value)}
              disabled={isPending}
              sx={{ '& .MuiOutlinedInput-root': { borderRadius: 0 } }}
            />

            <TextField
              label="Características / Recursos"
              variant="outlined"
              fullWidth
              multiline
              rows={3}
              size="small"
              placeholder="Ex: Ar condicionado, Projetor, Som, etc."
              value={caracteristicas}
              onChange={(e) => setCaracteristicas(e.target.value)}
              disabled={isPending}
              sx={{ '& .MuiOutlinedInput-root': { borderRadius: 0 } }}
            />
          </Box>
        </DialogContent>
        <DialogActions sx={{ borderTop: '1px solid #eee', pt: 2, pb: 2, px: 3 }}>
          <Button
            onClick={onClose}
            disabled={isPending}
            sx={{ textTransform: 'none', fontWeight: 600, color: '#d97706' }}
          >
            Cancelar
          </Button>
          <Button
            type="submit"
            variant="contained"
            disabled={isPending}
            sx={{
              bgcolor: '#1a237e',
              color: '#fff',
              textTransform: 'none',
              fontWeight: 600,
              borderRadius: 0,
              px: 3,
              '&:hover': { bgcolor: '#0d47a1' },
            }}
          >
            {local ? 'Salvar Alterações' : 'Cadastrar'}
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  );
};

export default LocalFormModal;
