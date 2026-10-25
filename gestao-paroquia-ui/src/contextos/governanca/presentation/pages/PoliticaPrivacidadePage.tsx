import React from 'react';
import {
  Alert,
  Box,
  Container,
  Paper,
  Typography,
  Divider,
  Card,
  CardContent,
  Chip,
} from '@mui/material';
import ShieldIcon from '@mui/icons-material/Shield';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import { useNavigate } from 'react-router-dom';
import ActionButton from '@/shared/presentation/components/ActionButton';
import { useConfiguracaoParoquia } from '@/contextos/governanca/presentation/hooks/useConfiguracaoParoquia';

const PoliticaPrivacidadePage: React.FC = () => {
  const navigate = useNavigate();
  const { configuracao } = useConfiguracaoParoquia();

  const paroquiaNome = configuracao?.nome || 'Paróquia Nossa Senhora de Fátima';
  const dioceseNome = configuracao?.diocese || 'Diocese de Presidente Prudente';
  const dpoNome = configuracao?.dpoNome || 'Pe. Administrador / DPO Paroquial';
  const dpoEmail = configuracao?.dpoEmail || 'privacidade@nsfatima.org.br';
  const dpoTelefone = configuracao?.dpoTelefone || '(18) 3221-0000';

  return (
    <Container maxWidth="md" sx={{ py: 4 }}>
      <Paper sx={{ p: 4, borderRadius: 2 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
          <ShieldIcon color="primary" sx={{ fontSize: 40, mr: 2 }} />
          <Box>
            <Typography variant="h5" component="h1" sx={{ fontWeight: 'bold' }}>
              Política de Privacidade e Proteção de Dados
            </Typography>
            <Typography variant="body2" color="text.secondary">
              {paroquiaNome} • {dioceseNome}
            </Typography>
          </Box>
        </Box>

        <Divider sx={{ mb: 3 }} />

        {/* 1. Apresentação e Encarregado */}
        <Card variant="outlined" sx={{ mb: 3, borderRadius: 2, bgcolor: 'action.hover' }}>
          <CardContent>
            <Typography variant="subtitle1" sx={{ fontWeight: 'bold' }} gutterBottom>
              1. Identificação do Controlador e do Encarregado (DPO - Art. 41)
            </Typography>
            <Typography variant="body2" sx={{ mb: 1.5 }}>
              A <strong>{paroquiaNome}</strong>, parte integrante da <strong>{dioceseNome}</strong>, no papel de
              Controladora de dados pessoais, reafirma seu compromisso com a privacidade, a dignidade da pessoa humana e
              o cumprimento estrito da Lei Geral de Proteção de Dados Pessoais (Lei nº 13.709/2018 - LGPD), harmonizada
              com a disciplina secular do Código de Direito Canônico (Cân. 535).
            </Typography>
            <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1.5, mt: 1 }}>
              <Chip label={`Encarregado: ${dpoNome}`} variant="outlined" color="primary" size="small" />
              <Chip label={`E-mail: ${dpoEmail}`} variant="outlined" color="primary" size="small" />
              <Chip label={`Telefone: ${dpoTelefone}`} variant="outlined" color="primary" size="small" />
            </Box>
          </CardContent>
        </Card>

        {/* 2. Finalidades e Bases Legais */}
        <Typography variant="h6" gutterBottom sx={{ mt: 3, fontWeight: 'bold' }}>
          2. Finalidades do Tratamento e Bases Legais (Arts. 7º e 11)
        </Typography>
        <Typography variant="body2" sx={{ mb: 1.5 }}>
          Os dados pessoais coletados pela Paróquia — que revelam convicção religiosa e filiação eclesial (dados pessoais
          sensíveis conforme o Art. 5º, II da LGPD) — destinam-se exclusivamente às seguintes finalidades legítimas:
        </Typography>
        <Typography variant="body2" component="ul" sx={{ pl: 3, mb: 2 }}>
          <li>
            <strong>Assentos e Administração Sacramental (Batismo, Crisma, Matrimônio, Ordem):</strong> Cumprimento de
            obrigação canônica e dever histórico-documental perpétuo (Art. 7º, II; Art. 11, II, "a" da LGPD e Cân. 535 do CIC).
          </li>
          <li>
            <strong>Pastoral do Dízimo e Doações Comunitárias:</strong> Gestão de contribuições voluntárias e sustentação
            material paroquial (Art. 7º, V e IX da LGPD).
          </li>
          <li>
            <strong>Catequese e Formação Cristã da Infância e Juventude:</strong> Consentimento específico firmado por pai,
            mãe ou responsável legal (Art. 14, § 1º da LGPD).
          </li>
          <li>
            <strong>Comunicação Pastoral, Imagem e Voz:</strong> Transmissão de missas, publicações evangelizadoras e
            divulgação de aniversários mediante consentimento livre e revogável do titular (Art. 7º, I; Art. 11, I da LGPD).
          </li>
        </Typography>

        {/* 3. Vedação Comercial e Uso Compartilhado */}
        <Typography variant="h6" gutterBottom sx={{ mt: 3, fontWeight: 'bold' }}>
          3. Uso Compartilhado Eclesial e Vedação Comercial Absoluta (Arts. 11, § 4º e 18, VII)
        </Typography>
        <Typography variant="body2" sx={{ mb: 1.5 }}>
          A Paróquia compartilha dados estritamente no âmbito eclesial e administrativo institucional:
        </Typography>
        <Typography variant="body2" component="ul" sx={{ pl: 3, mb: 2 }}>
          <li><strong>Mitra Diocesana e Cúria Diocesana:</strong> Para arquivos históricos, processos de habilitação matrimonial e registros de clérigos.</li>
          <li><strong>Tribunal Eclesiástico Diocesano:</strong> Para instrução de causas e processos canônicos específicos.</li>
          <li><strong>Instituições Bancárias Conveniadas:</strong> Para processamento exclusivo de doações e dízimos voluntários autorizados.</li>
        </Typography>
        <Alert severity="info" sx={{ my: 2 }}>
          <strong>Vedação Comercial Absoluta:</strong> É terminantemente proibida a comercialização, aluguel, cessão
          onerosa ou compartilhamento de dados pessoais paroquiais com terceiros para fins de marketing ou exploração
          econômica (Art. 11, § 4º da LGPD).
        </Alert>

        {/* 4. Direitos dos Titulares e Revogação */}
        <Typography variant="h6" gutterBottom sx={{ mt: 3, fontWeight: 'bold' }}>
          4. Direitos dos Titulares e Procedimento de Revogação Facilitada (Arts. 8º e 18)
        </Typography>
        <Typography variant="body2" sx={{ mb: 1.5 }}>
          O titular dos dados (ou seu responsável legal) tem o direito de, a qualquer momento e gratuitamente:
        </Typography>
        <Typography variant="body2" component="ul" sx={{ pl: 3, mb: 2 }}>
          <li>Confirmar a existência de tratamento e acessar seus dados cadastrais.</li>
          <li>Corrigir dados incompletos, inexatos ou desatualizados.</li>
          <li>Solicitar a portabilidade dos dados em formato eletrônico interoperável (JSON).</li>
          <li>
            <strong>Revogar expressamente seu consentimento</strong> para fins de comunicação comunitária, imagem e
            aniversários através do nosso <strong>Portal de Autoatendimento</strong>, sem necessidade de justificativa.
          </li>
        </Typography>
        <Typography variant="body2" sx={{ mb: 1.5, fontStyle: 'italic', color: 'text.secondary' }}>
          *Nota Canônica: Conforme o Art. 16, I da LGPD, os assentos dos livros sacramentais paroquiais possuem fé pública
          eclesiástica perpétua e não são passíveis de eliminação por revogação de comunicação.
        </Typography>

        {/* 5. Medidas de Segurança */}
        <Typography variant="h6" gutterBottom sx={{ mt: 3, fontWeight: 'bold' }}>
          5. Segurança da Informação (Art. 46)
        </Typography>
        <Typography variant="body2" sx={{ mb: 1.5 }}>
          Adotamos rigorosas medidas técnicas e administrativas: criptografia forte em repouso (AES-256) para todos os
          documentos civis, sanitização de logs (PII Zero), isolamento de acessos por papéis estritos e política formal de
          comunicação de incidentes à ANPD em conformidade com o Art. 48 da lei.
        </Typography>

        <Box sx={{ display: 'flex', justifyContent: 'space-between', mt: 4, pt: 2, borderTop: 1, borderColor: 'divider' }}>
          <ActionButton
            title="Voltar"
            icon={<ArrowBackIcon />}
            onClick={() => navigate(-1)}
            variant="outlined"
          />
          <ActionButton
            title="Acessar Portal de Consentimento"
            icon={<ShieldIcon />}
            onClick={() => navigate('/portal/consentimento')}
          />
        </Box>
      </Paper>
    </Container>
  );
};

export default PoliticaPrivacidadePage;
