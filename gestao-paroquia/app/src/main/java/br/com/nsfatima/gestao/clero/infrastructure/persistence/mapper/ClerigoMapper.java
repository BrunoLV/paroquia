package br.com.nsfatima.gestao.clero.infrastructure.persistence.mapper;

import br.com.nsfatima.gestao.clero.api.v1.dto.ClerigoInstitucionalResponse;
import br.com.nsfatima.gestao.clero.api.v1.dto.ClerigoResponse;
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
                clerigo.getEndereco(),
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
