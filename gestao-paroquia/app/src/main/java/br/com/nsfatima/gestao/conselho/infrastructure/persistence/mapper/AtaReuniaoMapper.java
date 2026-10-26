package br.com.nsfatima.gestao.conselho.infrastructure.persistence.mapper;

import br.com.nsfatima.gestao.conselho.domain.model.AtaReuniao;
import br.com.nsfatima.gestao.conselho.domain.model.AtaReuniaoId;
import br.com.nsfatima.gestao.conselho.domain.model.ConteudoAta;
import br.com.nsfatima.gestao.conselho.infrastructure.persistence.entity.AtaReuniaoEntity;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * Converte entre o agregado {@link AtaReuniao} e a {@link AtaReuniaoEntity}. O conteúdo é gravado como
 * JSON dos próprios records de domínio; ao ler, os construtores compactos revalidam tudo, de modo que dado
 * adulterado diretamente no banco não vira agregado inválido.
 *
 * <p>ponytail: o JSON acompanha a forma atual dos records; renomear componente de
 * {@code ConteudoAta} e filhos exige migração dos dados gravados.</p>
 */
@Component
public class AtaReuniaoMapper {

    private final ObjectMapper objectMapper;

    public AtaReuniaoMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public AtaReuniao toDomain(AtaReuniaoEntity e) {
        ConteudoAta conteudo = objectMapper.readValue(e.getConteudo(), ConteudoAta.class);
        return AtaReuniao.reconstituir(new AtaReuniao.Estado(
                new AtaReuniaoId(e.getId()), e.getTipo(), e.getOrganizacaoConvocanteId(),
                e.getOrganizacaoAssociadaId(), e.getRedatorId(), e.getCriadoEmUtc(),
                e.getAtaRetificadaId() == null ? null : new AtaReuniaoId(e.getAtaRetificadaId()),
                e.getJustificativaRetificacao(), conteudo, e.getStatus(), e.getNumero(), e.getAno(),
                e.getMembrosConvocados(), e.getHomologadorId(), e.getMotivoVeto(), e.getDecididoEmUtc()));
    }

    /** Copia o estado do agregado para a entidade, nova ou gerenciada. */
    public void aplicar(AtaReuniao ata, AtaReuniaoEntity e) {
        ConteudoAta conteudo = ata.getConteudo();
        e.setId(ata.getId().value());
        e.setTipo(ata.getTipo());
        e.setOrganizacaoConvocanteId(ata.getOrganizacaoConvocanteId());
        e.setOrganizacaoAssociadaId(ata.getOrganizacaoAssociadaId());
        e.setTitulo(conteudo.titulo());
        e.setDataReuniao(conteudo.dataReuniao());
        e.setCarater(conteudo.carater());
        e.setModalidade(conteudo.modalidade());
        e.setStatus(ata.getStatus());
        e.setConteudo(objectMapper.writeValueAsString(conteudo));
        e.setNumero(ata.getNumero());
        e.setAno(ata.getAno());
        e.setMembrosConvocados(ata.getMembrosConvocados());
        e.setRedatorId(ata.getRedatorId());
        e.setHomologadorId(ata.getHomologadorId());
        e.setMotivoVeto(ata.getMotivoVeto());
        e.setDecididoEmUtc(ata.getDecididoEmUtc());
        e.setAtaRetificadaId(ata.getAtaRetificadaId() == null ? null : ata.getAtaRetificadaId().value());
        e.setJustificativaRetificacao(ata.getJustificativaRetificacao());
    }
}
