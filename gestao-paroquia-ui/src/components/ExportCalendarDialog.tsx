import React, {useState} from 'react';
import {
    Box,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    FormControl,
    FormControlLabel,
    FormLabel,
    Grid,
    InputLabel,
    MenuItem,
    Radio,
    RadioGroup,
    Select,
    TextField,
} from '@mui/material';

import {Close as CloseIcon, Download as DownloadIcon} from '@mui/icons-material';
import {Organizacao} from '../api/types';
import ActionButton from './ActionButton';

export interface ExportCalendarDialogProps {
  open: boolean;
  onClose: () => void;
  organizations: Organizacao[];
}

type FormatoExportacao = 'PDF' | 'EXCEL' | 'CSV';
type OpcaoPeriodo = 'ANO' | 'MES' | 'PERIODO';

const isFormatoExportacao = (value: string): value is FormatoExportacao => ['PDF', 'EXCEL', 'CSV'].includes(value);
const isOpcaoPeriodo = (value: string): value is OpcaoPeriodo => ['ANO', 'MES', 'PERIODO'].includes(value);

const CATEGORIES = [
  'PASTORAL',
  'SOCIAL',
  'LITURGICO',
  'ADMINISTRATIVO',
  'SACRAMENTAL',
  'FORMATIVO',
  'ASSISTENCIAL',
];

