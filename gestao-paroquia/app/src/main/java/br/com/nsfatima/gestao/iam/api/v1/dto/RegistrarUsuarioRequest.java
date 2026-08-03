package br.com.nsfatima.gestao.iam.api.v1.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Dados do autocadastro público de um novo fiel/usuário (endpoint {@code POST /api/v1/usuarios/registrar},
 * sem autenticação). A {@code igrejaId} deve apontar para uma organização do tipo MATRIZ ou COMUNIDADE
 * (validado em {@code RegistrarUsuarioUseCase}); os campos sacramentais ({@code batizado},
 * {@code primeiraComunhao}, {@code crismado}, {@code casadoIgrejaCatolica}) são opcionais e usados apenas
 * para caracterização pastoral do fiel, não para controle de acesso.
 */
public record RegistrarUsuarioRequest(
        @NotBlank(message = "Nome é obrigatório")
        String nome,

        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail inválido")
        String username,

        @NotBlank(message = "Senha é obrigatória")
        String password,

        @NotBlank(message = "Telefone é obrigatório")
        String telefone,

        @NotNull(message = "Data de nascimento é obrigatória")
        LocalDate dataNascimento,

        @NotNull(message = "Igreja é obrigatória")
        UUID igrejaId,

        Boolean batizado,
        Boolean primeiraComunhao,
        Boolean crismado,
        Boolean casadoIgrejaCatolica
) {
    /**
     * Construtor de conveniência que assume todos os campos sacramentais como não preenchidos
     * ({@code false}), usado quando essas informações não são coletadas no fluxo de cadastro.
     */
    public RegistrarUsuarioRequest(String nome,
                                   String username, String password, String telefone, LocalDate dataNascimento,
                                   UUID igrejaId) {

        this(nome, username, password, telefone, dataNascimento, igrejaId, false, false, false, false);
    }

    /**
     * Alias de {@link #igrejaId()} usado onde o domínio se refere à igreja/comunidade de vínculo pelo
     * termo "community" (ex.: integrações que preferem terminologia neutra em vez de "igreja").
     *
     * @return o mesmo valor de {@link #igrejaId()}
     */
    public UUID communityId() {
        return igrejaId;
    }
}


