package br.com.nsfatima.gestao.clero.infrastructure.persistence.mapper;

import br.com.nsfatima.gestao.clero.api.v1.dto.ClerigoInstitucionalResponse;
import br.com.nsfatima.gestao.clero.api.v1.dto.ClerigoResponse;
import br.com.nsfatima.gestao.clero.api.v1.dto.EnderecoClerigoDto;
import br.com.nsfatima.gestao.clero.domain.model.Clerigo;
import br.com.nsfatima.gestao.clero.domain.model.EnderecoClerigo;
import br.com.nsfatima.gestao.clero.domain.model.FuncaoEclesiastica;
import br.com.nsfatima.gestao.clero.infrastructure.persistence.entity.ClerigoEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Aplica máscaras somente na saída autorizada da API. */
@Component
public class ClerigoMapper {
    private final String paroquia;

    public ClerigoMapper(@Value("${app.paroquia.nome:Paróquia Nossa Senhora de Fátima}") String paroquia) {
        this.paroquia = paroquia;
    }

    public ClerigoResponse completo(ClerigoEntity clerigo, FuncaoEclesiastica funcao) {
        return new ClerigoResponse(
                clerigo.getId(),
                clerigo.getNomeCivil(),
                clerigo.getNomeEclesiastico(),
                clerigo.getDataNascimento(),
                clerigo.getPaisOrigem(),
                cpf(clerigo.getCpf()),
                clerigo.getRg(),
                clerigo.getDocumentoEstrangeiro(),
                clerigo.getEmail(),
                telefone(clerigo.getCelular()),
                clerigo.isWhatsapp(),
                telefone(clerigo.getTelefoneFixo()),
                toEnderecoDto(clerigo),
                clerigo.getGrauOrdem(),
                clerigo.getDioceseIncardinacao(),
                clerigo.getDataOrdenacaoDiaconal(),
                clerigo.getDataOrdenacaoPresbiteral(),
                clerigo.getDataOrdenacaoEpiscopal(),
                funcao,
                clerigo.isAtivo());
    }

    public ClerigoInstitucionalResponse institucional(ClerigoEntity clerigo, FuncaoEclesiastica funcao) {
        return new ClerigoInstitucionalResponse(
                clerigo.getId(),
                clerigo.getNomeEclesiastico(),
                clerigo.getGrauOrdem(),
                funcao,
                paroquia);
    }

    /** Reconstitui o agregado {@link Clerigo} a partir da entidade persistida (valores já decifrados). */
    public Clerigo toDomain(ClerigoEntity e) {
        if (e == null) {
            return null;
        }
        return new Clerigo(
                e.getId(), e.getNomeCivil(), e.getNomeEclesiastico(), e.getDataNascimento(), e.getPaisOrigem(),
                e.getCpf(), e.getRg(), e.getDocumentoEstrangeiro(), e.getEmail(), e.getCelular(), e.isWhatsapp(),
                e.getTelefoneFixo(), toEnderecoDomain(e), e.getGrauOrdem(), e.getDioceseIncardinacao(),
                e.getDataOrdenacaoDiaconal(), e.getDataOrdenacaoPresbiteral(), e.getDataOrdenacaoEpiscopal(),
                e.isAtivo(), e.getDeletedAt(), e.getCriadoEmUtc(), e.getAtualizadoEmUtc(),
                e.getCriadoPor(), e.getAtualizadoPor());
    }

    /** Nova entidade a partir de um agregado recém-cadastrado. */
    public ClerigoEntity toEntity(Clerigo c) {
        ClerigoEntity e = new ClerigoEntity();
        e.setId(c.getId());
        e.setCriadoEmUtc(c.getCriadoEmUtc());
        e.setCriadoPor(c.getCriadoPor());
        aplicar(c, e);
        return e;
    }

