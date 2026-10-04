import AccountTreeIcon from '@mui/icons-material/AccountTree';
import AssessmentIcon from '@mui/icons-material/Assessment';
import AssignmentTurnedInIcon from '@mui/icons-material/AssignmentTurnedIn';
import CalendarIcon from '@mui/icons-material/CalendarMonth';
import ChurchIcon from '@mui/icons-material/Church';
import DashboardIcon from '@mui/icons-material/Dashboard';
import ReservasIcon from '@mui/icons-material/EventAvailable';
import FactCheckIcon from '@mui/icons-material/FactCheck';
import VolunteerActivismIcon from '@mui/icons-material/VolunteerActivism';
import AccountBalanceIcon from '@mui/icons-material/AccountBalance';
import LocationIcon from '@mui/icons-material/LocationOn';
import HowToRegIcon from '@mui/icons-material/HowToReg';
import HistoryEduIcon from '@mui/icons-material/HistoryEdu';
import MenuBookIcon from '@mui/icons-material/MenuBook';
import NotificationsIcon from '@mui/icons-material/Notifications';
import PeopleIcon from '@mui/icons-material/People';
import SettingsIcon from '@mui/icons-material/Settings';
import BadgeIcon from '@mui/icons-material/Badge';
import { canApprove, canManageAnoParoquial, canManageLocais, canManageOrganizacoes, canManageUnidadesTerritoriais, canViewSacramentos, type User } from '@/contextos/iam/presentation/auth/permissions';

export interface NavigationItem {
  readonly label: string;
  readonly path: string;
  readonly icon: typeof DashboardIcon;
  readonly visible?: (user: User | null) => boolean;
}

export interface NavigationSection {
  readonly id: string;
  readonly title: string;
  readonly items: readonly NavigationItem[];
}

/** Ordem por tarefa, não alfabética. Os predicados existentes conservam a visibilidade autorizada. */
const sections: readonly NavigationSection[] = [
  { id: 'visao-geral', title: 'Visão geral', items: [
    { label: 'Painel', path: '/painel', icon: DashboardIcon },
    { label: 'Notificações', path: '/notificacoes', icon: NotificationsIcon },
    { label: 'Relatórios', path: '/relatorios', icon: AssessmentIcon },
  ] },
  { id: 'pessoas-comunidades', title: 'Pessoas e comunidades', items: [
    { label: 'Fiéis', path: '/fieis', icon: PeopleIcon },
    { label: 'Clero', path: '/clerigos', icon: BadgeIcon },
    { label: 'Pastorais e Movimentos', path: '/organizacoes', icon: VolunteerActivismIcon, visible: canManageOrganizacoes },
  ] },
  { id: 'vida-pastoral', title: 'Vida pastoral', items: [
    { label: 'Calendário', path: '/calendario', icon: CalendarIcon },
    { label: 'Projetos', path: '/projetos', icon: AssignmentTurnedInIcon },
    { label: 'Sacramentos', path: '/sacramentos', icon: MenuBookIcon, visible: canViewSacramentos },
  ] },
  { id: 'governo-paroquial', title: 'Governo paroquial', items: [
    { label: 'Governo & Território', path: '/governo-territorio', icon: ChurchIcon, visible: canManageUnidadesTerritoriais },
    { label: 'Organograma', path: '/organograma', icon: AccountTreeIcon },
    { label: 'Conselhos & Atas', path: '/conselhos', icon: HistoryEduIcon },
  ] },
  { id: 'espacos-recursos', title: 'Espaços e recursos', items: [
    { label: 'Locais', path: '/locais', icon: LocationIcon, visible: canManageLocais },
    { label: 'Patrimônio', path: '/ativos', icon: AccountBalanceIcon },
    { label: 'Reservas', path: '/reservas', icon: ReservasIcon },
  ] },
  { id: 'administracao', title: 'Administração', items: [
    { label: 'Aprovações', path: '/aprovacoes', icon: FactCheckIcon, visible: canApprove },
    { label: 'Ajustes cadastrais', path: '/ajustes-cadastrais', icon: HowToRegIcon, visible: canApprove },
    { label: 'Configurações', path: '/configuracoes', icon: SettingsIcon, visible: user => canManageAnoParoquial(user) || canManageOrganizacoes(user) },
  ] },
];

/** Seções vazias desaparecem após filtragem; nenhum acesso novo é concedido pelo agrupamento. */
export const getNavigationSections = (user: User | null): readonly NavigationSection[] => sections
  .map(section => ({ ...section, items: section.items.filter(item => !item.visible || item.visible(user)) }))
  .filter(section => section.items.length > 0);
