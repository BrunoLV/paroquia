package br.com.nsfatima.gestao.sacramento.application.service;

import br.com.nsfatima.gestao.sacramento.domain.model.TipoSacramento;
import br.com.nsfatima.gestao.sacramento.domain.policy.RequisitosAssentoPolicy;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Seleciona a Strategy de requisitos canônicos pelo tipo do sacramento, sem if/else. */
@Service
public class RequisitosAssentoRegistry {

    private final Map<TipoSacramento, RequisitosAssentoPolicy> porTipo = new EnumMap<>(TipoSacramento.class);

    public RequisitosAssentoRegistry(List<RequisitosAssentoPolicy> policies) {
        policies.forEach(p -> porTipo.put(p.tipo(), p));
    }

    public RequisitosAssentoPolicy para(TipoSacramento tipo) {
        return porTipo.get(tipo);
    }
}