    /** Aplica o estado do agregado sobre a entidade JPA gerenciada. */
    public void aplicar(Clerigo c, ClerigoEntity e) {
        e.setNomeCivil(c.getNomeCivil());
        e.setNomeEclesiastico(c.getNomeEclesiastico());
        e.setDataNascimento(c.getDataNascimento());
        e.setPaisOrigem(c.getPaisOrigem());
        e.setCpf(c.getCpf());
        e.setRg(c.getRg());
        e.setDocumentoEstrangeiro(c.getDocumentoEstrangeiro());
        e.setEmail(c.getEmail());
        e.setCelular(c.getCelular());
        e.setWhatsapp(c.isWhatsapp());
        e.setTelefoneFixo(c.getTelefoneFixo());
        if (c.getEndereco() != null) {
            e.setEnderecoCep(c.getEndereco().cep());
            e.setEnderecoLogradouro(c.getEndereco().logradouro());
            e.setEnderecoNumero(c.getEndereco().numero());
            e.setEnderecoComplemento(c.getEndereco().complemento());
            e.setEnderecoBairro(c.getEndereco().bairro());
            e.setEnderecoCidade(c.getEndereco().cidade());
            e.setEnderecoUf(c.getEndereco().uf());
            e.setEndereco(c.getEndereco().formatado());
        }
        e.setGrauOrdem(c.getGrauOrdem());
        e.setDioceseIncardinacao(c.getDioceseIncardinacao());
        e.setDataOrdenacaoDiaconal(c.getDataOrdenacaoDiaconal());
        e.setDataOrdenacaoPresbiteral(c.getDataOrdenacaoPresbiteral());
        e.setDataOrdenacaoEpiscopal(c.getDataOrdenacaoEpiscopal());
        e.setAtivo(c.isAtivo());
        e.setDeletedAt(c.getDeletedAt());
        e.setAtualizadoEmUtc(c.getAtualizadoEmUtc());
        e.setAtualizadoPor(c.getAtualizadoPor());
    }

    public EnderecoClerigoDto toEnderecoDto(ClerigoEntity clerigo) {
        if (clerigo.getEnderecoCep() != null
                || clerigo.getEnderecoLogradouro() != null
                || clerigo.getEnderecoCidade() != null
                || clerigo.getEnderecoUf() != null) {
            return new EnderecoClerigoDto(
                    cep(clerigo.getEnderecoCep()),
                    clerigo.getEnderecoLogradouro(),
                    clerigo.getEnderecoNumero(),
                    clerigo.getEnderecoComplemento(),
                    clerigo.getEnderecoBairro(),
                    clerigo.getEnderecoCidade(),
                    clerigo.getEnderecoUf());
        }
        if (clerigo.getEndereco() != null && !clerigo.getEndereco().isBlank()) {
            return new EnderecoClerigoDto(null, clerigo.getEndereco(), null, null, null, null, null);
        }
        return null;
    }

    public EnderecoClerigo toEnderecoDomain(ClerigoEntity e) {
        if (e.getEnderecoCep() != null
                || e.getEnderecoLogradouro() != null
                || e.getEnderecoCidade() != null
                || e.getEnderecoUf() != null) {
            return new EnderecoClerigo(
                    e.getEnderecoCep(),
                    e.getEnderecoLogradouro(),
                    e.getEnderecoNumero(),
                    e.getEnderecoComplemento(),
                    e.getEnderecoBairro(),
                    e.getEnderecoCidade(),
                    e.getEnderecoUf());
        }
        if (e.getEndereco() != null && !e.getEndereco().isBlank()) {
            return new EnderecoClerigo(null, e.getEndereco(), null, null, null, null, null);
        }
        return EnderecoClerigo.vazio();
    }

    private String cep(String valor) {
        if (valor == null) {
            return null;
        }
        String limpo = valor.replaceAll("\\D", "");
        if (limpo.length() == 8) {
            return limpo.replaceFirst("(\\d{5})(\\d{3})", "$1-$2");
        }
        return valor;
    }

    private String cpf(String valor) {
        if (valor == null || valor.length() != 11) {
            return null;
        }
        return valor.replaceFirst("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4");
    }

    private String telefone(String valor) {
        if (valor == null) {
            return null;
        }
        if (valor.length() == 11) {
            return valor.replaceFirst("(\\d{2})(\\d{5})(\\d{4})", "($1) $2-$3");
        }
        if (valor.length() == 10) {
            return valor.replaceFirst("(\\d{2})(\\d{4})(\\d{4})", "($1) $2-$3");
        }
        return valor;
    }
}
