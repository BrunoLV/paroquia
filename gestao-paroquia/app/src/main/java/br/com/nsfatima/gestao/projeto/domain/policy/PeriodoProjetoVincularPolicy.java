package br.com.nsfatima.gestao.projeto.domain.policy;

import br.com.nsfatima.gestao.calendario.domain.type.RegraRecorrencia;
import br.com.nsfatima.gestao.projeto.domain.model.ProjetoStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

/** Impede vínculos em projetos terminais ou fora do período formal aprovado. */
public final class PeriodoProjetoVincularPolicy implements ProjetoVincularPolicy {

    @Override
    public void validarEvento(ProjetoVinculo projeto, Instant inicioEvento, Instant fimEvento) {
        validarProjeto(projeto);
        if (possuiPeriodo(projeto) && (inicioEvento.isBefore(projeto.inicio()) || fimEvento.isAfter(projeto.fim()))) {
            throw new IllegalArgumentException("O período do evento está fora do limite definido para o projeto "
                    + projeto.nome());
        }
    }

    @Override
    public void validarRecorrencia(ProjetoVinculo projeto, Instant inicioEvento, Instant fimEvento, RegraRecorrencia regra) {
        validarEvento(projeto, inicioEvento, fimEvento);
        if (!possuiPeriodo(projeto)) {
            return;
        }
        LocalDate fimProjeto = projeto.fim().atZone(ZoneOffset.UTC).toLocalDate();
        if (regra.dataLimite() == null && regra.numeroOcorrencias() == null) {
            throw new IllegalArgumentException("Para eventos vinculados a projetos, a repetição deve possuir limite.");
        }
        if (regra.dataLimite() != null && regra.dataLimite().isAfter(fimProjeto)) {
            throw new IllegalArgumentException("A data limite da recorrência excede o período definido para o projeto "
                    + projeto.nome());
        }
        if (regra.numeroOcorrencias() != null) {
            List<LocalDate> datas = regra.gerarDatas(inicioEvento.atZone(ZoneOffset.UTC).toLocalDate(), fimProjeto.plusYears(5));
            if (datas.isEmpty() || datas.get(Math.min(datas.size(), regra.numeroOcorrencias()) - 1).isAfter(fimProjeto)) {
                throw new IllegalArgumentException("As ocorrências da repetição excedem o período definido para o projeto "
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
