package br.com.nsfatima.gestao.conselho.application.usecase;

import br.com.nsfatima.gestao.conselho.application.dto.TipoReuniaoInfo;
import br.com.nsfatima.gestao.conselho.domain.model.TipoReuniao;
import br.com.nsfatima.gestao.conselho.domain.policy.RegrasTipoReuniaoPolicy;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/** Catálogo dos tipos de reunião com as regras de cada um, para a interface montar o formulário da ata. */
@Service
public class ListarTiposReuniaoUseCase {

    /**
     * Lista os tipos e suas regras.
     *
     * @return todos os tipos de reunião
     */
    public List<TipoReuniaoInfo> execute() {
        return Arrays.stream(TipoReuniao.values())
                .map(tipo -> new TipoReuniaoInfo(tipo, RegrasTipoReuniaoPolicy.para(tipo)))
                .toList();
    }
}
