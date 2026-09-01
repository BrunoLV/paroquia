package br.com.nsfatima.gestao.fiel.domain.model;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Representa a pessoa física real (o paroquiano) no Contexto Paroquial, independente de qualquer credencial
 * de acesso ao sistema. Estruturado conforme diretrizes canônicas, CNBB Doc 106 e regras de LGPD.
 */
public record Fiel(
        UUID id,
        String nome,
        String cpf,
        String rg,
        LocalDate dataNascimento,
        Sexo sexo,
        String profissao,
        String email,
        String telefone,
        String telefoneFixo,
        String celularWhatsapp,
        PreferenciaCanalComunicacao preferenciaCanalComunicacao,
        Endereco endereco,
        EstadoCivil estadoCivil,
        UUID conjugeId,
        Vocacao vocacao,
        boolean ativo,
        VidaSacramental vidaSacramental,
        List<Dependente> dependentes,
        String habilidadesVoluntariado,
        ConsentimentoLgpd consentimentoLgpd
) {
    public Fiel {
        dependentes = dependentes != null ? List.copyOf(dependentes) : List.of();
    }

    // Métodos convenientes de compatibilidade para vida sacramental
    public boolean batizado() {
        return vidaSacramental != null && vidaSacramental.batizado();
    }

    public boolean primeiraComunhao() {
        return vidaSacramental != null && vidaSacramental.primeiraComunhao();
    }

    public boolean crismado() {
        return vidaSacramental != null && vidaSacramental.crismado();
    }

    public boolean casadoIgrejaCatolica() {
        return vidaSacramental != null && vidaSacramental.casadoIgrejaCatolica();
    }
}
