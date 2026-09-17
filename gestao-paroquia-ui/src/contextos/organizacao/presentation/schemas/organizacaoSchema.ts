import { z } from 'zod';

/**
 * Regra canônica: a associação com o cadastro oficial de Pastoral/Movimento
 * só é exigida quando `tipo` for `PASTORAL` ou `MOVIMENTO`. Espelha a
 * validação antes imposta manualmente em `OrganizacaoFormModal.handleSubmit`.
 */
function validarPastoralOficial(
  data: { tipo: string; pastoralOficialId?: string },
  ctx: z.RefinementCtx,
) {
  const exigeOficial = data.tipo === 'PASTORAL' || data.tipo === 'MOVIMENTO';
  if (exigeOficial && !data.pastoralOficialId) {
    ctx.addIssue({
      code: 'custom',
      path: ['pastoralOficialId'],
      message: 'A associação oficial é obrigatória',
    });
  }
}

/** Schema de cadastro/edição de Organização (Pastoral, Movimento, Clero, Matriz etc). */
export const organizacaoSchema = z
  .object({
    nome: z.string().trim().min(1, 'O nome é obrigatório'),
    tipo: z.enum([
      'PASTORAL',
      'MOVIMENTO',
      'CLERO',
      'MATRIZ',
      'COMUNIDADE',
      'CONSELHO_PASTORAL',
      'CONSELHO_ECONOMICO',
      'OUTRO',
    ]),
    contato: z.string().optional(),
    coordenador: z.string().optional(),
    viceCoordenador: z.string().optional(),
    parentId: z.string().optional(),
    pastoralOficialId: z.string().optional(),
  })
  .superRefine(validarPastoralOficial);

export type OrganizacaoFormInput = z.infer<typeof organizacaoSchema>;
