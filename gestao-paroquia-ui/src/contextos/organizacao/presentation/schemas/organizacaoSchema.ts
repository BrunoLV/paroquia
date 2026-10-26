import { z } from 'zod';

/**
 * Invariantes eclesiais por tipologia, espelhando `RegrasTipologiaOrganizacao` no backend:
 * pastoral e movimento exigem o vínculo com o catálogo oficial; pastoral e serviço paroquial exigem o múnus
 * (setor); conselho exige o subtipo; todo organismo (exceto conselho, cuja mãe é territorial) se articula a um
 * conselho pastoral (CPP/CPC), expressão da comunhão pastoral.
 */
function validarTipologia(
  data: {
    tipo: string;
    pastoralOficialId?: string;
    setorPastoral?: string;
    subtipoConselho?: string;
    parentId?: string;
  },
  ctx: z.RefinementCtx,
) {
  const issue = (path: string, message: string) => ctx.addIssue({ code: 'custom', path: [path], message });

  if ((data.tipo === 'PASTORAL' || data.tipo === 'MOVIMENTO') && !data.pastoralOficialId) {
    issue('pastoralOficialId', 'A associação oficial é obrigatória');
  }
  if ((data.tipo === 'PASTORAL' || data.tipo === 'SERVICO_PAROQUIAL') && !data.setorPastoral) {
    issue('setorPastoral', 'O múnus (ensinar, santificar ou governar) é obrigatório');
  }
  if (data.tipo === 'CONSELHO' && !data.subtipoConselho) {
    issue('subtipoConselho', 'O subtipo do conselho é obrigatório');
  }
  if (!data.parentId) {
    issue(
      'parentId',
      data.tipo === 'CONSELHO'
        ? 'Informe a Matriz ou Comunidade do conselho'
        : 'Articule o organismo a um conselho pastoral (CPP ou CPC)',
    );
  }
}

/** Schema de cadastro/edição de organismo eclesial (pastoral, movimento, serviço paroquial ou conselho). */
export const organizacaoSchema = z
  .object({
    nome: z.string().trim().min(1, 'O nome é obrigatório'),
    tipo: z.enum(['PASTORAL', 'MOVIMENTO', 'SERVICO_PAROQUIAL', 'CONSELHO']),
    email: z.string().trim().email('E-mail inválido').or(z.literal('')).optional(),
    telefone: z.string().trim().max(32, 'Telefone muito longo').optional(),
    parentId: z.string().optional(),
    pastoralOficialId: z.string().optional(),
    setorPastoral: z.enum(['SANTIFICACAO_LITURGIA', 'FORMACAO_ENSINAR', 'SOCIAL_CARIDADE', '']).optional(),
    subtipoConselho: z.enum(['CPP', 'CPC', 'CAEP', '']).optional(),
  })
  .superRefine(validarTipologia);

export type OrganizacaoFormInput = z.infer<typeof organizacaoSchema>;
