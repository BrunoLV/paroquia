import React, { useState } from 'react';
import {
  Box,
  Card,
  CardContent,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Link,
  Stack,
  Typography,
  TextField,
  Alert,
} from '@mui/material';
import ShieldIcon from '@mui/icons-material/Shield';
import EmailIcon from '@mui/icons-material/Email';
import PhoneIcon from '@mui/icons-material/Phone';
import PersonIcon from '@mui/icons-material/Person';
import MenuBookIcon from '@mui/icons-material/MenuBook';
import CloseIcon from '@mui/icons-material/Close';
import ShareIcon from '@mui/icons-material/Share';
import SendIcon from '@mui/icons-material/Send';
import OpenInNewIcon from '@mui/icons-material/OpenInNew';
import ActionButton from '@/shared/presentation/components/ActionButton';
import { DIALOG_CONTENT_SCROLL_SX } from '@/shared/presentation/theme/dialogStyles';
import type { ParoquiaConfiguracao } from '@/contextos/governanca/infrastructure/http/configuracaoParoquia';
import { solicitarAoDpo } from '@/contextos/governanca/infrastructure/http/consentimentoPublico';
import { useNotification } from '@/shared/presentation/context/NotificationContext';
import { extractErrorMessage } from '@/shared/utils/errorUtils';
import { useNavigate } from 'react-router-dom';

interface DpoPublicoDialogProps {
  open: boolean;
  onClose: () => void;
  configuracao?: ParoquiaConfiguracao | null;
}

/**
 * Diálogo de transparência, uso compartilhado e canal de contato do Encarregado de Dados (DPO),
 * em conformidade com o Art. 41, Art. 18 e Art. 9º da LGPD (Lei 13.709/2018).
 */
