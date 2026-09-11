import React, {useState} from 'react';
import {
    Alert,
    alpha,
    Box,
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
    TablePagination,
    TableRow,
    Typography,
} from '@mui/material';
import {
    ArrowBack as ArrowBackIcon,
    ArrowForward as ArrowForwardIcon,
    Check as CheckIcon,
    CheckCircle as ValidIcon,
    CloudUpload as UploadIcon,
    Close as CloseIcon,
    Download as DownloadIcon,
    Error as ErrorIcon,
} from '@mui/icons-material';
import {useNotification} from '../context/NotificationContext';
import {extractErrorMessage} from '../utils/errorUtils';
import {useQueryClient} from '@tanstack/react-query';
import {
    confirmarImportacao,
    downloadTemplate,
    ImportacaoValidacaoResultado,
    validarImportacao
} from '../api/importacao';
import theme from '../theme';
import ActionButton from './ActionButton';

interface ImportModalProps {
  open: boolean;
  onClose: () => void;
  onSuccess?: () => void;
  defaultEntity?: 'MEMBRO' | 'USUARIO' | 'ORGANIZACAO' | 'EVENTO';
}

type EntidadeImportacao = NonNullable<ImportModalProps['defaultEntity']>;

const isEntidadeImportacao = (value: string): value is EntidadeImportacao => (
  value === 'MEMBRO' || value === 'USUARIO' || value === 'ORGANIZACAO' || value === 'EVENTO'
);

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
  const [importPage, setImportPage] = useState(0);
  const [importRowsPerPage, setImportRowsPerPage] = useState(10);

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
    } catch (err: unknown) {
      showError(extractErrorMessage(err, 'Falha ao processar e validar o arquivo enviado.'));
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
                onChange={(e) => isEntidadeImportacao(e.target.value) && setEntityType(e.target.value)}
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
              <ActionButton title="Baixar Template CSV" icon={<DownloadIcon />} color="primary" onClick={() => handleDownload('CSV')} />
              <ActionButton title="Baixar Template Excel (.xlsx)" icon={<DownloadIcon />} color="primary" onClick={() => handleDownload('XLSX')} />
            </Box>

            <Box sx={{ border: '2px dashed', borderColor: 'divider', p: 3, textAlign: 'center', bgcolor: theme.custom.subtlePanelBg }}>
              <ActionButton title="Selecionar Arquivo CSV ou Excel" icon={<UploadIcon />} color="primary" component="label">
                <input
                  type="file"
                  aria-label="Selecionar Arquivo"
                  hidden
                  accept=".csv,.xlsx"
                  onChange={handleFileChange}
                />
              </ActionButton>
              <Typography variant="body2" color="textSecondary">
                {selectedFile ? `Arquivo selecionado: ${selectedFile.name}` : 'Nenhum arquivo selecionado'}
              </Typography>
            </Box>
          </Box>
        )}

        {activeStep === 1 && validationResult && (
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2.5, mt: 1 }}>
            <Box sx={{ display: 'flex', gap: 2 }}>
              <Chip icon={<ValidIcon />} label={`${validationResult.totalValidos} Válidos`} color="success" />
              <Chip icon={<ErrorIcon />} label={`${validationResult.totalInvalidos} Inválidos`} color="error" />
            </Box>

            <TableContainer component={Paper} elevation={0} sx={{ border: '1px solid', borderColor: 'divider', maxHeight: 320 }}>
              <Table size="small" stickyHeader>
                <TableHead sx={{ backgroundColor: 'action.hover' }}>
                  <TableRow>
                    <TableCell sx={{ fontWeight: 600 }}>Linha</TableCell>
                    <TableCell sx={{ fontWeight: 600 }}>Status</TableCell>
                    <TableCell sx={{ fontWeight: 600 }}>Mensagem / Observação</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {validationResult.itens
                    .slice(importPage * importRowsPerPage, importPage * importRowsPerPage + importRowsPerPage)
                    .map((item) => (
                      <TableRow
                        key={item.linha}
                        hover
                        sx={{
                          bgcolor: (theme) => alpha(
                            item.valido ? theme.palette.success.main : theme.palette.error.main,
                            0.08
                          ),
                        }}
                      >
                        <TableCell>{item.linha}</TableCell>
                        <TableCell>
                          {item.valido ? (
                            <Chip label="Válido" color="success" size="small" />
                          ) : (
                            <Chip label="Erro" color="error" size="small" />
                          )}
                        </TableCell>
                        <TableCell>{item.valido ? 'Pronto para importação' : item.mensagemErro}</TableCell>
                      </TableRow>
                    ))}
                </TableBody>
              </Table>
              <TablePagination
                rowsPerPageOptions={[5, 10, 25]}
                component="div"
                count={validationResult.itens.length}
                rowsPerPage={importRowsPerPage}
                page={importPage}
                onPageChange={(_, newPage) => setImportPage(newPage)}
                onRowsPerPageChange={(e) => {
                  setImportRowsPerPage(parseInt(e.target.value, 10));
                  setImportPage(0);
                }}
                labelRowsPerPage="Linhas por página:"
                labelDisplayedRows={({ from, to, count }) => `${from}–${to} de ${count !== -1 ? count : `mais de ${to}`}`}
              />
            </TableContainer>
          </Box>
        )}

        {activeStep === 2 && validationResult && (
          <Box sx={{ textAlign: 'center', py: 2 }}>
            <Typography variant="h6" sx={{ fontWeight: 700, mb: 1 }}>
              Pronto para efetivar a gravação!
            </Typography>
            <Typography variant="body1" color="textSecondary">
              Serão importados <strong>{validationResult.totalValidos}</strong> registros válidos no banco de dados da paróquia.
            </Typography>
          </Box>
        )}
      </DialogContent>

      <DialogActions>
        <ActionButton title="Cancelar" icon={<CloseIcon />} onClick={handleCloseModal} color="secondary" />
        {activeStep > 0 && (
          <ActionButton title="Voltar" icon={<ArrowBackIcon />} onClick={() => setActiveStep(activeStep - 1)} color="secondary" />
        )}
        {activeStep === 0 && (
          <ActionButton
            title="Validar Arquivo"
            icon={isValidating ? <CircularProgress size={24} /> : <CheckIcon />}
            color="primary"
            onClick={handleProceedToValidation}
            disabled={!selectedFile || isValidating}
          />
        )}
        {activeStep === 1 && (
          <ActionButton
            title="Avançar para Confirmação"
            icon={<ArrowForwardIcon />}
            color="primary"
            onClick={() => setActiveStep(2)}
            disabled={validationResult?.totalValidos === 0}
          />
        )}
        {activeStep === 2 && (
          <ActionButton
            title="Confirmar Importação"
            icon={isConfirming ? <CircularProgress size={24} /> : <UploadIcon />}
            color="primary"
            onClick={handleConfirmImport}
            disabled={isConfirming}
          />
        )}
      </DialogActions>
    </Dialog>
  );
};

export default ImportModal;
