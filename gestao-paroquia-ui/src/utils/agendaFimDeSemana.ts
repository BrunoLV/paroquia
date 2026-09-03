import {WeeklyEvent} from '../api/types';

/**
 * Monta uma lista simplificada, em texto puro, dos eventos de sábado e domingo — para colar num
 * aplicativo de mensagens ou imprimir. Função pura: recebe a agenda da semana e devolve a string
 * já formatada (uma linha por evento, agrupada visualmente por dia).
 */
export const formatarAgendaFimDeSemana = (eventos: WeeklyEvent[]): string => {
  const fimDeSemana = eventos
    .filter((evento) => [0, 6].includes(new Date(evento.inicio).getDay()))
    .sort((a, b) => a.inicio.localeCompare(b.inicio));

  if (fimDeSemana.length === 0) {
    return 'Nenhum evento agendado para o fim de semana.';
  }

  const linhas = fimDeSemana.map((evento) => {
    const data = new Date(evento.inicio);
    const dia = data.toLocaleDateString('pt-BR', {weekday: 'long', day: '2-digit', month: '2-digit'});
    const hora = data.toLocaleTimeString('pt-BR', {hour: '2-digit', minute: '2-digit'});
    return `• ${dia} ${hora} — ${evento.titulo} (${evento.localNome})`;
  });

  return `Agenda do fim de semana\n${linhas.join('\n')}`;
};
