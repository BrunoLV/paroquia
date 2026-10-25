import { describe, expect, it } from 'vitest';
import { TipoReuniaoInfo } from '@/contextos/conselho/domain/ata';
import {
  AtaFormInput,
  ataSchema,
  ataVazia,
  deliberacaoVazia,
  itemVazio,
  paraConteudoPayload,
  paraRascunhoPayload,
} from '@/contextos/conselho/presentation/schemas/ataSchema';

const regras = (parcial: Partial<TipoReuniaoInfo> = {}): TipoReuniaoInfo => ({
  codigo: 'REUNIAO_CPP',
  rotulo: 'Reunião do CPP',
  exigeHomologacao: true,
  aplicaQuorum: true,
  modoPresenca: 'NOMINAL_MEMBROS',
  permiteVotacao: true,
  exigeOrganizacaoAssociada: false,
  tiposConvocantes: ['CONSELHO'],
  subtiposConselho: ['CPP'],
  papeisRedatores: ['SECRETARIO_CONSELHO'],
  ...parcial,
});

const valida = (): AtaFormInput => ({
  ...ataVazia('2026-06-01'),
  tipo: 'REUNIAO_CPP',
  organizacaoConvocanteId: 'org-1',
  titulo: ' Reunião de junho ',
  local: 'Salão paroquial',
  presencas: [{ fielId: 'f1', clerigo: false, funcao: 'Coordenador(a) de pastoral ou organismo', situacao: 'PRESENTE' }],
  itens: [
    {
      ...itemVazio(),
      titulo: 'Reforma',
      relato: '  ',
      deliberacoes: [
        { ...deliberacaoVazia(), texto: ' Aprovar ', comVotacao: true, favor: 2 },
        { ...deliberacaoVazia(), texto: 'Adiar', resultado: 'ADIADA' },
      ],
    },
  ],
});

describe('ataSchema', () => {
  it('aceita rascunho mínimo, sem itens nem presença', () => {
    const rascunho = { ...ataVazia('2026-06-01'), tipo: 'REUNIAO_CPP', organizacaoConvocanteId: 'o', titulo: 'T', local: 'L' };

    expect(ataSchema.safeParse(rascunho).success).toBe(true);
  });

  it('exige tipo, órgão, título, data e local', () => {
    const resultado = ataSchema.safeParse(ataVazia(''));
    const caminhos = resultado.success ? [] : resultado.error.issues.map((i) => i.path[0]);

    expect(caminhos).toEqual(expect.arrayContaining(['tipo', 'organizacaoConvocanteId', 'titulo', 'dataReuniao', 'local']));
  });

  it('exige ressalvas quando a ata anterior é aprovada com ressalvas', () => {
    const dados = { ...valida(), ataAnteriorId: 'a1', aprovacaoAtaAnterior: 'APROVADA_COM_RESSALVAS' as const };

    const resultado = ataSchema.safeParse(dados);

    expect(resultado.success).toBe(false);
    expect(!resultado.success && resultado.error.issues[0].path).toEqual(['ressalvasAtaAnterior']);
  });

  it('barra pessoa repetida, função única repetida e função de quem não esteve presente', () => {
    const presencas = [
      { fielId: 'f1', clerigo: false, funcao: 'Secretário(a)', situacao: 'PRESENTE' as const },
      { fielId: 'f2', clerigo: false, funcao: 'Secretário(a)', situacao: 'PRESENTE' as const },
      { fielId: 'f1', clerigo: false, funcao: '', situacao: 'PRESENTE' as const },
      { fielId: 'f3', clerigo: false, funcao: 'Membro', situacao: 'AUSENTE' as const },
      { fielId: 'f4', clerigo: false, funcao: 'Membro', situacao: 'PRESENTE' as const },
      { fielId: 'f5', clerigo: false, funcao: 'Membro', situacao: 'PRESENTE' as const },
    ];

    const resultado = ataSchema.safeParse({ ...valida(), presencas });
    const caminhos = resultado.success ? [] : resultado.error.issues.map((i) => i.path.join('.'));

    expect(caminhos).toEqual(['presencas.1.funcao', 'presencas.2.fielId', 'presencas.3.funcao']);
  });

  it('não aceita ata anterior sem dizer se foi aprovada', () => {
    const resultado = ataSchema.safeParse({ ...valida(), ataAnteriorId: 'a1' });

    expect(resultado.success).toBe(false);
    expect(!resultado.success && resultado.error.issues[0].path).toEqual(['aprovacaoAtaAnterior']);
  });

  it('rejeita conflito de interesses sem declarante e votos negativos', () => {
    const dados = valida();
    dados.itens[0].deliberacoes[0].favor = -1;
    dados.itens[0].deliberacoes[0].conflitos = [{ declaranteFielId: '', descricao: '' }];

    expect(ataSchema.safeParse(dados).success).toBe(false);
  });
});

describe('paraConteudoPayload', () => {
  it('aparamos textos, descartamos vazios e mantemos a votação onde o tipo vota', () => {
    const payload = paraConteudoPayload(valida(), regras());

    expect(payload.titulo).toBe('Reunião de junho');
    expect(payload.presenca.nominal).toEqual([{ fielId: 'f1', funcao: 'Coordenador(a) de pastoral ou organismo', situacao: 'PRESENTE' }]);
    const comClero = paraConteudoPayload({
      ...valida(),
      presencas: [{ fielId: 'c1', clerigo: true, funcao: 'Presidente', situacao: 'PRESENTE' }],
    }, regras());
    expect(comClero.presenca.nominal).toEqual([{ fielId: 'c1', clerigo: true, funcao: 'Presidente', situacao: 'PRESENTE' }]);
    expect(payload.itens[0].relato).toBeUndefined();
    expect(payload.itens[0].deliberacoes[0]).toMatchObject({ texto: 'Aprovar', votacao: { favor: 2, contra: 0, abstencoes: 0 } });
    expect(payload.itens[0].deliberacoes[1].votacao).toBeUndefined();
  });

  it('descarta a votação quando o tipo não vota', () => {
    const payload = paraConteudoPayload(valida(), regras({ permiteVotacao: false }));

    expect(payload.itens[0].deliberacoes[0].votacao).toBeUndefined();
  });

  it('presença quantitativa envia só o total', () => {
    const dados = { ...valida(), totalParticipantes: 40 };

    const payload = paraConteudoPayload(dados, regras({ modoPresenca: 'QUANTITATIVA' }));

    expect(payload.presenca).toEqual({ total: 40 });
  });

  it('sem ata anterior envia aprovação "não se aplica" e ignora ressalvas soltas', () => {
    const payload = paraConteudoPayload({ ...valida(), ressalvasAtaAnterior: 'lixo' }, regras());

    expect(payload.ataAnterior).toEqual({ aprovacao: 'NAO_APLICAVEL' });
  });
});

describe('paraRascunhoPayload', () => {
  it('só envia o órgão associado na reunião conjunta', () => {
    const dados = { ...valida(), organizacaoAssociadaId: 'caep-1' };

    expect(paraRascunhoPayload(dados, regras()).organizacaoAssociadaId).toBeUndefined();
    expect(paraRascunhoPayload(dados, regras({ exigeOrganizacaoAssociada: true })).organizacaoAssociadaId).toBe('caep-1');
  });
});
