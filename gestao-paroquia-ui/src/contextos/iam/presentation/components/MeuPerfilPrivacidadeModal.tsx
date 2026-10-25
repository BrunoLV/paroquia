import React, { useState } from 'react';
import {
  Avatar,
  Box,
  Card,
  CardContent,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Divider,
  FormControlLabel,
  Stack,
  Switch,
  Typography,
} from '@mui/material';
import PersonIcon from '@mui/icons-material/Person';
import DownloadIcon from '@mui/icons-material/Download';
import SaveIcon from '@mui/icons-material/Save';
import CloseIcon from '@mui/icons-material/Close';
import ActionButton from '@/shared/presentation/components/ActionButton';
import { DIALOG_CONTENT_SCROLL_SX } from '@/shared/presentation/theme/dialogStyles';
import { useAuth } from '@/contextos/iam/presentation/auth/AuthContext';
import { useNotification } from '@/shared/presentation/context/NotificationContext';
import {
  exportarMeusDados,
  atualizarPreferenciasPrivacidade,
} from '@/contextos/governanca/infrastructure/http/ajustesCadastrais';

interface MeuPerfilPrivacidadeModalProps {
  open: boolean;
  onClose: () => void;
}

/**
 * Modal de Perfil e Exercício dos Direitos do Titular sob a LGPD:
 * - Portabilidade dos dados cadastrais e sacramentais (Art. 18, V)
 * - Gestão e revogação de consentimento de uso de imagem e aniversários (Art. 8º, § 5º)
 */
export const MeuPerfilPrivacidadeModal: React.FC<MeuPerfilPrivacidadeModalProps> = ({
  open,
  onClose,
}) => {
  const { user } = useAuth();
  let showSuccess = (_msg: string) => {};
  let showError = (_msg: string) => {};
  try {
    const notify = useNotification();
    showSuccess = notify.showSuccess;
    showError = notify.showError;
  } catch {
    // Fallback gracioso caso renderizado fora de NotificationProvider em testes
  }

  const [usoImagemAutorizado, setUsoImagemAutorizado] = useState(false);
  const [permitirDivulgacaoAniversario, setPermitirDivulgacaoAniversario] = useState(true);
  const [baixandoPortabilidade, setBaixandoPortabilidade] = useState(false);
  const [salvandoPreferencias, setSalvandoPreferencias] = useState(false);

  const handleBaixarDados = async () => {
    try {
      setBaixandoPortabilidade(true);
      const dados = await exportarMeusDados();
      const jsonStr = JSON.stringify(dados, null, 2);
      const blob = new Blob([jsonStr], { type: 'application/json' });
      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `portabilidade-dados-titular-${user?.username || 'usuario'}.json`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      URL.revokeObjectURL(url);
      showSuccess('Arquivo de portabilidade exportado com sucesso.');
    } catch {
      showError('Não foi possível gerar o arquivo de portabilidade. Tente novamente mais tarde.');
    } finally {
      setBaixandoPortabilidade(false);
    }
  };

  const handleSalvarPreferencias = async () => {
    try {
      setSalvandoPreferencias(true);
      await atualizarPreferenciasPrivacidade({
        usoImagemAutorizado,
        permitirDivulgacaoAniversario,
      });
      showSuccess('Preferências de privacidade atualizadas com sucesso.');
    } catch {
      showError('Erro ao atualizar preferências de privacidade.');
    } finally {
      setSalvandoPreferencias(false);
    }
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle sx={{ fontWeight: 700, display: 'flex', alignItems: 'center', gap: 1 }}>
        <PersonIcon color="primary" />
        Meu Perfil & Privacidade (LGPD)
      </DialogTitle>

      <DialogContent dividers sx={DIALOG_CONTENT_SCROLL_SX}>
        <Stack spacing={2.5}>
          {/* Dados do Usuário Logado */}
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, p: 1 }}>
            <Avatar
              sx={{
                width: 48,
                height: 48,
                bgcolor: 'primary.main',
                color: 'primary.contrastText',
                fontWeight: 700,
                fontSize: '1.2rem',
              }}
            >
              {user?.name?.slice(0, 1)?.toLocaleUpperCase('pt-BR') || 'U'}
            </Avatar>
            <Box>
              <Typography variant="subtitle1" sx={{ fontWeight: 700 }}>
                {user?.name}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Login / Usuário: {user?.username}
              </Typography>
              {user?.roles && user.roles.length > 0 && (
                <Typography variant="caption" color="text.secondary">
                  Perfis: {user.roles.join(', ')}
                </Typography>
              )}
            </Box>
          </Box>

          <Divider />

          {/* Portabilidade de Dados - Art. 18, V */}
          <Card variant="outlined" sx={{ borderRadius: 2 }}>
            <CardContent>
              <Typography variant="subtitle2" sx={{ fontWeight: 700, mb: 1 }}>
                Portabilidade de Dados (Art. 18, V, LGPD)
              </Typography>
              <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                Você pode solicitar e baixar uma cópia completa dos seus dados pessoais e registros
                armazenados no sistema em formato legível por máquina (JSON).
              </Typography>
              <ActionButton
                title="Baixar cópia dos meus dados em JSON"
                label={baixandoPortabilidade ? 'Gerando arquivo...' : 'Baixar Meus Dados (JSON)'}
                icon={<DownloadIcon />}
                onClick={handleBaixarDados}
                disabled={baixandoPortabilidade}
                color="secondary"
              />
            </CardContent>
          </Card>

          {/* Gestão de Consentimento e Preferências - Art. 8, § 5 */}
          <Card variant="outlined" sx={{ borderRadius: 2 }}>
            <CardContent>
              <Typography variant="subtitle2" sx={{ fontWeight: 700, mb: 1 }}>
                Preferências e Consentimento (Art. 8º, § 5º, LGPD)
              </Typography>
              <Typography variant="caption" color="text.secondary" sx={{ mb: 1.5, display: 'block' }}>
                Gerencie permissões para finalidades pastorais opcionais. Você pode revogar ou conceder
                seu consentimento livremente a qualquer momento.
              </Typography>

              <Stack spacing={1}>
                <FormControlLabel
                  control={
                    <Switch
                      size="small"
                      checked={usoImagemAutorizado}
                      onChange={(e) => setUsoImagemAutorizado(e.target.checked)}
                    />
                  }
                  label={
                    <Typography variant="body2">
                      Autorizo o uso de minha imagem em fotografias e vídeos pastorais paroquiais.
                    </Typography>
                  }
                />

                <FormControlLabel
                  control={
                    <Switch
                      size="small"
                      checked={permitirDivulgacaoAniversario}
                      onChange={(e) => setPermitirDivulgacaoAniversario(e.target.checked)}
                    />
                  }
                  label={
                    <Typography variant="body2">
                      Permito a divulgação do meu aniversário em celebrações e murais da comunidade.
                    </Typography>
                  }
                />
              </Stack>

              <Box sx={{ mt: 2, display: 'flex', justifyContent: 'flex-end' }}>
                <ActionButton
                  title="Salvar preferências de consentimento"
                  label={salvandoPreferencias ? 'Salvando...' : 'Salvar Preferências'}
                  icon={<SaveIcon />}
                  onClick={handleSalvarPreferencias}
                  disabled={salvandoPreferencias}
                  color="primary"
                />
              </Box>
            </CardContent>
          </Card>
        </Stack>
      </DialogContent>

      <DialogActions>
        <ActionButton title="Fechar modal de perfil" label="Fechar" icon={<CloseIcon />} onClick={onClose} />
      </DialogActions>
    </Dialog>
  );
};

export default MeuPerfilPrivacidadeModal;
