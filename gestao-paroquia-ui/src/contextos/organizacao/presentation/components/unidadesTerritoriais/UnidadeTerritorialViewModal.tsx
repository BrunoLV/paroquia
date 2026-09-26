import React from 'react';
import {
  Card,
  CardContent,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Grid,
  Stack,
  Typography,
} from '@mui/material';
import AccountBalanceIcon from '@mui/icons-material/AccountBalance';
import AutoAwesomeIcon from '@mui/icons-material/AutoAwesome';
import BadgeIcon from '@mui/icons-material/Badge';
import BoltIcon from '@mui/icons-material/Bolt';
import CalendarMonthIcon from '@mui/icons-material/CalendarMonth';
import ChurchIcon from '@mui/icons-material/Church';
import CloseIcon from '@mui/icons-material/Close';
import DescriptionIcon from '@mui/icons-material/Description';
import EmailIcon from '@mui/icons-material/Email';
import EventIcon from '@mui/icons-material/Event';
import LocationOnIcon from '@mui/icons-material/LocationOn';
import PersonIcon from '@mui/icons-material/Person';
import PhoneIcon from '@mui/icons-material/Phone';
import ReceiptLongIcon from '@mui/icons-material/ReceiptLong';
import ShieldIcon from '@mui/icons-material/Shield';
import SquareFootIcon from '@mui/icons-material/SquareFoot';
import VerifiedIcon from '@mui/icons-material/Verified';
import WaterDropIcon from '@mui/icons-material/WaterDrop';
import WaterIcon from '@mui/icons-material/Water';
import ActionButton from '@/shared/presentation/components/ActionButton';
import StatusIcon from '@/shared/presentation/components/StatusIcon';
import { DIALOG_CONTENT_SCROLL_SX } from '@/shared/presentation/theme/dialogStyles';
import { InfoField } from '@/contextos/fiel/presentation/components/fieis/fielConstants';
import { formatDateBr } from '@/shared/utils/masks';
import {
  getUnidadeTerritorialTipoMeta,
  getAvcbStatusMeta,
} from '@/shared/utils/statusMeta/unidadeTerritorialStatus';
import { getSituacaoAtivoMeta } from '@/shared/utils/statusMeta/situacaoStatus';
import { Clerigo, UnidadeTerritorial } from '@/shared/domain/types/types';

interface UnidadeTerritorialViewModalProps {
  open: boolean;
  onClose: () => void;
  unidade: UnidadeTerritorial | null;
  clerigos?: Clerigo[];
}

/**
 * Modal para visualização detalhada de uma Unidade Territorial (Igreja Matriz ou Comunidade/Capela).
 *
 * ARQUITETURA VISUAL (DESIGN_SYSTEM.md):
 * - Dividers em DialogContent com DIALOG_CONTENT_SCROLL_SX.
 * - 4 Seções em Card variant="outlined" agrupando:
 *   1. Dimensão Canônica e Litúrgica
 *   2. Endereço e Contatos
 *   3. Dados Fiscais e Registro de Imóveis (RGI)
 *   4. Segurança (Bombeiros) e Concessionárias
 * - Todos os campos utilizam o componente canônico InfoField com ícones semânticos.
 */
