package br.com.nsfatima.gestao.fiel.domain.model;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Representa a pessoa física real (o paroquiano) no Contexto Paroquial, independente de qualquer credencial
 * de acesso ao sistema. Nunca possui e-mail de login ou senha — o {@code email} aqui é dado de contato,
 * conceitualmente distinto (e não sincronizado) do {@code username} de um eventual {@code Usuario} vinculado
 * via {@code fielId}.
 *
 * @param id             identificador do Fiel
 * @param nome           nome completo
 * @param cpf            CPF (documento único, criptografado em repouso via {@code FielEntity})
 * @param dataNascimento data de nascimento
 * @param email          e-mail de contato, independente de login
 * @param telefone       telefone de contato
 * @param endereco       endereço de contato
 * @param vocacao        estado de vida / vocação (LEIGO, SACERDOTE, DIACONO, RELIGIOSO)
 * @param ativo          se o cadastro está ativo
 */
public record Fiel(
        UUID id,
        String nome,
        String cpf,
        LocalDate dataNascimento,
        String email,
        String telefone,
        String endereco,
        Vocacao vocacao,
        boolean ativo
) {
}
