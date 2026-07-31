import React, {useState} from 'react';
import {
    Alert,
    Box,
    Button,
    Chip,
    CircularProgress,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    FormControl,
    InputLabel,
    MenuItem,
    Paper,
    Select,
    Step,
    StepLabel,
    Stepper,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    Typography,
} from '@mui/material';
import {
    CheckCircle as ValidIcon,
    CloudUpload as UploadIcon,
    Download as DownloadIcon,
    Error as ErrorIcon,
} from '@mui/icons-material';
import {useNotification} from '../context/NotificationContext';
import {useQueryClient} from '@tanstack/react-query';
import {
    confirmarImportacao,
    downloadTemplate,
    ImportacaoValidacaoResultado,
    validarImportacao
} from '../api/importacao';
import theme from '../theme';

interface ImportModalProps {
  open: boolean;
  onClose: () => void;
  onSuccess?: () => void;
  defaultEntity?: 'MEMBRO' | 'USUARIO' | 'ORGANIZACAO' | 'EVENTO';
}

const STEPS = ['Upload e Template', 'Pré-visualização e Validação', 'Confirmação'];

export const ImportModal: React.FC<ImportModalProps> = ({
  open,
  onClose,
  onSuccess,
  defaultEntity = 'MEMBRO',
}) => {
  const { showSuccess, showError } = useNotification();
  const queryClient = useQueryClient();
  const [activeStep, setActiveStep] = useState(0);
  const [entityType, setEntityType] = useState(defaultEntity);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [isValidating, setIsValidating] = useState(false);
  const [isConfirming, setIsConfirming] = useState(false);
  const [validationResult, setValidationResult] = useState<ImportacaoValidacaoResultado | null>(null);

  const handleReset = () => {
    setActiveStep(0);
    setSelectedFile(null);
    setValidationResult(null);
    setIsValidating(false);
    setIsConfirming(false);
  };

  const handleCloseModal = () => {
    handleReset();
    onClose();
  };

  const handleDownload = async (formato: 'CSV' | 'XLSX') => {
    try {
      await downloadTemplate(entityType, formato);
      showSuccess(`Modelo ${formato} de ${entityType} baixado com sucesso!`);
    } catch (err) {
      showError('Não foi possível baixar o modelo de importação.');
    }
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      setSelectedFile(e.target.files[0]);
    }
  };

  const handleProceedToValidation = async () => {
    if (!selectedFile) {
      showError('Selecione um arquivo CSV ou Excel antes de prosseguir.');
      return;
    }

    setIsValidating(true);
    try {
      const res = await validarImportacao(entityType, selectedFile);
      setValidationResult(res);
      setActiveStep(1);
    } catch (err: any) {
      showError(err.response?.data?.message || 'Falha ao processar e validar o arquivo enviado.');
    } finally {
      setIsValidating(false);
    }
  };

  const handleConfirmImport = async () => {
    if (!validationResult || validationResult.totalValidos === 0) {
      showError('Não há registros válidos para importar.');
      return;
    }

    setIsConfirming(true);
    try {
      const validItems = validationResult.itens.filter(i => i.valido).map(i => i.dados);
      const res = await confirmarImportacao(entityType, validItems);
      queryClient.invalidateQueries();
      showSuccess(`Sucesso! ${res.registrosImportados} registro(s) foram cadastrados no sistema.`);
      if (onSuccess) onSuccess();
      handleCloseModal();
    } catch (err) {
      showError('Erro ao efetivar a importação dos registros.');
    } finally {
      setIsConfirming(false);
    }
  };

  return (
    <Dialog open={open} onClose={handleCloseModal} maxWidth="md" fullWidth>
      <DialogTitle>
        Importação de Registros em Lote
      </DialogTitle>

      <DialogContent dividers>
        <Stepper activeStep={activeStep} sx={{ mb: 4 }}>
          {STEPS.map((label) => (
            <Step key={label}>
              <StepLabel>{label}</StepLabel>
            </Step>
          ))}
        </Stepper>

        {activeStep === 0 && (
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2.5, mt: 1 }}>
            <FormControl fullWidth size="small">
              <InputLabel id="import-entity-label">Tipo de Cadastro</InputLabel>
              <Select
                labelId="import-entity-label"
                value={entityType}
                label="Tipo de Cadastro"
                onChange={(e) => setEntityType(e.target.value as any)}
              >
                <MenuItem value="MEMBRO">Membros / Féis Leigos</MenuItem>
                <MenuItem value="USUARIO">Usuários do Sistema</MenuItem>
                <MenuItem value="ORGANIZACAO">Organizações / Pastorais</MenuItem>
                <MenuItem value="EVENTO">Eventos Paroquiais</MenuItem>
              </Select>
            </FormControl>

            <Alert severity="info">
              Faça o download do modelo padrão abaixo para preencher os dados corretamente antes de enviar o arquivo.
            </Alert>

            <Box sx={{ display: 'flex', gap: 2 }}>
              <Button
                variant="outlined"
                startIcon={<DownloadIcon />}
                onClick={() => handleDownload('CSV')}
                sx={{ textTransform: 'none' }}
              >
                Baixar Template CSV
              </Button>
              <Button
                variant="outlined"
                startIcon={<DownloadIcon />}
                onClick={() => handleDownload('XLSX')}
                sx={{ textTransform: 'none' }}
              >
                Baixar Template Excel (.xlsx)
              </Button>
            </Box>

            <Box sx={{ border: '2px dashed', borderColor: 'divider', p: 3, textAlign: 'center', bgcolor: theme.custom.subtlePanelBg }}>
              <Button
                variant="contained"
                component="label"
                startIcon={<UploadIcon />}
                sx={{ textTransform: 'none', mb: 1 }}
              >
                Selecionar Arquivo (.csv, .xls, .xlsx)
                <input type="file" hidden accept=".csv, .xls, .xlsx" onChange={handleFileChange} />
              </Button>
              <Typography variant="body2" color="textSecondary">
                {selectedFile ? `Arquivo selecionado: ${selectedFile.name}` : 'Nenhum arquivo selecionado'}
              </Typography>
            </Box>
          </Box>
        )}

        {activeStep === 1 && validationResult && (
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2.5, mt: 1 }}>
            <Box sx={{ display: 'flex', gap: 2 }}>
              <Chip icon={<ValidIcon />} label={`${validationResult.totalValidos} Válidos`} color="success" sx={{ borderRadius: 0 }} />
              <Chip icon={<ErrorIcon />} label={`${validationResult.totalInvalidos} Inválidos`} color="error" sx={{ borderRadius: 0 }} />
            </Box>

            <TableContainer component={Paper} elevation={0} sx={{ border: '1px solid', borderColor: 'divider', maxHeight: 320 }}>
              <Table size="small" stickyHeader>
                <TableHead>
                  <TableRow>
                    <TableCell sx={{ fontWeight: 600 }}>Linha</TableCell>
                    <TableCell sx={{ fontWeight: 600 }}>Status</TableCell>
                    <TableCell sx={{ fontWeight: 600 }}>Mensagem / Observação</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {validationResult.itens.map((item) => (
                    <TableRow key={item.linha} sx={{ bgcolor: item.valido ? '#f1f8e9' : '#ffebee' }}>
                      <TableCell>{item.linha}</TableCell>
                      <TableCell>
                        {item.valido ? (
                          <Chip label="Válido" color="success" size="small" sx={{ borderRadius: 0 }} />
                        ) : (
                          <Chip label="Erro" color="error" size="small" sx={{ borderRadius: 0 }} />
                        )}
                      </TableCell>
                      <TableCell>{item.valido ? 'Pronto para importação' : item.mensagemErro}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          </Box>
        )}

        {activeStep === 2 && validationResult && (
          <Box sx={{ textAlign: 'center', py: 2 }}>
            <Typography variant="h6" sx={{ fontWeight: 600, mb: 1 }}>
              Pronto para efetivar a gravação!
            </Typography>
            <Typography variant="body1" color="textSecondary">
              Serão importados <strong>{validationResult.totalValidos}</strong> registros válidos no banco de dados da paróquia.
            </Typography>
          </Box>
        )}
      </DialogContent>

      <DialogActions>
        <Button onClick={handleCloseModal} color="secondary" sx={{ textTransform: 'none' }}>
          Cancelar
        </Button>
        {activeStep > 0 && (
          <Button onClick={() => setActiveStep(activeStep - 1)} sx={{ textTransform: 'none' }}>
            Voltar
          </Button>
        )}
        {activeStep === 0 && (
          <Button
            variant="contained"
            onClick={handleProceedToValidation}
            disabled={!selectedFile || isValidating}
            sx={{ textTransform: 'none', fontWeight: 600 }}
          >
            {isValidating ? <CircularProgress size={24} /> : 'Validar Arquivo'}
          </Button>
        )}
        {activeStep === 1 && (
          <Button
            variant="contained"
            onClick={() => setActiveStep(2)}
            disabled={validationResult?.totalValidos === 0}
            sx={{ textTransform: 'none', fontWeight: 600 }}
          >
            Avançar para Confirmação
          </Button>
        )}
        {activeStep === 2 && (
          <Button
            variant="contained"
            color="primary"
            onClick={handleConfirmImport}
            disabled={isConfirming}
            sx={{ textTransform: 'none', fontWeight: 600 }}
          >
            {isConfirming ? <CircularProgress size={24} /> : 'Confirmar Importação'}
          </Button>
        )}
      </DialogActions>
    </Dialog>
  );
};

export default ImportModal;
