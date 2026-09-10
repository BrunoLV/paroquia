package br.com.nsfatima.gestao.membro.application.usecase;

import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.GrupoParoquialEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.GrupoParoquialJpaRepository;

/**
 * Sincroniza o campo de liderança ({@code GrupoParoquial.coordenador}) quando um vínculo é criado/atualizado com
 * um papel de coordenação, reaproveitado por {@link AdicionarMembroUseCase}, {@code UpdateMembroUseCase} e
 * {@link PromoverACoordenadorUseCase}. {@code PapelOrganizacional} não possui um valor genérico
 * "vice-coordenador" (o antigo literal {@code "VICE_COORDENADOR"} usado por
 * {@code CreateMembroUseCase}/{@code UpdateMembroUseCase} nunca correspondia a nenhum papel real, era código
 * morto) — por isso apenas o campo {@code coordenador} é sincronizado.
 */
final class LiderancaSyncSupport {

    private LiderancaSyncSupport() {
    }

    /**
     * Se {@code papel} for um papel de coordenação, atualiza {@code GrupoParoquial.coordenador} com o nome
     * informado e persiste a organização.
     *
     * @param grupoParoquialRepository repositório usado para persistir a organização, se necessário
     * @param papel                 papel atribuído no vínculo
     * @param nome                  nome de exibição do Fiel vinculado
     * @param grupoParoquial           organização a sincronizar
     */
    static void sync(GrupoParoquialJpaRepository grupoParoquialRepository,
                      PapelOrganizacional papel, String nome, GrupoParoquialEntity grupoParoquial) {
        if (papel.isCoordenacao()) {
            grupoParoquial.setCoordenador(nome);
            grupoParoquialRepository.save(grupoParoquial);
        }
    }
}
