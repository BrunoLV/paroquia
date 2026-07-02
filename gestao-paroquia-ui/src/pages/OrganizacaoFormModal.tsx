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
import { Usuario } from '../api/usuarios';
import { OrganizacaoMembro } from '../api/organizacoes';

interface OrganizacaoFormModalProps {
  open: boolean;
  onClose: () => void;
  onSave: (
    data: Omit<Organizacao, 'id'>,
    matrix?: { churchId: string; coordenadorId?: string | null; viceCoordenadorId?: string | null }[]
  ) => void;
  organizacao?: Organizacao | null;
  isPending: boolean;
  usuarios?: Usuario[];
  igrejas?: Organizacao[];
  membros?: OrganizacaoMembro[];
}

const OrganizacaoFormModal: React.FC<OrganizacaoFormModalProps> = ({
  open,
  onClose,
  onSave,
  organizacao,
  isPending,
  usuarios = [],
  igrejas = [],
  membros = [],
}) => {
  const [nome, setNome] = useState('');
  const [tipo, setTipo] = useState('PASTORAL');
  const [contato, setContato] = useState('');
  const [coordenador, setCoordenador] = useState('');
  const [viceCoordenador, setViceCoordenador] = useState('');
  const [errors, setErrors] = useState<{ nome?: string }>({});

  const [matrixState, setMatrixState] = useState<Record<string, { coordenadorId: string; viceCoordenadorId: string }>>({});

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

  useEffect(() => {
    if (open) {
      const initial: Record<string, { coordenadorId: string; viceCoordenadorId: string }> = {};
      if (igrejas && igrejas.length > 0) {
        igrejas.forEach(igreja => {
          const coord = membros.find(m => m.ativo && m.papel === 'COORDENADOR' && m.igrejaVinculoId === igreja.id);
          const vice = membros.find(m => m.ativo && m.papel === 'VICE_COORDENADOR' && m.igrejaVinculoId === igreja.id);
          initial[igreja.id] = {
            coordenadorId: coord ? coord.usuarioId : '',
            viceCoordenadorId: vice ? vice.usuarioId : '',
          };
        });
      }
      setMatrixState(initial);
    }
  }, [open, membros, igrejas]);

  const handleMatrixChange = (churchId: string, field: 'coordenadorId' | 'viceCoordenadorId', value: string) => {
    setMatrixState(prev => ({
      ...prev,
      [churchId]: {
        ...prev[churchId] || { coordenadorId: '', viceCoordenadorId: '' },
        [field]: value,
      },
    }));
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    if (!nome.trim()) {
      setErrors({ nome: 'O nome é obrigatório' });
      return;
    }

    const matrixPayload = (tipo === 'PASTORAL' || tipo === 'MOVIMENTO')
      ? Object.entries(matrixState).map(([churchId, state]) => ({
          churchId,
          coordenadorId: state.coordenadorId || null,
          viceCoordenadorId: state.viceCoordenadorId || null,
        }))
      : undefined;

    onSave({
      nome: nome.trim(),
      tipo,
      contato: contato.trim() || null,
      coordenador: (tipo === 'PASTORAL' || tipo === 'MOVIMENTO') ? null : (coordenador.trim() || null),
      viceCoordenador: (tipo === 'PASTORAL' || tipo === 'MOVIMENTO') ? null : (viceCoordenador.trim() || null),
      ativo: organizacao ? organizacao.ativo : true,
    }, matrixPayload);
  };

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

            {(tipo === 'PASTORAL' || tipo === 'MOVIMENTO') ? (
              <Grid size={{ xs: 12 }}>
                <p style={{ fontWeight: 600, marginTop: 16, marginBottom: 8, color: '#333' }}>
                  Matriz de Coordenação por Igreja (Matriz / Comunidade)
                </p>
                <div style={{ maxHeight: 250, overflowY: 'auto', border: '1px solid #ccc', padding: '16px 8px' }}>
                  <Grid container spacing={2}>
                    {igrejas.map(igreja => {
                      const key = igreja.id;
                      const coordVal = matrixState[key]?.coordenadorId || '';
                      const viceVal = matrixState[key]?.viceCoordenadorId || '';

                      return (
                        <React.Fragment key={key}>
                          <Grid size={{ xs: 4 }} style={{ display: 'flex', alignItems: 'center' }}>
                            <span style={{ fontSize: '0.9rem', fontWeight: 500 }}>{igreja.nome}</span>
                          </Grid>
                          <Grid size={{ xs: 4 }}>
                            <FormControl fullWidth size="small">
                              <InputLabel id={`coord-${key}-label`}>Coordenador</InputLabel>
                              <Select
                                labelId={`coord-${key}-label`}
                                label="Coordenador"
                                value={coordVal}
                                onChange={(e) => handleMatrixChange(key, 'coordenadorId', e.target.value as string)}
                                disabled={isPending}
                                sx={{ borderRadius: 0 }}
                                data-testid={`coord-select-${key}`}
                              >
                                <MenuItem value=""><em>Nenhum</em></MenuItem>
                                {usuarios.map(u => (
                                  <MenuItem key={u.id} value={u.id}>{u.nome}</MenuItem>
                                ))}
                              </Select>
                            </FormControl>
                          </Grid>
                          <Grid size={{ xs: 4 }}>
                            <FormControl fullWidth size="small">
                              <InputLabel id={`vice-${key}-label`}>Vice-Coordenador</InputLabel>
                              <Select
                                labelId={`vice-${key}-label`}
                                label="Vice-Coordenador"
                                value={viceVal}
                                onChange={(e) => handleMatrixChange(key, 'viceCoordenadorId', e.target.value as string)}
                                disabled={isPending}
                                sx={{ borderRadius: 0 }}
                                data-testid={`vice-select-${key}`}
                              >
                                <MenuItem value=""><em>Nenhum</em></MenuItem>
                                {usuarios.map(u => (
                                  <MenuItem key={u.id} value={u.id}>{u.nome}</MenuItem>
                                ))}
                              </Select>
                            </FormControl>
                          </Grid>
                        </React.Fragment>
                      );
                    })}
                  </Grid>
                </div>
              </Grid>
            ) : (
              <>
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
              </>
            )}
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
