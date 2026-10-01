package br.com.nsfatima.gestao.sacramento.application.service;

import br.com.nsfatima.gestao.sacramento.api.v1.dto.ParticipanteRequest;
import br.com.nsfatima.gestao.sacramento.api.v1.dto.TeorAssentoRequest;
import br.com.nsfatima.gestao.sacramento.application.port.out.ConsultaPessoaPort;
import br.com.nsfatima.gestao.sacramento.domain.model.Participante;
import br.com.nsfatima.gestao.sacramento.domain.model.ReferenciaLivro;
import br.com.nsfatima.gestao.sacramento.domain.model.TeorAssento;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

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

    /**
     * Monta o teor a partir do pedido.
     *
     * @param parocoAprova {@code true} se o ator é o Pároco: seus vínculos nascem aprovados; os da Secretaria
     *     entram pendentes
     * @param atuais participantes já lavrados (vazio na criação); a Secretaria não consegue remover nem trocar,
     *     por edição de rascunho, um vínculo existente — isso é ato do Pároco
     */
    public TeorAssento criar(TeorAssentoRequest r, boolean parocoAprova, List<Participante> atuais) {
        Map<UUID, Participante> existentes = atuais.stream().collect(Collectors.toMap(Participante::id, p -> p));
        List<Participante> participantes = Objects.requireNonNullElse(r.participantes(), List.<ParticipanteRequest>of())
                .stream().map(p -> participante(p, parocoAprova, existentes.get(p.id()))).toList();
        return new TeorAssento(new ReferenciaLivro(vazioParaNulo(r.livro()), vazioParaNulo(r.folha()),
                vazioParaNulo(r.termo())), r.paroquiaCustodiante(), r.dataCelebracao(), r.localCelebracao(),
                participantes, r.dadosEspecificos(), Boolean.TRUE.equals(r.dadoSensivel()));
    }

    private Participante participante(ParticipanteRequest p, boolean parocoAprova, Participante atual) {
        if (atual != null && atual.vinculado() && !parocoAprova) {
            return mantendoVinculo(p, atual);
        }
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
        boolean pendente = !parocoAprova;
        if (p.fielId() != null) {
            return base.comFiel(p.fielId(), pendente && !mesmoVinculo(atual, p));
        }
        return p.clerigoId() != null ? base.comClerigo(p.clerigoId(), pendente && !mesmoVinculo(atual, p)) : base;
    }

    private static boolean mesmoVinculo(Participante atual, ParticipanteRequest p) {
        return atual != null && ((p.fielId() != null && p.fielId().equals(atual.fielId()))
                || (p.clerigoId() != null && p.clerigoId().equals(atual.clerigoId())));
    }

    /** Edição pela Secretaria: o texto pode mudar no rascunho, mas o vínculo existente é preservado. */
    private static Participante mantendoVinculo(ParticipanteRequest p, Participante atual) {
        return new Participante(atual.id(), p.papel(), p.nomeHistorico() == null || p.nomeHistorico().isBlank()
                ? atual.nomeHistorico() : p.nomeHistorico(), p.documento(), p.localNascimento(), p.catolico(),
                p.crismado(), p.idadeNaCelebracao(), p.condicaoCanonica(), atual.fielId(), atual.clerigoId(),
                atual.vinculoPendente());
    }

    private static String vazioParaNulo(String v) {
        return v == null || v.isBlank() ? null : v.trim();
    }
}
