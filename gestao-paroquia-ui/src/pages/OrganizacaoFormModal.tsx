import React, {useEffect, useState} from 'react';
import {
    Box,
    Button,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    FormControl,
    FormHelperText,
    Grid,
    InputLabel,
    MenuItem,
    Select,
    TextField,
} from '@mui/material';
import {useQuery} from '@tanstack/react-query';
import {Organizacao, PastoralMovimentoOficial} from '../api/types';
import {Usuario} from '../api/usuarios';
import {getOrganizacaoMembros} from '../api/organizacoes';

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
    if (churchId) {
      if (churchMembers.length > 0) {
        return churchMembers.map(m => ({
          id: m.usuarioNome,
          nome: m.usuarioNome
        }));
      }
      const filteredByChurch = usuarios.filter(u => !u.igrejaId || u.igrejaId === churchId);
      return filteredByChurch.map(u => ({
        id: u.nome,
        nome: u.nome
      }));
    }
    return usuarios.map(u => ({
      id: u.nome,
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
      maxWidth="sm"
    >
      <form onSubmit={handleSubmit} noValidate>
        <DialogTitle>
          {organizacao ? 'Editar Pastoral / Movimento' : 'Nova Pastoral / Movimento'}
        </DialogTitle>
        <DialogContent dividers>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2.5, mt: 1 }}>
            <TextField
              label="Nome"
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

            <Grid container spacing={2}>
              <Grid size={{ xs: (tipo === 'PASTORAL' || tipo === 'MOVIMENTO') ? 6 : 12 }}>
                <FormControl fullWidth required size="small">
                  <InputLabel id="tipo-org-label">Tipo</InputLabel>
                  <Select
                    labelId="tipo-org-label"
                    label="Tipo"
                    value={tipo}
                    onChange={(e) => setTipo(e.target.value as string)}
                    disabled={isPending}
                  >
                    <MenuItem value="PASTORAL">Pastoral</MenuItem>
                    <MenuItem value="MOVIMENTO">Movimento</MenuItem>
                    <MenuItem value="CLERO">Clero</MenuItem>
                    <MenuItem value="MATRIZ">Matriz</MenuItem>
                    <MenuItem value="COMUNIDADE">Comunidade</MenuItem>
                    <MenuItem value="CONSELHO_PASTORAL">Conselho Pastoral Paroquial (CPP)</MenuItem>
                    <MenuItem value="CONSELHO_ECONOMICO">Conselho Econômico Paroquial (CAE)</MenuItem>
                    <MenuItem value="OUTRO">Outro</MenuItem>
                  </Select>
                </FormControl>
              </Grid>

              {(tipo === 'PASTORAL' || tipo === 'MOVIMENTO') && (
                <Grid size={{ xs: 6 }}>
                  <FormControl fullWidth required size="small" error={!!errors.pastoralOficialId}>
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
                    >
                      <MenuItem value=""><em>Selecione...</em></MenuItem>
                      {pastoraisOficiais
                        .filter(po => po.tipo === tipo)
                        .map(po => (
                          <MenuItem key={po.id} value={po.id}>{po.nome}</MenuItem>
                        ))}
                    </Select>
                    {errors.pastoralOficialId && (
                      <FormHelperText error>
                        {errors.pastoralOficialId}
                      </FormHelperText>
                    )}
                  </FormControl>
                </Grid>
              )}
            </Grid>

            {tipo !== 'CLERO' && (
              <FormControl fullWidth size="small">
                <InputLabel id="parent-org-label">Organização Pai</InputLabel>
                <Select
                  labelId="parent-org-label"
                  label="Organização Pai"
                  value={parentId}
                  onChange={(e) => setParentId(e.target.value as string)}
                  disabled={isPending}
                >
                  <MenuItem value=""><em>Nenhuma</em></MenuItem>
                  {parentOptions.map(org => (
                    <MenuItem key={org.id} value={org.id}>{org.nome}</MenuItem>
                  ))}
                </Select>
              </FormControl>
            )}

            <TextField
              label="Contato (E-mail/Tel)"
              variant="outlined"
              fullWidth
              size="small"
              value={contato}
              onChange={(e) => setContato(e.target.value)}
              disabled={isPending}
            />

            <Grid container spacing={2}>
              <Grid size={{ xs: 6 }}>
                <FormControl fullWidth size="small">
                  <InputLabel id="coordenador-select-label">Coordenador</InputLabel>
                  <Select
                    labelId="coordenador-select-label"
                    label="Coordenador"
                    value={coordenador}
                    onChange={(e) => setCoordenador(e.target.value as string)}
                    disabled={isPending}
                  >
                    <MenuItem value=""><em>Nenhum</em></MenuItem>
                    {coordinatorOptions.map(opt => (
                      <MenuItem key={opt.id} value={opt.nome}>{opt.nome}</MenuItem>
                    ))}
                  </Select>
                </FormControl>
              </Grid>

              <Grid size={{ xs: 6 }}>
                <FormControl fullWidth size="small">
                  <InputLabel id="vice-coordenador-select-label">Vice-Coordenador</InputLabel>
                  <Select
                    labelId="vice-coordenador-select-label"
                    label="Vice-Coordenador"
                    value={viceCoordenador}
                    onChange={(e) => setViceCoordenador(e.target.value as string)}
                    disabled={isPending}
                  >
                    <MenuItem value=""><em>Nenhum</em></MenuItem>
                    {coordinatorOptions.map(opt => (
                      <MenuItem key={opt.id} value={opt.nome}>{opt.nome}</MenuItem>
                    ))}
                  </Select>
                </FormControl>
              </Grid>
            </Grid>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button
            onClick={onClose}
            disabled={isPending}
            color="secondary"
            sx={{ textTransform: 'none', fontWeight: 600 }}
          >
            Cancelar
          </Button>
          <Button
            type="submit"
            variant="contained"
            color="primary"
            disabled={isPending}
            sx={{ textTransform: 'none', fontWeight: 600, px: 3 }}
          >
            {organizacao ? 'Salvar Alterações' : 'Salvar'}
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  );
};

export default OrganizacaoFormModal;
