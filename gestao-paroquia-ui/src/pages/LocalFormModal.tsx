import React, {useEffect, useState} from 'react';
import {useQuery} from '@tanstack/react-query';
import {
    Box,
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
import {Close as CloseIcon, Save as SaveIcon} from '@mui/icons-material';
import {useAuth} from '../auth/AuthContext';
import {isGlobalCreator} from '../auth/permissions';
import {Local} from '../api/types';
import {getOrganizacoes} from '../api/organizacoes';
import ActionButton from '../components/ActionButton';

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
  const { user } = useAuth();
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
    >
      <form onSubmit={handleSubmit} noValidate>
        <DialogTitle>
          {local ? 'Editar Local' : 'Novo Local'}
        </DialogTitle>
        <DialogContent dividers>
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
            />

            <FormControl fullWidth size="small">
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
                <FormControl fullWidth required size="small">
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
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <ActionButton title="Cancelar" icon={<CloseIcon />} onClick={onClose} disabled={isPending} color="secondary" />
          <ActionButton
            title={local ? 'Salvar Alterações' : 'Salvar'}
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

export default LocalFormModal;
