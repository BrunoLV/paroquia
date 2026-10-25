import React, { useState, useEffect } from 'react';
import {
  Box,
  Container,
  Paper,
  Typography,
  Card,
  CardContent,
  TextField,
  FormControlLabel,
  Checkbox,
  Switch,
  Divider,
  Alert,
  Chip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  LinearProgress,
} from '@mui/material';
import ShieldIcon from '@mui/icons-material/Shield';
import LockOpenIcon from '@mui/icons-material/LockOpen';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import BlockIcon from '@mui/icons-material/Block';
import PrintIcon from '@mui/icons-material/Print';
import RefreshIcon from '@mui/icons-material/Refresh';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import ActionButton from '@/shared/presentation/components/ActionButton';
import { DIALOG_CONTENT_SCROLL_SX } from '@/shared/presentation/theme/dialogStyles';
import { useNotification } from '@/shared/presentation/context/NotificationContext';
import { extractErrorMessage } from '@/shared/utils/errorUtils';
import {
  solicitarCodigoConsentimento,
  validarCodigoConsentimento,
  manifestarConsentimento,
  revogarConsentimento,
  ValidarCodigoConsentimentoResponse,
  ComprovanteConsentimentoResponse,
} from '@/contextos/governanca/infrastructure/http/consentimentoPublico';

export const formatarCpfInput = (valor: string): string => {
  const digitos = valor.replace(/\D/g, '').slice(0, 11);
  if (digitos.length <= 3) return digitos;
  if (digitos.length <= 6) return `${digitos.slice(0, 3)}.${digitos.slice(3)}`;
  if (digitos.length <= 9) return `${digitos.slice(0, 3)}.${digitos.slice(3, 6)}.${digitos.slice(6)}`;
  return `${digitos.slice(0, 3)}.${digitos.slice(3, 6)}.${digitos.slice(6, 9)}-${digitos.slice(9)}`;
};

