package br.com.nsfatima.gestao.fiel.infrastructure.persistence.mapper;

import br.com.nsfatima.gestao.fiel.api.v1.dto.ContatoDto;
import br.com.nsfatima.gestao.fiel.api.v1.dto.EnderecoDto;
import br.com.nsfatima.gestao.fiel.api.v1.dto.FielContatoResponse;
import br.com.nsfatima.gestao.fiel.api.v1.dto.FielResponse;
import br.com.nsfatima.gestao.fiel.api.v1.dto.IdentificacaoDto;
import br.com.nsfatima.gestao.fiel.api.v1.dto.IgrejaFrequentaDto;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.UnidadeTerritorialJpaRepository;
import org.springframework.stereotype.Component;

/** Traduz PII cifrada em valores de apresentação, sem retornar o valor bruto. */
@Component
public class FielMapper {
    private final UnidadeTerritorialJpaRepository unidadeRepository;

    public FielMapper(UnidadeTerritorialJpaRepository unidadeRepository) {
        this.unidadeRepository = unidadeRepository;
    }

    public FielResponse toResponse(FielEntity fiel) {
        var unidade = unidadeRepository.findById(fiel.getUnidadeTerritorialId()).orElse(null);
        return new FielResponse(
                fiel.getId(),
                fiel.getNome(),
                fiel.getSexo(),
                fiel.getDataNascimento(),
                new IdentificacaoDto(
                        cpf(fiel.getCpf()),
                        fiel.getRg(),
                        fiel.getPaisOrigem(),
                        fiel.getDocumentoEstrangeiroTipo(),
                        fiel.getDocumentoEstrangeiroNumero()),
                new ContatoDto(
                        telefone(fiel.getCelular()),
                        fiel.isWhatsapp(),
                        telefone(fiel.getTelefoneFixo()),
                        fiel.getEmail()),
                new EnderecoDto(
                        cep(fiel.getEnderecoCep()),
                        fiel.getEnderecoLogradouro(),
                        fiel.getEnderecoNumero(),
                        fiel.getEnderecoComplemento(),
                        fiel.getEnderecoBairro(),
                        fiel.getEnderecoCidade(),
                        fiel.getEnderecoEstado()),
                unidade == null ? null : new IgrejaFrequentaDto(
                        unidade.getId(),
                        unidade.getNome(),
                        unidade.getTipo().name()),
                fiel.isDizimista(),
                fiel.isPermitirDivulgacaoAniversario(),
                fiel.isAtivo());
    }

    public FielContatoResponse toContatoResponse(FielEntity fiel) {
        return new FielContatoResponse(fiel.getId(), fiel.getNome(), contato(fiel));
    }

    private ContatoDto contato(FielEntity fiel) {
        return new ContatoDto(
                telefone(fiel.getCelular()),
                fiel.isWhatsapp(),
                telefone(fiel.getTelefoneFixo()),
                fiel.getEmail());
    }

    private String cpf(String value) {
        if (value == null || value.length() != 11) {
            return null;
        }
        return value.replaceFirst("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4");
    }

    private String cep(String value) {
        if (value == null || value.length() != 8) {
            return null;
        }
        return value.replaceFirst("(\\d{5})(\\d{3})", "$1-$2");
    }

    private String telefone(String value) {
        if (value == null) {
            return null;
        }
        if (value.length() == 11) {
            return value.replaceFirst("(\\d{2})(\\d{5})(\\d{4})", "($1) $2-$3");
        }
        if (value.length() == 10) {
            return value.replaceFirst("(\\d{2})(\\d{4})(\\d{4})", "($1) $2-$3");
        }
        return value;
    }
}
