package br.com.nsfatima.gestao.sacramento.application.service;

import br.com.nsfatima.gestao.sacramento.application.port.out.NotificadorVinculoPort;
import br.com.nsfatima.gestao.sacramento.domain.model.AssentoSacramental;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

/**
 * Detecta vínculos recém-propostos pela Secretaria (ficaram pendentes após a mutação) e avisa os
 * Párocos uma única vez por operação.
 */
@Service
public class NotificacaoVinculoService {

    private final NotificadorVinculoPort notificador;

    public NotificacaoVinculoService(NotificadorVinculoPort notificador) {
        this.notificador = notificador;
    }

    /**
     * Fotografa os vínculos pendentes antes da mutação.
     *
     * @param assento agregado ainda não mutado
     * @return chaves participante:pessoa dos vínculos pendentes
     */
    public Set<String> pendentes(AssentoSacramental assento) {
        Set<String> chaves = new HashSet<>();
        assento.getTeor().participantes().stream().filter(p -> p.vinculoPendente())
                .forEach(p -> chaves.add(p.id() + ":" + (p.fielId() != null ? p.fielId() : p.clerigoId())));
        return chaves;
    }

    /**
     * Notifica os Párocos se a mutação criou vínculos pendentes.
     *
     * @param autor login de quem agiu
     * @param antes pendências antes da mutação
     * @param depois agregado já mutado
     */
    public void notificarNovos(String autor, Set<String> antes, AssentoSacramental depois) {
        Set<String> novos = pendentes(depois);
        novos.removeAll(antes);
        if (!novos.isEmpty()) {
            notificador.notificarParocos(autor, depois.getTipo().name(), novos.size());
        }
    }
}
