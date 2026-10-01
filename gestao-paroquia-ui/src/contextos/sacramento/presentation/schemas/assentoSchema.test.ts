import { describe, expect, it } from 'vitest';
import { Assento } from '@/contextos/sacramento/domain/sacramento';
import {
  AssentoFormInput, assentoParaForm, assentoSchema, paraTeorPayload, participanteVazio,
} from '@/contextos/sacramento/presentation/schemas/assentoSchema';

const base = (): AssentoFormInput => ({
  tipo: 'BATISMO',
  visibilidade: 'COMUM',
  livro: '',
  folha: '',
  termo: '',
  paroquiaCustodiante: '',
  localCelebracao: '',
  dataCelebracao: '',
  dadoSensivel: false,
  dadosEspecificos: {},
  participantes: [{ ...participanteVazio('BATIZANDO'), nomeHistorico: 'Maria da Silva' }],
});

describe('assentoSchema', () => {
  it('aceita rascunho sem livro, folha, termo nem cadastro de fiel', () => {
    expect(assentoSchema.safeParse(base()).success).toBe(true);
  });

  it('exige nome do participante quando não há vínculo a cadastro', () => {
    const dados = base();
    dados.participantes[0].nomeHistorico = '';
    expect(assentoSchema.safeParse(dados).success).toBe(false);
  });

  it('dispensa o nome quando há fielId (snapshot vem do cadastro)', () => {
    const dados = base();
    dados.participantes[0] = { ...dados.participantes[0], nomeHistorico: '', fielId: 'fiel-1' };
    expect(assentoSchema.safeParse(dados).success).toBe(true);
  });

  it('exige ao menos um participante', () => {
    expect(assentoSchema.safeParse({ ...base(), participantes: [] }).success).toBe(false);
  });
});

describe('paraTeorPayload', () => {
  it('omite campos vazios e acrescenta segundos à data da celebração', () => {
    const payload = paraTeorPayload({
      ...base(), livro: ' 7 ', dataCelebracao: '2026-03-01T10:00', dadosEspecificos: { grau: '', estadoFiliacao: 'ADOTIVA' },
    });
    expect(payload.livro).toBe('7');
    expect(payload.folha).toBeUndefined();
    expect(payload.dataCelebracao).toBe('2026-03-01T10:00:00');
    expect(payload.dadosEspecificos).toEqual({ estadoFiliacao: 'ADOTIVA' });
  });
});

describe('assentoParaForm', () => {
  it('preserva ids e vínculos tardios ao reabrir o rascunho', () => {
    const assento = {
      id: 'a1', tipo: 'BATISMO', status: 'DRAFT', visibilidade: 'COMUM', dataLavratura: '2026-01-01', livro: '7',
      folha: null, termo: null, paroquiaCustodiante: null, dataCelebracao: '2026-03-01T10:00:00',
      localCelebracao: null, dadoSensivel: false, dadosEspecificos: {}, averbacoes: [], ressalvas: [],
      notificacoes: [], homologadoPor: null, homologadoEmUtc: null, motivoCancelamento: null,
      participantes: [{
        id: 'p1', papel: 'BATIZANDO', nomeHistorico: 'Maria', documento: '123', localNascimento: null,
        catolico: null, crismado: null, idadeNaCelebracao: null, condicaoCanonica: null, fielId: 'f1', clerigoId: null,
      }],
    } as Assento;
    const form = assentoParaForm(assento);
    expect(form.dataCelebracao).toBe('2026-03-01T10:00');
    expect(form.participantes[0]).toMatchObject({ id: 'p1', fielId: 'f1', documento: '123' });
  });
});
