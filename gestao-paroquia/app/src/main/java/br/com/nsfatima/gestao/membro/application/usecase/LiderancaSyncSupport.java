package br.com.nsfatima.gestao.membro.application.usecase;

import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;

/**
 * Sincroniza o campo de liderança ({@code Organizacao.coordenador}) quando um vínculo é criado/atualizado com
 * um papel de coordenação, reaproveitado por {@link AdicionarMembroUseCase}, {@code AtualizarMembroUseCase} e
 * {@link PromoverACoordenadorUseCase}. {@code PapelOrganizacional} não possui um valor genérico
 * "vice-coordenador" (o antigo literal {@code "VICE_COORDENADOR"} usado por
 * {@code CreateMembroUseCase}/{@code AtualizarMembroUseCase} nunca correspondia a nenhum papel real, era código
 * morto) — por isso apenas o campo {@code coordenador} é sincronizado.
 */
final class LiderancaSyncSupport {

    private LiderancaSyncSupport() {
    }

    /**
     * Se {@code papel} for um papel de coordenação, atualiza {@code Organizacao.coordenador} com o nome
     * informado e persiste a organização.
     *
     * @param organizacaoRepository repositório usado para persistir a organização, se necessário
     * @param papel                 papel atribuído no vínculo
     * @param nome                  nome de exibição do Fiel vinculado
     * @param organizacao           organização a sincronizar
     */
    static void sync(OrganizacaoJpaRepository organizacaoRepository,
                      PapelOrganizacional papel, String nome, OrganizacaoEntity organizacao) {
        if (papel.isCoordenacao()) {
            organizacao.setCoordenador(nome);
            organizacaoRepository.save(organizacao);
        }
    }
}
