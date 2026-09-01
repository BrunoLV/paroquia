package br.com.nsfatima.gestao.fiel.application.usecase;

import br.com.nsfatima.gestao.fiel.api.v1.dto.CreateFielRequest;
import br.com.nsfatima.gestao.fiel.api.v1.dto.DependenteDto;
import br.com.nsfatima.gestao.fiel.api.v1.dto.FielResponse;
import br.com.nsfatima.gestao.fiel.domain.model.Parentesco;
import br.com.nsfatima.gestao.fiel.domain.model.Sexo;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielDependenteEntity;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielEntity;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.mapper.FielMapper;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CreateFielUseCase {

    private final FielJpaRepository fielRepository;
    private final FielMapper fielMapper;

    public CreateFielUseCase(FielJpaRepository fielRepository, FielMapper fielMapper) {
        this.fielRepository = fielRepository;
        this.fielMapper = fielMapper;
    }

    @Transactional
    public FielResponse execute(CreateFielRequest request, String actor) {
        FielEntity entity = new FielEntity();
        entity.setId(UUID.randomUUID());
        entity.setNome(request.nome());
        entity.setCpf(request.cpf());
        entity.setRg(request.rg());
        entity.setDataNascimento(request.dataNascimento());
        entity.setSexo(request.sexo() != null ? request.sexo() : Sexo.NAO_INFORMADO);
        entity.setProfissao(request.profissao());
        entity.setEmail(request.email());
        entity.setTelefone(request.telefone());
        entity.setTelefoneFixo(request.telefoneFixo());
        entity.setCelularWhatsapp(request.celularWhatsapp());
        if (request.preferenciaCanalComunicacao() != null) {
            entity.setPreferenciaCanalComunicacao(request.preferenciaCanalComunicacao());
        }

        if (request.endereco() != null) {
            entity.setEnderecoLogradouro(request.endereco().logradouro());
            entity.setEnderecoNumero(request.endereco().numero());
            entity.setEnderecoComplemento(request.endereco().complemento());
            entity.setEnderecoBairro(request.endereco().bairro());
            entity.setEnderecoCep(request.endereco().cep());
            entity.setEnderecoCidade(request.endereco().cidade());
            entity.setEnderecoEstado(request.endereco().estado());
        }

        entity.setEstadoCivil(request.estadoCivil());
        entity.setConjugeId(request.conjugeId());
        entity.setVocacao(request.vocacao());
        entity.setAtivo(true);

        // Vida Sacramental
        if (request.vidaSacramental() != null) {
            var vs = request.vidaSacramental();
            entity.setDataBatismo(vs.dataBatismo());
            entity.setParoquiaBatismo(vs.paroquiaBatismo());
            entity.setCelebranteBatismo(vs.celebranteBatismo());
            entity.setLivroBatismo(vs.livroBatismo());
            entity.setFolhaBatismo(vs.folhaBatismo());
            entity.setTermoBatismo(vs.termoBatismo());
            entity.setPadrinhosBatismo(vs.padrinhosBatismo());

            entity.setDataPrimeiraComunhao(vs.dataPrimeiraComunhao());
            entity.setParoquiaPrimeiraComunhao(vs.paroquiaPrimeiraComunhao());
            entity.setTurmaCatequese(vs.turmaCatequese());

            entity.setDataCrisma(vs.dataCrisma());
            entity.setParoquiaCrisma(vs.paroquiaCrisma());
            entity.setCelebranteCrisma(vs.celebranteCrisma());
            entity.setPadrinhoCrisma(vs.padrinhoCrisma());

            entity.setDataMatrimonio(vs.dataMatrimonio());
            entity.setParoquiaMatrimonio(vs.paroquiaMatrimonio());
            entity.setNumeroProcessoMatrimonial(vs.numeroProcessoMatrimonial());
            entity.setCertidaoMatrimonio(vs.certidaoMatrimonio());
        }

        entity.setHabilidadesVoluntariado(request.habilidadesVoluntariado());

        // Consentimento LGPD
        if (request.consentimentoLgpd() != null) {
            var c = request.consentimentoLgpd();
            entity.setTermoConsentimentoAceito(Boolean.TRUE.equals(c.termoConsentimentoAceito()));
            entity.setDataHoraConsentimentoUtc(
                    c.dataHoraConsentimentoUtc() != null ? c.dataHoraConsentimentoUtc() : Instant.now());
            entity.setIpConsentimento(c.ipConsentimento());
            if (c.versaoTermoConsentimento() != null) {
                entity.setVersaoTermoConsentimento(c.versaoTermoConsentimento());
            }
            entity.setUsoImagemAutorizado(Boolean.TRUE.equals(c.usoImagemAutorizado()));
            Boolean permitirDivulgacao = c.permitirDivulgacaoAniversario();
            entity.setPermitirDivulgacaoAniversario(
                    permitirDivulgacao == null || Boolean.TRUE.equals(permitirDivulgacao));
        }

        // Dependentes
        if (request.dependentes() != null && !request.dependentes().isEmpty()) {
            List<FielDependenteEntity> depEntities = new ArrayList<>();
            for (DependenteDto depDto : request.dependentes()) {
                FielDependenteEntity depEntity = new FielDependenteEntity();
                depEntity.setId(UUID.randomUUID());
                depEntity.setFiel(entity);
                depEntity.setNome(depDto.nome());
                depEntity.setDataNascimento(depDto.dataNascimento());
                depEntity.setParentesco(depDto.parentesco() != null ? depDto.parentesco() : Parentesco.FILHO);
                depEntity.setBatizado(Boolean.TRUE.equals(depDto.batizado()));
                depEntity.setPrimeiraComunhao(Boolean.TRUE.equals(depDto.primeiraComunhao()));
                depEntity.setCrismado(Boolean.TRUE.equals(depDto.crismado()));
                depEntities.add(depEntity);
            }
            entity.setDependentes(depEntities);
        }

        Instant now = Instant.now();
        entity.setCriadoEmUtc(now);
        entity.setAtualizadoEmUtc(now);
        entity.setCriadoPor(actor != null ? actor : "system");
        entity.setAtualizadoPor(actor != null ? actor : "system");

        FielEntity saved = fielRepository.save(entity);
        return fielMapper.toResponse(saved);
    }
}