const PortalConsentimentoPage: React.FC = () => {
  const { showSuccess, showError } = useNotification();

  // Etapa atual: 1 = CPF, 2 = OTP, 3 = Painel de Decisão, 4 = Comprovante
  const [etapa, setEtapa] = useState<1 | 2 | 3 | 4>(1);
  const [carregando, setCarregando] = useState(false);

  // Etapa 1
  const [cpf, setCpf] = useState('');

  // Etapa 2
  const [codigoOtp, setCodigoOtp] = useState('');
  const [emailMascarado, setEmailMascarado] = useState('');
  const [tempoRestante, setTempoRestante] = useState(600);

  // Etapa 3 (Sessão validada)
  const [dadosSessao, setDadosSessao] = useState<ValidarCodigoConsentimentoResponse | null>(null);
  const [termoAceito, setTermoAceito] = useState(false);
  const [usoImagem, setUsoImagem] = useState(false);
  const [permitirAniversario, setPermitirAniversario] = useState(false);
  const [responsavelNome, setResponsavelNome] = useState('');
  const [responsavelCpf, setResponsavelCpf] = useState('');

  // Diálogo de confirmação de revogação
  const [dialogoRevogacaoAberto, setDialogoRevogacaoAberto] = useState(false);

  // Etapa 4
  const [comprovante, setComprovante] = useState<ComprovanteConsentimentoResponse | null>(null);

  // Timer para expiração do OTP
  useEffect(() => {
    if (etapa !== 2 || tempoRestante <= 0) return;
    const interval = setInterval(() => {
      setTempoRestante((prev) => (prev > 0 ? prev - 1 : 0));
    }, 1000);
    return () => clearInterval(interval);
  }, [etapa, tempoRestante]);

  const handleSolicitarCodigo = async (e: React.FormEvent) => {
    e.preventDefault();
    const digitos = cpf.replace(/\D/g, '');
    if (digitos.length !== 11) {
      showError('Informe um CPF válido com 11 dígitos.');
      return;
    }
    setCarregando(true);
    try {
      const resp = await solicitarCodigoConsentimento({ cpf: digitos });
      setEmailMascarado(resp.emailMascarado);
      setTempoRestante(resp.expiraEmSegundos);
      setEtapa(2);
      showSuccess(resp.mensagem);
    } catch (err) {
      showError(extractErrorMessage(err, 'Falha ao solicitar código verificador.'));
    } finally {
      setCarregando(false);
    }
  };

  const handleValidarCodigo = async (e: React.FormEvent) => {
    e.preventDefault();
    if (codigoOtp.trim().length !== 6) {
      showError('O código deve conter exatamente 6 dígitos.');
      return;
    }
    setCarregando(true);
    try {
      const resp = await validarCodigoConsentimento({
        cpf: cpf.replace(/\D/g, ''),
        codigo: codigoOtp.trim(),
      });
      setDadosSessao(resp);
      setTermoAceito(resp.termoAceito);
      setUsoImagem(resp.usoImagemAutorizado);
      setPermitirAniversario(resp.permitirDivulgacaoAniversario);
      setResponsavelNome(resp.responsavelMenorNome || '');
      setResponsavelCpf(formatarCpfInput(resp.responsavelMenorCpf || ''));
      setEtapa(3);
    } catch (err) {
      showError(extractErrorMessage(err, 'Código verificador inválido ou expirado.'));
    } finally {
      setCarregando(false);
    }
  };

  const handleManifestarConcessao = async () => {
    if (!dadosSessao) return;
    if (!termoAceito) {
      showError('Para confirmar, é necessário assinalar o aceite expresso dos termos.');
      return;
    }
    if (dadosSessao.isMenor) {
      if (!responsavelNome.trim()) {
        showError('O nome do responsável legal é obrigatório para titulares menores de idade.');
        return;
      }
      if (responsavelCpf.replace(/\D/g, '').length !== 11) {
        showError('O CPF do responsável legal deve conter 11 dígitos.');
        return;
      }
    }
    setCarregando(true);
    try {
      const resp = await manifestarConsentimento(dadosSessao.token, {
        termoAceito: true,
        usoImagemAutorizado: usoImagem,
        permitirDivulgacaoAniversario: permitirAniversario,
        responsavelMenorNome: dadosSessao.isMenor ? responsavelNome.trim() : null,
        responsavelMenorCpf: dadosSessao.isMenor ? responsavelCpf.replace(/\D/g, '') : null,
      });
      setComprovante(resp);
      setEtapa(4);
      showSuccess(resp.mensagem);
    } catch (err) {
      showError(extractErrorMessage(err, 'Falha ao registrar consentimento.'));
    } finally {
      setCarregando(false);
    }
  };

  const handleConfirmarRevogacao = async () => {
    if (!dadosSessao) return;
    setCarregando(true);
    try {
      const resp = await revogarConsentimento(dadosSessao.token);
      setDialogoRevogacaoAberto(false);
      setComprovante(resp);
      setEtapa(4);
      showSuccess('Consentimento revogado com sucesso.');
    } catch (err) {
      showError(extractErrorMessage(err, 'Falha ao revogar consentimento.'));
    } finally {
      setCarregando(false);
    }
  };

  const formatarTempo = (segundos: number): string => {
    const min = Math.floor(segundos / 60);
    const seg = segundos % 60;
    return `${min}:${seg < 10 ? '0' : ''}${seg}`;
  };

  return (
    <Container maxWidth="md" sx={{ py: 4 }}>
      <Paper sx={{ p: 4, borderRadius: 2 }}>
        {/* Cabeçalho */}
        <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
          <ShieldIcon color="primary" sx={{ fontSize: 40, mr: 2 }} />
          <Box>
            <Typography variant="h5" component="h1" sx={{ fontWeight: 'bold' }}>
              Portal de Privacidade e Consentimento
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Paróquia Nossa Senhora de Fátima • Autoatendimento Seguro LGPD
            </Typography>
          </Box>
        </Box>

        {carregando && <LinearProgress sx={{ mb: 2, borderRadius: 1 }} />}

        {/* ETAPA 1: SOLICITAÇÃO DO CÓDIGO */}
        {etapa === 1 && (
          <Box component="form" onSubmit={handleSolicitarCodigo}>
            <Alert severity="info" sx={{ mb: 3 }}>
              Para garantir a segurança de seus dados e evitar fraudes, informe seu CPF. Um código de uso único (OTP)
              será enviado ao e-mail cadastrado junto à secretaria paroquial.
            </Alert>
            <Card variant="outlined" sx={{ p: 3, mb: 3, borderRadius: 2 }}>
              <Typography variant="subtitle1" sx={{ fontWeight: 500, mb: 2 }}>
                Identificação do Titular (Fiel ou Clérigo)
              </Typography>
              <TextField
                fullWidth
                size="small"
                label="CPF do Titular"
                placeholder="000.000.000-00"
                value={cpf}
                onChange={(e) => setCpf(formatarCpfInput(e.target.value))}
                slotProps={{ htmlInput: { maxLength: 14 } }}
                autoFocus
              />
            </Card>
            <Box sx={{ display: 'flex', justifyContent: 'flex-end' }}>
              <ActionButton
                title="Enviar Código de Verificação"
                icon={<LockOpenIcon />}
                onClick={() => {}}
                type="submit"
                disabled={carregando}
              />
            </Box>
          </Box>
        )}

        {/* ETAPA 2: DIGITAÇÃO DO CÓDIGO OTP */}
        {etapa === 2 && (
          <Box component="form" onSubmit={handleValidarCodigo}>
            <Alert severity="success" sx={{ mb: 3 }}>
              Um código verificador de 6 dígitos foi enviado para <strong>{emailMascarado}</strong>.
              O código expira em <strong>{formatarTempo(tempoRestante)}</strong>.
            </Alert>
            <Card variant="outlined" sx={{ p: 3, mb: 3, borderRadius: 2 }}>
              <Typography variant="subtitle1" sx={{ fontWeight: 500, mb: 2 }}>
                Validação de Posse de E-mail
              </Typography>
              <TextField
                fullWidth
                size="small"
                label="Código Verificador (6 dígitos)"
                placeholder="123456"
                value={codigoOtp}
                onChange={(e) => setCodigoOtp(e.target.value.replace(/\D/g, '').slice(0, 6))}
                slotProps={{
                  htmlInput: {
                    maxLength: 6,
                    style: { letterSpacing: 8, fontSize: '1.2rem', textAlign: 'center' },
                  },
                }}
                autoFocus
              />
            </Card>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <ActionButton
                title="Voltar / Corrigir CPF"
                icon={<ArrowBackIcon />}
                onClick={() => setEtapa(1)}
                variant="outlined"
              />
              <Box sx={{ display: 'flex', gap: 2 }}>
                <ActionButton
                  title="Reenviar Código"
                  icon={<RefreshIcon />}
                  onClick={handleSolicitarCodigo}
                  variant="outlined"
                  disabled={tempoRestante > 540 || carregando}
                />
                <ActionButton
                  title="Acessar Painel"
                  icon={<CheckCircleIcon />}
                  onClick={() => {}}
                  type="submit"
                  disabled={carregando}
                />
              </Box>
            </Box>
          </Box>
        )}

        {/* ETAPA 3: PAINEL DE GESTÃO DO CONSENTIMENTO */}
        {etapa === 3 && dadosSessao && (
          <Box>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
              <Typography variant="h6" sx={{ fontWeight: 'bold' }}>
                Olá, {dadosSessao.nome}
              </Typography>
              <Chip
                label={dadosSessao.termoAceito ? 'Consentimento Ativo' : 'Consentimento Pendente / Revogado'}
                color={dadosSessao.termoAceito ? 'success' : 'warning'}
                variant="outlined"
              />
            </Box>

            {dadosSessao.isMenor && (
              <Alert severity="warning" sx={{ mb: 3 }}>
                <strong>Titular menor de 18 anos (Art. 14 da LGPD):</strong> O consentimento deve ser firmado pelo
                pai, mãe ou responsável legal devidamente identificado.
              </Alert>
            )}

            {/* Texto do Termo Oficial */}
            <Card variant="outlined" sx={{ mb: 3, borderRadius: 2 }}>
              <CardContent>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                  <Typography variant="subtitle2" color="primary">
                    Termo Vigente: {dadosSessao.termoVersao}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    Hash SHA-256: {dadosSessao.termoHash.slice(0, 16)}...
                  </Typography>
                </Box>
                <Box sx={{ ...DIALOG_CONTENT_SCROLL_SX, maxHeight: 180, p: 1.5, bgcolor: 'action.hover', borderRadius: 1 }}>
                  <Typography variant="body2" sx={{ whiteSpace: 'pre-line', fontSize: '0.85rem' }}>
                    {dadosSessao.textoTermo}
                  </Typography>
                </Box>
                <FormControlLabel
                  control={<Checkbox checked={termoAceito} onChange={(e) => setTermoAceito(e.target.checked)} />}
                  label="Declaro que li e manifesto expressamente meu consentimento para as finalidades pastorais descritas."
                  sx={{ mt: 1.5 }}
                />
              </CardContent>
            </Card>

            {/* Dados do Responsável (se menor) */}
            {dadosSessao.isMenor && (
              <Card variant="outlined" sx={{ p: 2.5, mb: 3, borderRadius: 2 }}>
                <Typography variant="subtitle2" sx={{ fontWeight: 'bold', mb: 1.5 }}>
                  Identificação do Responsável Legal Anuente
                </Typography>
                <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' }, gap: 2 }}>
                  <TextField
                    size="small"
                    label="Nome Completo do Responsável"
                    value={responsavelNome}
                    onChange={(e) => setResponsavelNome(e.target.value)}
                  />
                  <TextField
                    size="small"
                    label="CPF do Responsável"
                    value={responsavelCpf}
                    onChange={(e) => setResponsavelCpf(formatarCpfInput(e.target.value))}
                  />
                </Box>
              </Card>
            )}

            {/* Preferências Opcionais */}
            <Card variant="outlined" sx={{ p: 2.5, mb: 3, borderRadius: 2 }}>
              <Typography variant="subtitle2" sx={{ fontWeight: 'bold', mb: 1 }}>
                Preferências e Autorizações Opcionais
              </Typography>
              <FormControlLabel
                control={<Switch checked={usoImagem} onChange={(e) => setUsoImagem(e.target.checked)} />}
                label="Autorizo o uso da minha imagem e voz em celebrações litúrgicas e redes sociais oficiais da paróquia."
              />
              <Divider sx={{ my: 1 }} />
              <FormControlLabel
                control={<Switch checked={permitirAniversario} onChange={(e) => setPermitirAniversario(e.target.checked)} />}
                label="Autorizo a divulgação do meu aniversário na comunidade paroquial e murais."
              />
            </Card>

            {/* Ações */}
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              {dadosSessao.termoAceito ? (
                <ActionButton
                  title="Revogar Meu Consentimento"
                  icon={<BlockIcon />}
                  onClick={() => setDialogoRevogacaoAberto(true)}
                  color="error"
                  variant="outlined"
                />
              ) : (
                <Box />
              )}
              <ActionButton
                title="Confirmar Consentimento"
                icon={<CheckCircleIcon />}
                onClick={handleManifestarConcessao}
                disabled={carregando}
              />
            </Box>
          </Box>
        )}

        {/* ETAPA 4: COMPROVANTE DIGITAL OFICIAL */}
        {etapa === 4 && comprovante && (
          <Box>
            <Alert severity={comprovante.tipoEvento === 'REVOGACAO' ? 'warning' : 'success'} sx={{ mb: 3 }}>
              {comprovante.tipoEvento === 'REVOGACAO'
                ? 'Seu consentimento foi revogado com sucesso. Todas as comunicações e divulgações foram interrompidas.'
                : 'Seu consentimento foi registrado com sucesso na base paroquial.'}
            </Alert>
            <Card variant="outlined" sx={{ p: 3, mb: 3, borderRadius: 2 }}>
              <Typography variant="h6" gutterBottom sx={{ fontWeight: 'bold' }}>
                Comprovante Digital de Manifestação LGPD
              </Typography>
              <Divider sx={{ mb: 2 }} />
              <Typography variant="body2" sx={{ mb: 1 }}>
                <strong>Protocolo Oficial:</strong> {comprovante.protocolo}
              </Typography>
              <Typography variant="body2" sx={{ mb: 1 }}>
                <strong>Titular:</strong> {comprovante.titularNome}
              </Typography>
              <Typography variant="body2" sx={{ mb: 1 }}>
                <strong>Natureza:</strong> {comprovante.tipoEvento}
              </Typography>
              <Typography variant="body2" sx={{ mb: 1 }}>
                <strong>Carimbo UTC:</strong> {comprovante.dataHoraUtc}
              </Typography>
              <Typography variant="body2" sx={{ mb: 1 }}>
                <strong>Versão e Hash:</strong> {comprovante.termoVersao} ({comprovante.termoHash.slice(0, 16)}...)
              </Typography>
              <Typography variant="body2" color="text.secondary" sx={{ mt: 2, fontSize: '0.8rem' }}>
                Canal: Portal de Autoatendimento com verificação de posse por OTP de e-mail (LGPD Art. 8º).
              </Typography>
            </Card>
            <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
              <ActionButton
                title="Imprimir / Salvar PDF"
                icon={<PrintIcon />}
                onClick={() => window.print()}
                variant="outlined"
              />
              <ActionButton
                title="Novo Acesso"
                icon={<RefreshIcon />}
                onClick={() => {
                  setEtapa(1);
                  setCpf('');
                  setCodigoOtp('');
                  setDadosSessao(null);
                  setComprovante(null);
                }}
              />
            </Box>
          </Box>
        )}

        {/* DIÁLOGO MODAL DE CONFIRMAÇÃO DE REVOGAÇÃO */}
        <Dialog open={dialogoRevogacaoAberto} onClose={() => setDialogoRevogacaoAberto(false)} maxWidth="sm" fullWidth>
          <DialogTitle>Confirmar Revogação de Consentimento</DialogTitle>
          <DialogContent dividers sx={DIALOG_CONTENT_SCROLL_SX}>
            <Typography variant="body2" sx={{ mb: 2 }}>
              Você está exercendo seu direito de revogação sob o <strong>Art. 8º, § 5º da LGPD</strong>.
            </Typography>
            <Typography variant="subtitle2" color="error" sx={{ fontWeight: 'bold' }}>
              O que será interrompido:
            </Typography>
            <Typography variant="body2" component="ul" sx={{ pl: 2, mb: 2 }}>
              <li>Envio de informativos, mensagens e avisos pastorais.</li>
              <li>Divulgação comunitária do seu aniversário.</li>
              <li>Veiculação de sua imagem e voz em novos materiais ou transmissões da paróquia.</li>
            </Typography>
            <Typography variant="subtitle2" color="primary" sx={{ fontWeight: 'bold' }}>
              O que permanece resguardado por obrigação legal e canônica:
            </Typography>
            <Typography variant="body2" sx={{ fontSize: '0.85rem', color: 'text.secondary' }}>
              Em conformidade com o <strong>Art. 16, I da LGPD</strong> e com o <strong>Cân. 535 do Código de Direito Canônico</strong>,
              os assentos de sacramentos (Batismo, Crisma, Matrimônio, Ordem) permanecem perpetuamente arquivados nos livros
              históricos da Paróquia e da Diocese, não sendo passíveis de exclusão por revogação de consentimento de comunicação.
            </Typography>
          </DialogContent>
          <DialogActions sx={{ p: 2 }}>
            <ActionButton
              title="Cancelar e Manter Ativo"
              icon={<ArrowBackIcon />}
              onClick={() => setDialogoRevogacaoAberto(false)}
              variant="outlined"
            />
            <ActionButton
              title="Sim, Revogar Meu Consentimento"
              icon={<BlockIcon />}
              onClick={handleConfirmarRevogacao}
              color="error"
              disabled={carregando}
            />
          </DialogActions>
        </Dialog>
      </Paper>
    </Container>
  );
};

export default PortalConsentimentoPage;
