package br.com.nsfatima.gestao.sacramento.application.service;

import br.com.nsfatima.gestao.sacramento.api.v1.dto.ParticipanteRequest;
import br.com.nsfatima.gestao.sacramento.api.v1.dto.TeorAssentoRequest;
import br.com.nsfatima.gestao.sacramento.application.port.out.ConsultaPessoaPort;
import br.com.nsfatima.gestao.sacramento.domain.model.Participante;
import br.com.nsfatima.gestao.sacramento.domain.model.ReferenciaLivro;
import br.com.nsfatima.gestao.sacramento.domain.model.TeorAssento;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Converte o teor recebido em {@link TeorAssento}, aplicando o late binding: o vínculo a fiel ou
 * clérigo é opcional; quando informado, o cadastro precisa existir e o snapshot do nome é preenchido
 * a partir dele se o operador não o tiver digitado.
 */
@Service
public class TeorAssentoFactory {

    private final ConsultaPessoaPort pessoas;

    public TeorAssentoFactory(ConsultaPessoaPort pessoas) {
        this.pessoas = pessoas;
    }

    public TeorAssento criar(TeorAssentoRequest r) {
        List<Participante> participantes = Objects.requireNonNullElse(r.participantes(), List.<ParticipanteRequest>of())
                .stream().map(this::participante).toList();
        return new TeorAssento(new ReferenciaLivro(vazioParaNulo(r.livro()), vazioParaNulo(r.folha()),
                vazioParaNulo(r.termo())), r.paroquiaCustodiante(), r.dataCelebracao(), r.localCelebracao(),
                participantes, r.dadosEspecificos(), Boolean.TRUE.equals(r.dadoSensivel()));
    }

    private Participante participante(ParticipanteRequest p) {
        UUID id = p.id() != null ? p.id() : UUID.randomUUID();
        String nome = p.nomeHistorico();
        if (p.fielId() != null && p.clerigoId() != null) {
            throw new IllegalArgumentException("Participante não pode apontar simultaneamente a fiel e clérigo.");
        }
        if (p.fielId() != null) {
            String cadastro = pessoas.nomeDoFiel(p.fielId())
                    .orElseThrow(() -> new IllegalArgumentException("Fiel informado não está cadastrado."));
            nome = nome == null || nome.isBlank() ? cadastro : nome;
        }
        if (p.clerigoId() != null) {
            String cadastro = pessoas.nomeDoClerigo(p.clerigoId())
                    .orElseThrow(() -> new IllegalArgumentException("Clérigo informado não está cadastrado."));
            nome = nome == null || nome.isBlank() ? cadastro : nome;
        }
        Participante base = new Participante(id, p.papel(), nome, p.documento(), p.localNascimento(), p.catolico(),
                p.crismado(), p.idadeNaCelebracao(), p.condicaoCanonica(), null, null);
        if (p.fielId() != null) {
            return base.comFiel(p.fielId());
        }
        return p.clerigoId() != null ? base.comClerigo(p.clerigoId()) : base;
    }

    private static String vazioParaNulo(String v) {
        return v == null || v.isBlank() ? null : v.trim();
    }
}
