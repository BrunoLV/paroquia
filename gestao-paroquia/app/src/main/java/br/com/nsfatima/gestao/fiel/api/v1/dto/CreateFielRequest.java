package br.com.nsfatima.gestao.fiel.api.v1.dto;

import br.com.nsfatima.gestao.fiel.domain.model.EstadoCivil;
import br.com.nsfatima.gestao.fiel.domain.model.PreferenciaCanalComunicacao;
import br.com.nsfatima.gestao.fiel.domain.model.Sexo;
import br.com.nsfatima.gestao.fiel.domain.model.Vocacao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreateFielRequest(
        @NotBlank String nome,
        @NotBlank String cpf,
        String rg,
        @NotNull LocalDate dataNascimento,
        Sexo sexo,
        String profissao,
        @NotBlank String email,
        @NotBlank String telefone,
        String telefoneFixo,
        String celularWhatsapp,
        PreferenciaCanalComunicacao preferenciaCanalComunicacao,
        EnderecoDto endereco,
        EstadoCivil estadoCivil,
        UUID conjugeId,
        @NotNull Vocacao vocacao,
        VidaSacramentalDto vidaSacramental,
        List<DependenteDto> dependentes,
        String habilidadesVoluntariado,
        ConsentimentoLgpdDto consentimentoLgpd
) {
    public CreateFielRequest(
            String nome, String cpf, LocalDate dataNascimento,
            String email, String telefone, String endereco, Vocacao vocacao,
            Boolean batizado, Boolean primeiraComunhao, Boolean crismado, Boolean casadoIgrejaCatolica
    ) {
        this(nome, cpf, null, dataNascimento, Sexo.NAO_INFORMADO, null, email, telefone, null, null,
                PreferenciaCanalComunicacao.WHATSAPP, new EnderecoDto(endereco, null, null, null, null, null, null),
                EstadoCivil.SOLTEIRO, null, vocacao,
                new VidaSacramentalDto(batizado, null, null, null, null, null, null, null,
                        primeiraComunhao, null, null, null,
                        crismado, null, null, null, null,
                        casadoIgrejaCatolica, null, null, null, null),
                List.of(), null, new ConsentimentoLgpdDto(true, null, null, "v1.0", false, true));
    }
}
