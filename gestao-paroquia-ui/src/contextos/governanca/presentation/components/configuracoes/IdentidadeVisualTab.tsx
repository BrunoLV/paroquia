import React, { useEffect, useState } from 'react';
import Alert from '@mui/material/Alert';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import CircularProgress from '@mui/material/CircularProgress';
import Grid from '@mui/material/Grid';
import Skeleton from '@mui/material/Skeleton';
import Stack from '@mui/material/Stack';
import Typography from '@mui/material/Typography';
import SaveIcon from '@mui/icons-material/Save';
import InfoOutlinedIcon from '@mui/icons-material/InfoOutlined';
import PaletteIcon from '@mui/icons-material/Palette';

import { useConfiguracaoParoquia } from '@/contextos/governanca/presentation/hooks/useConfiguracaoParoquia';
import { ColorPickerField } from './ColorPickerField';
import { LogoUploadCard } from './LogoUploadCard';
import { useNotification } from '@/shared/presentation/context/NotificationContext';
import { extractErrorMessage } from '@/shared/utils/errorUtils';

export const IdentidadeVisualTab: React.FC = () => {
  const { showSuccess, showError } = useNotification();
  const { configuracao, isLoading, isError, atualizarCores, atualizarLogo } = useConfiguracaoParoquia();

  const [corPrimaria, setCorPrimaria] = useState('#1976D2');
  const [corSecundaria, setCorSecundaria] = useState('#9C27B0');

  useEffect(() => {
    if (configuracao) {
      if (configuracao.corPrimaria) setCorPrimaria(configuracao.corPrimaria);
      if (configuracao.corSecundaria) setCorSecundaria(configuracao.corSecundaria);
    }
  }, [configuracao]);

  const handleSalvarCores = async () => {
    try {
      await atualizarCores.mutateAsync({ corPrimaria, corSecundaria });
      showSuccess('Cores institucionais atualizadas com sucesso!');
    } catch (err) {
      showError(extractErrorMessage(err, 'Erro ao atualizar cores institucionais.'));
    }
  };

  const handleUploadLogo = async (variante: 'claro' | 'escuro', arquivo: File) => {
    try {
      await atualizarLogo.mutateAsync({ variante, arquivo });
      showSuccess(`Logotipo para fundo ${variante} atualizado com sucesso!`);
    } catch (err) {
      showError(extractErrorMessage(err, `Erro ao atualizar logotipo para fundo ${variante}.`));
      throw err;
    }
  };

  if (isLoading) {
    return (
      <Stack spacing={3}>
        <Skeleton variant="rectangular" height={140} sx={{ borderRadius: 2 }} />
        <Skeleton variant="rectangular" height={160} sx={{ borderRadius: 2 }} />
        <Skeleton variant="rectangular" height={220} sx={{ borderRadius: 2 }} />
      </Stack>
    );
  }

  if (isError || !configuracao) {
    return <Alert severity="error">Erro ao carregar configurações de identidade visual da paróquia.</Alert>;
  }

  return (
    <Stack spacing={3}>
      {/* 1. Dados Institucionais SSOT (somente leitura) */}
      <Card variant="outlined" sx={{ borderRadius: 2 }}>
        <CardContent>
          <Stack spacing={2}>
            <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
              <InfoOutlinedIcon color="primary" />
              <Typography variant="h6" sx={{ fontWeight: 600 }}>
                Dados Cadastrais da Paróquia
              </Typography>
            </Stack>
            <Alert severity="info" sx={{ py: 0.5 }}>
              Estes dados são centralizados a partir da Igreja Matriz e do Governo Eclesiástico para evitar duplicidade cadastral.
            </Alert>
            <Grid container spacing={2}>
              <Grid size={{ xs: 12, md: 6 }}>
                <Typography variant="caption" color="text.secondary">
                  Nome da Paróquia
                </Typography>
                <Typography variant="body1" sx={{ fontWeight: 500 }}>
                  {configuracao.nome || '-'}
                </Typography>
              </Grid>
              <Grid size={{ xs: 12, md: 6 }}>
                <Typography variant="caption" color="text.secondary">
                  Diocese / Arquidiocese
                </Typography>
                <Typography variant="body1" sx={{ fontWeight: 500 }}>
                  {configuracao.diocese || '-'}
                </Typography>
              </Grid>
              <Grid size={{ xs: 12, md: 4 }}>
                <Typography variant="caption" color="text.secondary">
                  CNPJ
                </Typography>
                <Typography variant="body2">
                  {configuracao.cnpj || 'Não informado'}
                </Typography>
              </Grid>
              <Grid size={{ xs: 12, md: 4 }}>
                <Typography variant="caption" color="text.secondary">
                  Telefone
                </Typography>
                <Typography variant="body2">
                  {configuracao.telefone || 'Não informado'}
                </Typography>
              </Grid>
              <Grid size={{ xs: 12, md: 4 }}>
                <Typography variant="caption" color="text.secondary">
                  E-mail
                </Typography>
                <Typography variant="body2">
                  {configuracao.email || 'Não informado'}
                </Typography>
              </Grid>
              <Grid size={{ xs: 12 }}>
                <Typography variant="caption" color="text.secondary">
                  Endereço da Matriz
                </Typography>
                <Typography variant="body2">
                  {configuracao.endereco || 'Não informado'}
                </Typography>
              </Grid>
            </Grid>
          </Stack>
        </CardContent>
      </Card>

      {/* 2. Cores Institucionais */}
      <Card variant="outlined" sx={{ borderRadius: 2 }}>
        <CardContent>
          <Stack spacing={2.5}>
            <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
              <PaletteIcon color="primary" />
              <Typography variant="h6" sx={{ fontWeight: 600 }}>
                Cores Institucionais
              </Typography>
            </Stack>
            <Typography variant="body2" color="text.secondary">
              Defina a paleta primária e secundária utilizada na identidade do sistema e relatórios da paróquia.
            </Typography>

            <Grid container spacing={3}>
              <Grid size={{ xs: 12, sm: 6 }}>
                <ColorPickerField
                  label="Cor Primária"
                  value={corPrimaria}
                  onChange={setCorPrimaria}
                  helperText="Cor principal da interface e cabeçalhos"
                />
              </Grid>
              <Grid size={{ xs: 12, sm: 6 }}>
                <ColorPickerField
                  label="Cor Secundária"
                  value={corSecundaria}
                  onChange={setCorSecundaria}
                  helperText="Cor de destaque para ações e etiquetas"
                />
              </Grid>
            </Grid>

            <Box sx={{ display: 'flex', justifyContent: 'flex-start', pt: 1 }}>
              <Button
                variant="contained"
                size="small"
                startIcon={atualizarCores.isPending ? <CircularProgress size={16} color="inherit" /> : <SaveIcon />}
                onClick={handleSalvarCores}
                disabled={atualizarCores.isPending}
              >
                Salvar Cores
              </Button>
            </Box>
          </Stack>
        </CardContent>
      </Card>

      {/* 3. Logotipos (Upload binarizado no banco de dados) */}
      <Card variant="outlined" sx={{ borderRadius: 2 }}>
        <CardContent>
          <Stack spacing={2.5}>
            <Typography variant="h6" sx={{ fontWeight: 600 }}>
              Logotipos Oficiais da Paróquia
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Faça o upload dos logotipos nos dois formatos de fundo para garantir contraste e legibilidade ideais em qualquer tela.
            </Typography>

            <Grid container spacing={3}>
              <Grid size={{ xs: 12, md: 6 }}>
                <LogoUploadCard
                  titulo="Logotipo para Fundo Claro"
                  subtitulo="Utilizado em cabeçalhos claros, tela de login e impressões."
                  variante="claro"
                  logoUrl="/api/v1/configuracao-paroquia/logo/claro"
                  temLogo={configuracao.temLogoFundoClaro}
                  onUpload={(arquivo) => handleUploadLogo('claro', arquivo)}
                  isUploading={atualizarLogo.isPending}
                />
              </Grid>
              <Grid size={{ xs: 12, md: 6 }}>
                <LogoUploadCard
                  titulo="Logotipo para Fundo Escuro"
                  subtitulo="Utilizado em temas escuros e superfícies escuras."
                  variante="escuro"
                  logoUrl="/api/v1/configuracao-paroquia/logo/escuro"
                  temLogo={configuracao.temLogoFundoEscuro}
                  onUpload={(arquivo) => handleUploadLogo('escuro', arquivo)}
                  isUploading={atualizarLogo.isPending}
                />
              </Grid>
            </Grid>
          </Stack>
        </CardContent>
      </Card>
    </Stack>
  );
};