export const ExportCalendarDialog: React.FC<ExportCalendarDialogProps> = ({ open, onClose, organizations }) => {
  const currentYear = new Date().getFullYear();
  const currentMonth = new Date().getMonth() + 1;

  const [formato, setFormato] = useState<FormatoExportacao>('PDF');
  const [opcao, setOpcao] = useState<OpcaoPeriodo>('MES');
  const [ano, setAno] = useState<number>(currentYear);
  const [mes, setMes] = useState<number>(currentMonth);
  const [dataInicio, setDataInicio] = useState<string>('');
  const [dataFim, setDataFim] = useState<string>('');
  const [selectedOrg, setSelectedOrg] = useState<string>('');
  const [selectedCategory, setSelectedCategory] = useState<string>('');

  const handleExport = () => {
    const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1';
    let url = `${API_URL}/eventos/exportar?formato=${formato}&opcao=${opcao}`;

    if (opcao === 'ANO') {
      url += `&ano=${ano}`;
    } else if (opcao === 'MES') {
      url += `&ano=${ano}&mes=${mes}`;
    } else if (opcao === 'PERIODO') {
      const startIso = dataInicio ? new Date(dataInicio).toISOString() : new Date().toISOString();
      // Ajusta o fim do dia para 23:59:59 para pegar todos os eventos do último dia selecionado
      const endIso = dataFim ? new Date(dataFim + 'T23:59:59').toISOString() : new Date().toISOString();
      url += `&dataInicio=${startIso}&dataFim=${endIso}`;
    }

    if (selectedOrg) {
      url += `&organizacaoIds=${selectedOrg}`;
    }
    if (selectedCategory) {
      url += `&categorias=${selectedCategory}`;
    }

    // Abre em uma nova janela/aba para iniciar o download do navegador
    window.open(url, '_blank');
    onClose();
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="xs" fullWidth>
      <DialogTitle>Exportar Calendário</DialogTitle>
      <DialogContent dividers>
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2.5, mt: 1 }}>
          
          <FormControl>
            <FormLabel sx={{ fontWeight: 600, fontSize: '0.75rem', mb: 0.5 }}>Formato de Saída:</FormLabel>
            <RadioGroup row value={formato} onChange={(e) => isFormatoExportacao(e.target.value) && setFormato(e.target.value)}>
              <FormControlLabel value="PDF" control={<Radio size="small" />} label="PDF" />
              <FormControlLabel value="EXCEL" control={<Radio size="small" />} label="Excel" />
              <FormControlLabel value="CSV" control={<Radio size="small" />} label="CSV" />
            </RadioGroup>
          </FormControl>

          <FormControl>
            <FormLabel sx={{ fontWeight: 600, fontSize: '0.75rem', mb: 0.5 }}>Filtrar Período por:</FormLabel>
            <RadioGroup row value={opcao} onChange={(e) => isOpcaoPeriodo(e.target.value) && setOpcao(e.target.value)}>
              <FormControlLabel value="ANO" control={<Radio size="small" />} label="Ano" />
              <FormControlLabel value="MES" control={<Radio size="small" />} label="Mês" />
              <FormControlLabel value="PERIODO" control={<Radio size="small" />} label="Período" />
            </RadioGroup>
          </FormControl>

          <Box>
            {opcao === 'ANO' && (
              <TextField
                label="Ano"
                type="number"
                value={ano}
                onChange={(e) => setAno(parseInt(e.target.value) || currentYear)}
                fullWidth
                size="small"
                slotProps={{ htmlInput: { min: 2000, max: 2100 } }}
              />
            )}

            {opcao === 'MES' && (
              <Grid container spacing={2}>
                <Grid size={{ xs: 6 }}>
                  <FormControl fullWidth size="small">
                    <InputLabel id="export-month-label">Mês</InputLabel>
                    <Select
                      labelId="export-month-label"
                      value={mes}
                      label="Mês"
                      onChange={(e) => setMes(Number(e.target.value))}
                    >
                      <MenuItem value={1}>Janeiro</MenuItem>
                      <MenuItem value={2}>Fevereiro</MenuItem>
                      <MenuItem value={3}>Março</MenuItem>
                      <MenuItem value={4}>Abril</MenuItem>
                      <MenuItem value={5}>Maio</MenuItem>
                      <MenuItem value={6}>Junho</MenuItem>
                      <MenuItem value={7}>Julho</MenuItem>
                      <MenuItem value={8}>Agosto</MenuItem>
                      <MenuItem value={9}>Setembro</MenuItem>
                      <MenuItem value={10}>Outubro</MenuItem>
                      <MenuItem value={11}>Novembro</MenuItem>
                      <MenuItem value={12}>Dezembro</MenuItem>
                    </Select>
                  </FormControl>
                </Grid>
                <Grid size={{ xs: 6 }}>
                  <TextField
                    label="Ano"
                    type="number"
                    value={ano}
                    onChange={(e) => setAno(parseInt(e.target.value) || currentYear)}
                    fullWidth
                    size="small"
                    slotProps={{ htmlInput: { min: 2000, max: 2100 } }}
                  />
                </Grid>
              </Grid>
            )}

            {opcao === 'PERIODO' && (
              <Grid container spacing={2}>
                <Grid size={{ xs: 6 }}>
                  <TextField
                    label="Data Início"
                    type="date"
                    value={dataInicio}
                    onChange={(e) => setDataInicio(e.target.value)}
                    fullWidth
                    size="small"
                    slotProps={{ inputLabel: { shrink: true } }}
                  />
                </Grid>
                <Grid size={{ xs: 6 }}>
                  <TextField
                    label="Data Fim"
                    type="date"
                    value={dataFim}
                    onChange={(e) => setDataFim(e.target.value)}
                    fullWidth
                    size="small"
                    slotProps={{ inputLabel: { shrink: true } }}
                  />
                </Grid>
              </Grid>
            )}
          </Box>

          <Box sx={{ borderTop: '1px dashed', borderColor: 'divider', pt: 2, display: 'flex', flexDirection: 'column', gap: 2 }}>
            <FormControl fullWidth size="small">
              <InputLabel id="export-org-label">Organização Responsável</InputLabel>
              <Select
                labelId="export-org-label"
                value={selectedOrg}
                label="Organização Responsável"
                onChange={(e) => setSelectedOrg(e.target.value)}
              >
                <MenuItem value="">
                  <em>Todas as organizações</em>
                </MenuItem>
                {organizations.map((org) => (
                  <MenuItem key={org.id} value={org.id}>
                    {org.nome}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>

            <FormControl fullWidth size="small">
              <InputLabel id="export-category-label">Categoria</InputLabel>
              <Select
                labelId="export-category-label"
                value={selectedCategory}
                label="Categoria"
                onChange={(e) => setSelectedCategory(e.target.value)}
              >
                <MenuItem value="">
                  <em>Todas as categorias</em>
                </MenuItem>
                {CATEGORIES.map((cat) => (
                  <MenuItem key={cat} value={cat}>
                    {cat}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          </Box>

        </Box>
      </DialogContent>
      <DialogActions>
        <ActionButton title="Cancelar" icon={<CloseIcon />} onClick={onClose} color="secondary" />
        <ActionButton
          title="Exportar Calendário"
          icon={<DownloadIcon />}
          onClick={handleExport}
          color="primary"
          disabled={opcao === 'PERIODO' && (!dataInicio || !dataFim)}
        />
      </DialogActions>
    </Dialog>
  );
};
