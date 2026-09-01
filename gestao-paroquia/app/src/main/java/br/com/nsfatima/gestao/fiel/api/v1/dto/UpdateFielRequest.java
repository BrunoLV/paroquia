package br.com.nsfatima.gestao.fiel.api.v1.dto;

import br.com.nsfatima.gestao.fiel.domain.model.EstadoCivil;
import br.com.nsfatima.gestao.fiel.domain.model.PreferenciaCanalComunicacao;
import br.com.nsfatima.gestao.fiel.domain.model.Sexo;
import br.com.nsfatima.gestao.fiel.domain.model.Vocacao;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record UpdateFielRequest(
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
        EnderecoDto endereco,
        EstadoCivil estadoCivil,
        UUID conjugeId,
        Vocacao vocacao,
        Boolean ativo,
        VidaSacramentalDto vidaSacramental,
        List<DependenteDto> dependentes,
        String habilidadesVoluntariado,
        ConsentimentoLgpdDto consentimentoLgpd
) {
}
