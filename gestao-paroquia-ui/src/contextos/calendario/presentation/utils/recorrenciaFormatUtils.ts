import { RegraRecorrencia } from '@/contextos/calendario/presentation/components/FormularioRecorrencia';
import { getRecurrenceDates } from '@/shared/utils/recurrenceUtils';

interface MetaDiaSemana {
  nomeSingular: string;
  nomePlural: string;
  genero: 'M' | 'F';
}

const DIAS_META: Record<string, MetaDiaSemana> = {
  SUNDAY: { nomeSingular: 'domingo', nomePlural: 'domingos', genero: 'M' },
  MONDAY: { nomeSingular: 'segunda-feira', nomePlural: 'segundas-feiras', genero: 'F' },
  TUESDAY: { nomeSingular: 'terça-feira', nomePlural: 'terças-feiras', genero: 'F' },
  WEDNESDAY: { nomeSingular: 'quarta-feira', nomePlural: 'quartas-feiras', genero: 'F' },
  THURSDAY: { nomeSingular: 'quinta-feira', nomePlural: 'quintas-feiras', genero: 'F' },
  FRIDAY: { nomeSingular: 'sexta-feira', nomePlural: 'sextas-feiras', genero: 'F' },
  SATURDAY: { nomeSingular: 'sábado', nomePlural: 'sábados', genero: 'M' },
};

const POSICAO_ORDINAL: Record<string, { M: string; F: string }> = {
  PRIMEIRO: { M: 'primeiro', F: 'primeira' },
  SEGUNDO: { M: 'segundo', F: 'segunda' },
  TERCEIRO: { M: 'terceiro', F: 'terceira' },
  QUARTO: { M: 'quarto', F: 'quarta' },
  ULTIMO: { M: 'último', F: 'última' },
};

/**
 * Converte lista de identificadores de dias da semana em texto natural com a preposição correta.
 * Ex: ['TUESDAY'] -> 'às terças-feiras'
 * Ex: ['SUNDAY', 'SATURDAY'] -> 'aos domingos e sábados'
 * Ex: ['SUNDAY', 'MONDAY'] -> 'aos domingos e às segundas-feiras'
 */
export function formatarDiasSemanaComPreposicao(dias: string[]): string {
  if (!dias || dias.length === 0) return '';

  const partes = dias.map((d) => {
    const meta = DIAS_META[d];
    if (!meta) return d;
    return meta.genero === 'M' ? `aos ${meta.nomePlural}` : `às ${meta.nomePlural}`;
  });

  if (partes.length === 1) {
    return partes[0];
  }

  const todosMesmoGenero = dias.every((d) => DIAS_META[d]?.genero === DIAS_META[dias[0]]?.genero);
  if (todosMesmoGenero) {
    const prep = DIAS_META[dias[0]]?.genero === 'M' ? 'aos' : 'às';
    const nomes = dias.map((d) => DIAS_META[d]?.nomePlural || d);
    if (nomes.length === 2) {
      return `${prep} ${nomes[0]} e ${nomes[1]}`;
    }
    return `${prep} ${nomes.slice(0, -1).join(', ')} e ${nomes[nomes.length - 1]}`;
  }

  if (partes.length === 2) {
    return `${partes[0]} e ${partes[1]}`;
  }
  return `${partes.slice(0, -1).join(', ')} e ${partes[partes.length - 1]}`;
}

/**
 * Formata uma regra de recorrência eclesial em linguagem natural, descritiva e sem abreviações.
 * Apresenta a periodicidade, dias da semana, modalidade mensal e a data calculada de término
 * ou indicação explícita de extensão indefinida.
 *
 * @param rule Objeto de regra de recorrência do evento.
 * @param dataInicioIso Data de início do evento (opcional, para projetar a data da última ocorrência).
 */
export function formatarResumoRecorrencia(rule?: RegraRecorrencia | null, dataInicioIso?: string): string {
  if (!rule) return '';

  const { frequencia, intervalo = 1, diasDaSemana, posicaoNoMes, diaDoMes, dataLimite, numeroOcorrencias } = rule;

  let base = '';

  if (frequencia === 'DIARIA') {
    base = intervalo === 1 ? 'Diariamente' : `A cada ${intervalo} dias`;
  } else if (frequencia === 'SEMANAL') {
    const freqTexto = intervalo === 1 ? 'Semanalmente' : `A cada ${intervalo} semanas`;
    if (diasDaSemana && diasDaSemana.length > 0) {
      const diasTexto = formatarDiasSemanaComPreposicao(diasDaSemana);
      base = `${freqTexto}, ${diasTexto}`;
    } else {
      base = freqTexto;
    }
  } else if (frequencia === 'MENSAL') {
    const freqTexto = intervalo === 1 ? 'Mensalmente' : `A cada ${intervalo} meses`;
    if (posicaoNoMes && diasDaSemana && diasDaSemana.length > 0) {
      const dia = diasDaSemana[0];
      const meta = DIAS_META[dia];
      const genero = meta ? meta.genero : 'M';
      const ordinal = POSICAO_ORDINAL[posicaoNoMes]?.[genero] || posicaoNoMes.toLowerCase();
      const nomeDia = meta ? meta.nomeSingular : dia;
      const artigo = genero === 'M' ? 'no' : 'na';
      base = `${freqTexto}, ${artigo} ${ordinal} ${nomeDia} do mês`;
    } else if (diaDoMes) {
      base = `${freqTexto}, todo dia ${diaDoMes}`;
    } else {
      base = freqTexto;
    }
  } else {
    base = 'Recorrência configurada';
  }

  let termino = '';
  if (dataLimite) {
    const [ano, mes, dia] = dataLimite.split('T')[0].split('-').map(Number);
    const dataBr = new Date(ano, mes - 1, dia).toLocaleDateString('pt-BR');
    termino = `Término em ${dataBr}`;
  } else if (numeroOcorrencias && numeroOcorrencias > 0) {
    const ocorrenciasTexto = numeroOcorrencias === 1 ? '1 ocorrência' : `${numeroOcorrencias} ocorrências`;
    let dataFinalTexto = '';
    if (dataInicioIso) {
      const inicio = new Date(dataInicioIso);
      if (!isNaN(inicio.getTime())) {
        const limiteFuturo = new Date(inicio.getTime() + 50 * 365 * 24 * 60 * 60 * 1000);
        const datas = getRecurrenceDates(inicio, rule, limiteFuturo);
        if (datas.length > 0) {
          const ultimaData = datas[datas.length - 1];
          dataFinalTexto = ` (término em ${ultimaData.toLocaleDateString('pt-BR')})`;
        }
      }
    }
    termino = `Total de ${ocorrenciasTexto}${dataFinalTexto}`;
  } else {
    termino = 'Estende-se indefinidamente';
  }

  return `${base} · ${termino}`;
}
