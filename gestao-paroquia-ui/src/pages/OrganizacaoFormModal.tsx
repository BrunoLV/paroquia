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
import { useQuery } from '@tanstack/react-query';
import { Organizacao, PastoralMovimentoOficial } from '../api/types';
import { Usuario } from '../api/usuarios';
import { getOrganizacaoMembros } from '../api/organizacoes';

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
  const [nome, setNome] = useState('');
  const [tipo, setTipo] = useState('PASTORAL');
  const [contato, setContato] = useState('');
  const [coordenador, setCoordenador] = useState('');
  const [viceCoordenador, setViceCoordenador] = useState('');
  const [parentId, setParentId] = useState('');
  const [pastoralOficialId, setPastoralOficialId] = useState('');
  const [errors, setErrors] = useState<{ nome?: string; pastoralOficialId?: string }>({});

  useEffect(() => {
    if (organizacao) {
      setNome(organizacao.nome);
      setTipo(organizacao.tipo || 'PASTORAL');
      setContato(organizacao.contato || '');
      setCoordenador(organizacao.coordenador || '');
      setViceCoordenador(organizacao.viceCoordenador || '');
      setParentId(organizacao.parentId || '');
      setPastoralOficialId(organizacao.pastoralOficialId || '');
    } else {
      setNome('');
      setTipo('PASTORAL');
      setContato('');
      setCoordenador('');
      setViceCoordenador('');
      setParentId('');
      setPastoralOficialId('');
    }
    setErrors({});
  }, [organizacao, open]);

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
    if (churchId && churchMembers.length > 0) {
      return churchMembers.map(m => ({
        id: m.membershipId,
        nome: m.usuarioNome
      }));
    }
    return usuarios.map(u => ({
      id: u.id,
      nome: u.nome
    }));
  };

  const coordinatorOptions = getCoordinatorOptions();

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    const newErrors: { nome?: string; pastoralOficialId?: string } = {};
    if (!nome.trim()) {
      newErrors.nome = 'O nome é obrigatório';
    }
    if ((tipo === 'PASTORAL' || tipo === 'MOVIMENTO') && !pastoralOficialId) {
      newErrors.pastoralOficialId = 'A associação oficial é obrigatória';
    }

    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }

    onSave({
      nome: nome.trim(),
      tipo,
      contato: contato.trim() || null,
      coordenador: coordenador.trim() || null,
      viceCoordenador: viceCoordenador.trim() || null,
      ativo: organizacao ? organizacao.ativo : true,
      parentId: (tipo === 'CLERO') ? null : (parentId || null),
      pastoralOficialId: (tipo === 'PASTORAL' || tipo === 'MOVIMENTO') ? pastoralOficialId : null,
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
      onClose={onClose}
      fullWidth
      maxWidth="md"
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
                  <MenuItem value="CLERO">Clero</MenuItem>
                  <MenuItem value="MATRIZ">Matriz</MenuItem>
                  <MenuItem value="COMUNIDADE">Comunidade</MenuItem>
                  <MenuItem value="CONSELHO_PASTORAL">Conselho Pastoral</MenuItem>
                  <MenuItem value="CONSELHO_ECONOMICO">Conselho Econômico</MenuItem>
                  <MenuItem value="OUTRO">Outro</MenuItem>
                </Select>
              </FormControl>
            </Grid>

            {(tipo === 'PASTORAL' || tipo === 'MOVIMENTO') && (
              <Grid size={{ xs: 12, sm: 6 }}>
                <FormControl fullWidth required error={!!errors.pastoralOficialId}>
                  <InputLabel id="pastoral-oficial-label">Tipo Oficial / Movimento</InputLabel>
                  <Select
                    labelId="pastoral-oficial-label"
                    label="Tipo Oficial / Movimento"
                    value={pastoralOficialId}
                    onChange={(e) => {
                      setPastoralOficialId(e.target.value as string);
                      setErrors(prev => ({ ...prev, pastoralOficialId: undefined }));
                    }}
                    disabled={isPending}
                    sx={{ borderRadius: 0 }}
                  >
                    <MenuItem value=""><em>Selecione...</em></MenuItem>
                    {pastoraisOficiais
                      .filter(po => po.tipo === tipo)
                      .map(po => (
                        <MenuItem key={po.id} value={po.id}>{po.nome}</MenuItem>
                      ))}
                  </Select>
                  {errors.pastoralOficialId && (
                    <span style={{ color: '#d32f2f', fontSize: '0.75rem', marginTop: '3px', marginLeft: '14px' }}>
                      {errors.pastoralOficialId}
                    </span>
                  )}
                </FormControl>
              </Grid>
            )}

            {tipo !== 'CLERO' && (
              <Grid size={{ xs: 12, sm: 6 }}>
                <FormControl fullWidth>
                  <InputLabel id="parent-org-label">Organização Pai</InputLabel>
                  <Select
                    labelId="parent-org-label"
                    label="Organização Pai"
                    value={parentId}
                    onChange={(e) => setParentId(e.target.value as string)}
                    disabled={isPending}
                    sx={{ borderRadius: 0 }}
                  >
                    <MenuItem value=""><em>Nenhuma</em></MenuItem>
                    {parentOptions.map(org => (
                      <MenuItem key={org.id} value={org.id}>{org.nome}</MenuItem>
                    ))}
                  </Select>
                </FormControl>
              </Grid>
            )}

            <Grid size={{ xs: 12, sm: tipo === 'CLERO' ? 6 : 12 }}>
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
              <FormControl fullWidth>
                <InputLabel id="coordenador-select-label">Coordenador</InputLabel>
                <Select
                  labelId="coordenador-select-label"
                  label="Coordenador"
                  value={coordenador}
                  onChange={(e) => setCoordenador(e.target.value as string)}
                  disabled={isPending}
                  sx={{ borderRadius: 0 }}
                >
                  <MenuItem value=""><em>Nenhum</em></MenuItem>
                  {coordinatorOptions.map(opt => (
                    <MenuItem key={opt.id} value={opt.nome}>{opt.nome}</MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>

            <Grid size={{ xs: 12, sm: 6 }}>
              <FormControl fullWidth>
                <InputLabel id="vice-coordenador-select-label">Vice-Coordenador</InputLabel>
                <Select
                  labelId="vice-coordenador-select-label"
                  label="Vice-Coordenador"
                  value={viceCoordenador}
                  onChange={(e) => setViceCoordenador(e.target.value as string)}
                  disabled={isPending}
                  sx={{ borderRadius: 0 }}
                >
                  <MenuItem value=""><em>Nenhum</em></MenuItem>
                  {coordinatorOptions.map(opt => (
                    <MenuItem key={opt.id} value={opt.nome}>{opt.nome}</MenuItem>
                  ))}
                </Select>
              </FormControl>
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
