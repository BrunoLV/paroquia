package br.com.nsfatima.gestao.projeto.domain.policy;

import br.com.nsfatima.gestao.calendario.domain.type.RegraRecorrencia;
import br.com.nsfatima.gestao.projeto.domain.model.ProjetoStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

/** Impede vínculos em projetos terminais ou fora do período formal aprovado. */
public final class PeriodoProjetoVincularPolicy implements ProjetoVincularPolicy {

    private final ZoneId fuso;

    /**
     * Cria a política para o fuso civil da paróquia, que define o dia civil do fim do projeto e do evento.
     *
     * @param fuso fuso civil da paróquia
     */
    public PeriodoProjetoVincularPolicy(ZoneId fuso) {
        this.fuso = fuso;
    }

    @Override
    public void validarEvento(ProjetoVinculo projeto, Instant inicioEvento, Instant fimEvento) {
        validarProjeto(projeto);
        boolean foraDoPeriodo = possuiPeriodo(projeto)
                && (inicioEvento.isBefore(projeto.inicio()) || fimEvento.isAfter(projeto.fim()));
        if (foraDoPeriodo) {
            throw new IllegalArgumentException("O período do evento está fora do limite definido para o projeto "
                    + projeto.nome());
        }
    }

    @Override
    public void validarRecorrencia(
            ProjetoVinculo projeto, Instant inicioEvento, Instant fimEvento, RegraRecorrencia regra) {
        validarEvento(projeto, inicioEvento, fimEvento);
        if (!possuiPeriodo(projeto)) {
            return;
        }
        LocalDate fimProjeto = projeto.fim().atZone(fuso).toLocalDate();
        if (regra.dataLimite() == null && regra.numeroOcorrencias() == null) {
            throw new IllegalArgumentException("Para eventos vinculados a projetos, a repetição deve possuir limite.");
        }
        if (regra.dataLimite() != null && regra.dataLimite().isAfter(fimProjeto)) {
            throw new IllegalArgumentException("A data limite da recorrência excede o período definido para o projeto "
                    + projeto.nome());
        }
        if (regra.numeroOcorrencias() != null) {
            List<LocalDate> datas = regra.gerarDatas(
                    inicioEvento.atZone(fuso).toLocalDate(), fimProjeto.plusYears(5));
            int ultimaOcorrencia = Math.min(datas.size(), regra.numeroOcorrencias()) - 1;
            if (datas.isEmpty() || datas.get(ultimaOcorrencia).isAfter(fimProjeto)) {
                throw new IllegalArgumentException(
                        "As ocorrências da repetição excedem o período definido para o projeto "
                                + projeto.nome());
            }
        }
    }

    private void validarProjeto(ProjetoVinculo projeto) {
        if (projeto.status() == ProjetoStatus.CANCELADO || projeto.status() == ProjetoStatus.CONCLUIDO) {
            throw new IllegalStateException("Não é possível vincular evento a um projeto CANCELADO ou CONCLUÍDO");
        }
    }

    private boolean possuiPeriodo(ProjetoVinculo projeto) {
        return projeto.inicio() != null && projeto.fim() != null;
    }
}
