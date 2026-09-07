import {MarkEmailRead as LidaIcon, Notifications as NotificacaoIcon} from '@mui/icons-material';
import {Box, Button, List, ListItem, ListItemText, Paper} from '@mui/material';
import {useMutation, useQuery, useQueryClient} from '@tanstack/react-query';
import {listarNotificacoes, marcarNotificacaoLida} from '@/api/notificacoes';
import PageHeader from '@/components/PageHeader';
const Notificacoes = () => { const client=useQueryClient(); const itens=useQuery({queryKey:['notificacoes'],queryFn:listarNotificacoes}); const marcar=useMutation({mutationFn:marcarNotificacaoLida,onSuccess:()=>client.invalidateQueries({queryKey:['notificacoes']})}); return <Box sx={{p:3}}><PageHeader icon={<NotificacaoIcon/>} title="Notificações" subtitle="Decisões e avisos do seu cadastro."/><Paper><List>{(itens.data??[]).map(n=><ListItem key={n.id} secondaryAction={!n.lidaEmUtc?<Button startIcon={<LidaIcon/>} onClick={()=>marcar.mutate(n.id)}>Marcar lida</Button>:null}><ListItemText primary={n.mensagem} secondary={new Date(n.criadaEmUtc).toLocaleString('pt-BR')}/></ListItem>)}</List></Paper></Box>;}; export default Notificacoes;
