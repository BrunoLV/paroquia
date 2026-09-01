package br.com.nsfatima.gestao.fiel.infrastructure.persistence.mapper;

import br.com.nsfatima.gestao.fiel.api.v1.dto.ConsentimentoLgpdDto;
import br.com.nsfatima.gestao.fiel.api.v1.dto.DependenteDto;
import br.com.nsfatima.gestao.fiel.api.v1.dto.EnderecoDto;
import br.com.nsfatima.gestao.fiel.api.v1.dto.FielResponse;
import br.com.nsfatima.gestao.fiel.api.v1.dto.VidaSacramentalDto;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielDependenteEntity;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielEntity;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class FielMapper {

    public FielResponse toResponse(FielEntity entity) {
        if (entity == null) {
            return null;
        }

        EnderecoDto enderecoDto = new EnderecoDto(
                entity.getEnderecoLogradouro(),
                entity.getEnderecoNumero(),
                entity.getEnderecoComplemento(),
                entity.getEnderecoBairro(),
                entity.getEnderecoCep(),
                entity.getEnderecoCidade(),
                entity.getEnderecoEstado()
        );

        VidaSacramentalDto vidaSacramentalDto = new VidaSacramentalDto(
                entity.isBatizado(),
                entity.getDataBatismo(),
                entity.getParoquiaBatismo(),
                entity.getCelebranteBatismo(),
                entity.getLivroBatismo(),
                entity.getFolhaBatismo(),
                entity.getTermoBatismo(),
                entity.getPadrinhosBatismo(),
                entity.isPrimeiraComunhao(),
                entity.getDataPrimeiraComunhao(),
                entity.getParoquiaPrimeiraComunhao(),
                entity.getTurmaCatequese(),
                entity.isCrismado(),
                entity.getDataCrisma(),
                entity.getParoquiaCrisma(),
                entity.getCelebranteCrisma(),
                entity.getPadrinhoCrisma(),
                entity.isCasadoIgrejaCatolica(),
                entity.getDataMatrimonio(),
                entity.getParoquiaMatrimonio(),
                entity.getNumeroProcessoMatrimonial(),
                entity.getCertidaoMatrimonio()
        );

        ConsentimentoLgpdDto consentimentoLgpdDto = new ConsentimentoLgpdDto(
                entity.isTermoConsentimentoAceito(),
                entity.getDataHoraConsentimentoUtc(),
                entity.getIpConsentimento(),
                entity.getVersaoTermoConsentimento(),
                entity.isUsoImagemAutorizado(),
                entity.isPermitirDivulgacaoAniversario()
        );

        List<DependenteDto> dependentes = entity.getDependentes() != null
                ? entity.getDependentes().stream().map(this::toDependenteDto).toList()
                : Collections.emptyList();

        return new FielResponse(
                entity.getId(),
                entity.getNome(),
                entity.getCpf(),
                entity.getRg(),
                entity.getDataNascimento(),
                entity.getSexo(),
                entity.getProfissao(),
                entity.getEmail(),
                entity.getTelefone(),
                entity.getTelefoneFixo(),
                entity.getCelularWhatsapp(),
                entity.getPreferenciaCanalComunicacao(),
                enderecoDto,
                entity.getEstadoCivil(),
                entity.getConjugeId(),
                entity.getVocacao(),
                entity.isAtivo(),
                vidaSacramentalDto,
                dependentes,
                entity.getHabilidadesVoluntariado(),
                consentimentoLgpdDto
        );
    }

    private DependenteDto toDependenteDto(FielDependenteEntity entity) {
        if (entity == null) {
            return null;
        }
        return new DependenteDto(
                entity.getId(),
                entity.getNome(),
                entity.getDataNascimento(),
                entity.getParentesco(),
                entity.isBatizado(),
                entity.isPrimeiraComunhao(),
                entity.isCrismado()
        );
    }
}