export const DpoPublicoDialog: React.FC<DpoPublicoDialogProps> = ({
  open,
  onClose,
  configuracao,
}) => {
  const navigate = useNavigate();
  const { showSuccess, showError } = useNotification();

  const [abaForm, setAbaForm] = useState(false);
  const [nome, setNome] = useState('');
  const [email, setEmail] = useState('');
  const [cpf, setCpf] = useState('');
  const [tipoSolicitacao, setTipoSolicitacao] = useState('CONFIRMACAO_ACESSO');
  const [mensagem, setMensagem] = useState('');
  const [enviando, setEnviando] = useState(false);
  const [protocoloGerado, setProtocoloGerado] = useState<string | null>(null);

  const dpoNome = configuracao?.dpoNome || 'Comitê Paroquial de Privacidade e Proteção de Dados';
  const dpoEmail = configuracao?.dpoEmail || 'dpo@nsfatima.org.br';
  const dpoTelefone = configuracao?.dpoTelefone || '(44) 3028-1500';

  const handleEnviarSolicitacao = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!nome.trim() || !email.trim() || !mensagem.trim()) {
      showError('Nome, e-mail e mensagem são obrigatórios.');
      return;
    }
    setEnviando(true);
    try {
      const resp = await solicitarAoDpo({
        titularNome: nome.trim(),
        titularEmail: email.trim(),
        titularCpf: cpf.replace(/\D/g, '') || undefined,
        tipoSolicitacao,
        mensagem: mensagem.trim(),
      });
      setProtocoloGerado(resp.protocolo);
      showSuccess(`Solicitação registrada com sucesso! Protocolo: ${resp.protocolo}`);
    } catch (err) {
      showError(extractErrorMessage(err, 'Falha ao registrar solicitação ao DPO.'));
    } finally {
      setEnviando(false);
    }
  };

  const handleReset = () => {
    setAbaForm(false);
    setNome('');
    setEmail('');
    setCpf('');
    setMensagem('');
    setProtocoloGerado(null);
  };

  return (
    <Dialog open={open} onClose={() => { handleReset(); onClose(); }} maxWidth="sm" fullWidth>
      <DialogTitle sx={{ fontWeight: 700, display: 'flex', alignItems: 'center', gap: 1 }}>
        <ShieldIcon color="primary" />
        Privacidade & Encarregado de Dados (DPO)
      </DialogTitle>

      <DialogContent dividers sx={DIALOG_CONTENT_SCROLL_SX}>
        {!abaForm ? (
          <Stack spacing={2.5}>
            <Typography variant="body2" color="text.secondary">
              A Paróquia trata dados pessoais com base na Lei Geral de Proteção de Dados Pessoais
              (Lei nº 13.709/2018), adotando medidas de segurança, privacidade por design e respeito aos
              direitos fundamentais de liberdade e privacidade.
            </Typography>

            {/* Canal de Atendimento do DPO */}
            <Card variant="outlined" sx={{ borderRadius: 2 }}>
              <CardContent>
                <Typography variant="subtitle2" sx={{ fontWeight: 700, mb: 1.5, display: 'flex', alignItems: 'center', gap: 1 }}>
                  <PersonIcon color="primary" fontSize="small" />
                  Canais de Atendimento do Encarregado (DPO)
                </Typography>

                <Stack spacing={1.5}>
                  <Box>
                    <Typography variant="caption" color="text.secondary" sx={{ fontWeight: 600 }}>
                      Encarregado(a) Indicado(a):
                    </Typography>
                    <Typography variant="body2" sx={{ fontWeight: 500 }}>
                      {dpoNome}
                    </Typography>
                  </Box>

                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <EmailIcon fontSize="small" color="action" />
                    <Typography variant="body2">
                      E-mail:{' '}
                      <Link href={`mailto:${dpoEmail}`} underline="hover">
                        {dpoEmail}
                      </Link>
                    </Typography>
                  </Box>

                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <PhoneIcon fontSize="small" color="action" />
                    <Typography variant="body2">
                      Telefone:{' '}
                      <Link href={`tel:${dpoTelefone.replace(/\D/g, '')}`} underline="hover">
                        {dpoTelefone}
                      </Link>
                    </Typography>
                  </Box>

                  <Typography variant="caption" color="text.secondary" sx={{ pt: 1 }}>
                    Prazo legal de resposta a requisições de titulares: até 15 (quinze) dias (Art. 19, II, LGPD).
                  </Typography>

                  <Box sx={{ pt: 1 }}>
                    <ActionButton
                      title="Enviar Solicitação Direta ao DPO"
                      label="Fazer Solicitação Formal ao DPO"
                      icon={<SendIcon />}
                      onClick={() => setAbaForm(true)}
                      variant="outlined"
                      size="small"
                      sx={{ textTransform: 'none' }}
                    />
                  </Box>
                </Stack>
              </CardContent>
            </Card>

            {/* Uso Compartilhado Eclesial */}
            <Card variant="outlined" sx={{ borderRadius: 2 }}>
              <CardContent>
                <Typography variant="subtitle2" sx={{ fontWeight: 700, mb: 1, display: 'flex', alignItems: 'center', gap: 1 }}>
                  <ShareIcon color="primary" fontSize="small" />
                  Uso Compartilhado de Dados Eclesiais (Art. 18, VII, LGPD)
                </Typography>
                <Typography variant="body2" color="text.secondary" component="ul" sx={{ pl: 2, m: 0 }}>
                  <li><strong>Mitra / Cúria Diocesana:</strong> Para arquivos históricos, habilitações matrimoniais e registros eclesiásticos.</li>
                  <li><strong>Tribunal Eclesiástico Diocesano:</strong> Para instrução de causas e processos canônicos específicos.</li>
                  <li><strong>Instituições Bancárias:</strong> Exclusivamente para processamento voluntário do dízimo e doações bancárias autorizadas.</li>
                </Typography>
                <Typography variant="caption" color="error.main" sx={{ display: 'block', mt: 1, fontWeight: 600 }}>
                  * Vedada expressamente qualquer comercialização ou cessão onerosa a terceiros (Art. 11, § 4º).
                </Typography>
              </CardContent>
            </Card>

            {/* Direitos do Titular */}
            <Card variant="outlined" sx={{ borderRadius: 2 }}>
              <CardContent>
                <Typography variant="subtitle2" sx={{ fontWeight: 700, mb: 1 }}>
                  Seus Direitos como Titular (Art. 18, LGPD)
                </Typography>
                <Typography variant="body2" color="text.secondary" component="ul" sx={{ pl: 2, m: 0 }}>
                  <li>Confirmação da existência de tratamento e acesso aos dados.</li>
                  <li>Correção de dados incompletos, inexatos ou desatualizados.</li>
                  <li>Portabilidade dos dados para outra entidade (formato JSON).</li>
                  <li>Revogação imediata do consentimento para imagem, avisos e aniversários.</li>
                </Typography>
                <Box sx={{ mt: 1.5 }}>
                  <ActionButton
                    title="Acessar Portal de Consentimento do Fiel"
                    label="Portal de Autoatendimento do Fiel"
                    icon={<OpenInNewIcon />}
                    onClick={() => {
                      onClose();
                      navigate('/portal/consentimento');
                    }}
                    size="small"
                    variant="text"
                    sx={{ textTransform: 'none' }}
                  />
                </Box>
              </CardContent>
            </Card>

            {/* Salvaguarda Canônica */}
            <Card variant="outlined" sx={{ borderRadius: 2, bgcolor: 'background.default' }}>
              <CardContent>
                <Typography variant="subtitle2" sx={{ fontWeight: 700, mb: 1, display: 'flex', alignItems: 'center', gap: 1 }}>
                  <MenuBookIcon color="secondary" fontSize="small" />
                  Salvaguarda Canônica e Eclesiástica (Art. 16, I, LGPD)
                </Typography>
                <Typography variant="caption" color="text.secondary" sx={{ display: 'block' }}>
                  Em virtude da fé pública eclesiástica perpétua dos assentos sacramentais
                  (batismo, matrimônio, crisma e óbito) e cumprimento de obrigação legal e canônica (Cân. 535),
                  os registros históricos não sofrem eliminação física, sendo retificados exclusivamente por averbação oficial.
                </Typography>
              </CardContent>
            </Card>
          </Stack>
        ) : (
          <Box component="form" onSubmit={handleEnviarSolicitacao}>
            <Typography variant="subtitle1" sx={{ fontWeight: 'bold' }} gutterBottom>
              Formulário de Solicitação ao Encarregado (DSAR)
            </Typography>
            <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mb: 2 }}>
              Exercício dos direitos do titular de dados (Art. 18 da LGPD). Prazo de atendimento: até 15 dias.
            </Typography>

            {protocoloGerado ? (
              <Alert severity="success" sx={{ mb: 2 }}>
                Solicitação registrada com sucesso! Guarde o seu número de protocolo para acompanhamento:
                <Typography variant="subtitle2" sx={{ fontWeight: 'bold', mt: 1 }}>
                  {protocoloGerado}
                </Typography>
              </Alert>
            ) : (
              <Stack spacing={2}>
                <TextField
                  size="small"
                  fullWidth
                  label="Seu Nome Completo"
                  value={nome}
                  onChange={(e) => setNome(e.target.value)}
                  required
                />
                <TextField
                  size="small"
                  fullWidth
                  label="Seu E-mail para Contato"
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                />
                <TextField
                  size="small"
                  fullWidth
                  label="CPF (Opcional, para localização de prontuário)"
                  value={cpf}
                  onChange={(e) => setCpf(e.target.value)}
                />
                <TextField
                  size="small"
                  fullWidth
                  label="Tipo de Direito Exercido"
                  value={tipoSolicitacao}
                  onChange={(e) => setTipoSolicitacao(e.target.value)}
                  placeholder="Ex: Confirmação de dados, Correção, Esclarecimento"
                  required
                />
                <TextField
                  size="small"
                  fullWidth
                  label="Descrição da Solicitação"
                  value={mensagem}
                  onChange={(e) => setMensagem(e.target.value)}
                  multiline
                  rows={3}
                  required
                />
              </Stack>
            )}
          </Box>
        )}
      </DialogContent>

      <DialogActions sx={{ p: 2 }}>
        {abaForm && !protocoloGerado ? (
          <>
            <ActionButton
              title="Voltar"
              label="Voltar aos Canais"
              icon={<CloseIcon />}
              onClick={() => setAbaForm(false)}
              variant="outlined"
            />
            <ActionButton
              title="Enviar Solicitação"
              label="Enviar ao DPO"
              icon={<SendIcon />}
              onClick={() => {}}
              type="submit"
              disabled={enviando}
            />
          </>
        ) : (
          <ActionButton title="Fechar diálogo" label="Fechar" icon={<CloseIcon />} onClick={() => { handleReset(); onClose(); }} />
        )}
      </DialogActions>
    </Dialog>
  );
};

export default DpoPublicoDialog;