export const UnidadeTerritorialViewModal: React.FC<UnidadeTerritorialViewModalProps> = ({
  open,
  onClose,
  unidade,
  clerigos = [],
}) => {
  if (!unidade) return null;

  const clerigoResponsavel = clerigos.find((c) => c.clerigoId === unidade.clerigoResponsavelId);
  const clerigoNome = clerigoResponsavel ? clerigoResponsavel.nomeEclesiastico : (unidade.clerigoResponsavelId ? 'Clérigo Vinculado' : 'Não vinculado');

  const enderecoFormatado = [
    unidade.enderecoLogradouro,
    unidade.enderecoNumero ? `nº ${unidade.enderecoNumero}` : null,
    unidade.enderecoComplemento ? `(${unidade.enderecoComplemento})` : null,
    unidade.enderecoBairro,
    unidade.enderecoCidade && unidade.enderecoUf ? `${unidade.enderecoCidade}/${unidade.enderecoUf}` : unidade.enderecoCidade,
    unidade.enderecoCep ? `CEP: ${unidade.enderecoCep}` : null,
  ]
    .filter(Boolean)
    .join(', ');

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>
        <Stack direction="row" spacing={1.5} sx={{ alignItems: 'center', justifyContent: 'space-between' }}>
          <Stack direction="row" spacing={1.5} sx={{ alignItems: 'center' }}>
            <ChurchIcon color="primary" sx={{ fontSize: 28 }} />
            <Typography variant="h6" component="div" sx={{ fontWeight: 700 }}>
              {unidade.nome}
            </Typography>
          </Stack>
          <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
            <StatusIcon {...getUnidadeTerritorialTipoMeta(unidade.tipo)} />
            <StatusIcon {...getSituacaoAtivoMeta(unidade.ativo)} />
          </Stack>
        </Stack>
      </DialogTitle>

      <DialogContent dividers sx={DIALOG_CONTENT_SCROLL_SX}>
        <Stack spacing={2.5}>
          {/* Seção 1: Canônica e Litúrgica */}
          <Card variant="outlined" sx={{ borderRadius: 2 }}>
            <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
              <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
                <ChurchIcon color="primary" fontSize="small" />
                <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>
                  Dimensão Canônica & Litúrgica
                </Typography>
              </Stack>
              <Grid container spacing={2}>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <InfoField
                    label="Santo Padroeiro / Orago"
                    icon={<AutoAwesomeIcon fontSize="inherit" />}
                    value={unidade.padroeiro || 'Não informado'}
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <InfoField
                    label="Data da Festa Padroeira"
                    icon={<EventIcon fontSize="inherit" />}
                    value={unidade.dataFestaPadroeira || 'Não informada'}
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <InfoField
                    label="Dedicação do Altar / Bênção da Igreja"
                    icon={<CalendarMonthIcon fontSize="inherit" />}
                    value={unidade.dataDedicacaoAltar ? formatDateBr(unidade.dataDedicacaoAltar) : 'Não dedicada'}
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <InfoField
                    label="Clérigo Responsável Pastoral"
                    icon={<PersonIcon fontSize="inherit" />}
                    value={clerigoNome}
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <InfoField
                    label="Reserva do Santíssimo (Sacrário)"
                    icon={<VerifiedIcon fontSize="inherit" />}
                    value={unidade.possuiSacrario ? 'Sim (Presença da Reserva Eucarística)' : 'Não possui'}
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <InfoField
                    label="Pia Batismal Canônica"
                    icon={<WaterDropIcon fontSize="inherit" />}
                    value={unidade.possuiPiaBatismal ? 'Sim (Fonte Batismal Autorizada)' : 'Não possui'}
                  />
                </Grid>
              </Grid>
            </CardContent>
          </Card>

          {/* Seção 2: Endereço e Contato Civil */}
          <Card variant="outlined" sx={{ borderRadius: 2 }}>
            <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
              <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
                <LocationOnIcon color="primary" fontSize="small" />
                <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>
                  Endereço & Contatos
                </Typography>
              </Stack>
              <Grid container spacing={2}>
                <Grid size={{ xs: 12 }}>
                  <InfoField
                    label="Endereço Completo do Imóvel"
                    icon={<LocationOnIcon fontSize="inherit" />}
                    value={enderecoFormatado || 'Endereço não informado'}
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <InfoField
                    label="Telefone da Secretaria"
                    icon={<PhoneIcon fontSize="inherit" />}
                    value={unidade.telefone || 'Não informado'}
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <InfoField
                    label="E-mail Institucional"
                    icon={<EmailIcon fontSize="inherit" />}
                    value={unidade.email || 'Não informado'}
                  />
                </Grid>
              </Grid>
            </CardContent>
          </Card>

          {/* Seção 3: Fiscal e Registro de Imóveis (RGI) */}
          <Card variant="outlined" sx={{ borderRadius: 2 }}>
            <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
              <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
                <AccountBalanceIcon color="primary" fontSize="small" />
                <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>
                  Identificação Fiscal & Registro de Imóveis (RGI)
                </Typography>
              </Stack>
              <Grid container spacing={2}>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <InfoField
                    label="CNPJ de Faturamento / Filial"
                    icon={<BadgeIcon fontSize="inherit" />}
                    value={unidade.cnpj || 'Não informado'}
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <InfoField
                    label="Inscrição Municipal (CCM)"
                    icon={<BadgeIcon fontSize="inherit" />}
                    value={unidade.inscricaoMunicipal || 'Não informada'}
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <InfoField
                    label="Inscrição Imobiliária (IPTU)"
                    icon={<ReceiptLongIcon fontSize="inherit" />}
                    value={unidade.inscricaoImobiliariaIptu || 'Não informada'}
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <InfoField
                    label="Processo de Imunidade Tributária"
                    icon={<DescriptionIcon fontSize="inherit" />}
                    value={unidade.processoImunidadeIptu || 'Não informado'}
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <InfoField
                    label="Cartório de Registro de Imóveis (RGI)"
                    icon={<AccountBalanceIcon fontSize="inherit" />}
                    value={unidade.cartorioRgi || 'Não informado'}
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <InfoField
                    label="Matrícula / Transcrição Imobiliária"
                    icon={<DescriptionIcon fontSize="inherit" />}
                    value={unidade.matriculaImovel || 'Não informada'}
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 4 }}>
                  <InfoField
                    label="Tipo de Aquisição do Solo"
                    icon={<DescriptionIcon fontSize="inherit" />}
                    value={unidade.tipoAquisicao || 'Não informado'}
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 4 }}>
                  <InfoField
                    label="Área Total do Terreno"
                    icon={<SquareFootIcon fontSize="inherit" />}
                    value={unidade.areaTerrenoM2 != null ? `${unidade.areaTerrenoM2.toLocaleString('pt-BR')} m²` : 'Não informada'}
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 4 }}>
                  <InfoField
                    label="Área Construída Edificada"
                    icon={<SquareFootIcon fontSize="inherit" />}
                    value={unidade.areaConstruidaM2 != null ? `${unidade.areaConstruidaM2.toLocaleString('pt-BR')} m²` : 'Não informada'}
                  />
                </Grid>
              </Grid>
            </CardContent>
          </Card>

          {/* Seção 4: Bombeiros e Concessionárias */}
          <Card variant="outlined" sx={{ borderRadius: 2 }}>
            <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
              <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
                <ShieldIcon color="primary" fontSize="small" />
                <Typography variant="subtitle2" sx={{ fontWeight: 700, color: 'primary.main' }}>
                  Segurança (Bombeiros) & Concessionárias de Serviços Públicos
                </Typography>
              </Stack>
              <Grid container spacing={2}>
                <Grid size={{ xs: 12, sm: 4 }}>
                  <InfoField
                    label="Número do AVCB / CLCB"
                    icon={<ShieldIcon fontSize="inherit" />}
                    value={unidade.avcbNumero || 'Não informado'}
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 4 }}>
                  <InfoField
                    label="Situação do Laudo dos Bombeiros"
                    icon={<ShieldIcon fontSize="inherit" />}
                    value={<StatusIcon {...getAvcbStatusMeta(unidade.avcbValidade, unidade.avcbNumero)} />}
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 4 }}>
                  <InfoField
                    label="Lotação Máxima Aprovada"
                    icon={<ShieldIcon fontSize="inherit" />}
                    value={unidade.lotacaoMaximaBombeiros != null ? `${unidade.lotacaoMaximaBombeiros} pessoas` : 'Não informada'}
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <InfoField
                    label="Instalação de Energia Elétrica"
                    icon={<BoltIcon fontSize="inherit" />}
                    value={unidade.codigoConsumidorEnergia || 'Não informado'}
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <InfoField
                    label="Ligação de Água e Saneamento"
                    icon={<WaterIcon fontSize="inherit" />}
                    value={unidade.codigoConsumidorAgua || 'Não informado'}
                  />
                </Grid>
              </Grid>
            </CardContent>
          </Card>
        </Stack>
      </DialogContent>

      <DialogActions sx={{ px: 3, py: 2 }}>
        <ActionButton title="Fechar" icon={<CloseIcon />} color="secondary" onClick={onClose} />
      </DialogActions>
    </Dialog>
  );
};

export default UnidadeTerritorialViewModal;
