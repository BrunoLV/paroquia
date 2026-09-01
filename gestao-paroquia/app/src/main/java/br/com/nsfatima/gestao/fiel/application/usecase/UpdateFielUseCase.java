package br.com.nsfatima.gestao.fiel.application.usecase;

import br.com.nsfatima.gestao.fiel.api.v1.dto.DependenteDto;
import br.com.nsfatima.gestao.fiel.api.v1.dto.FielResponse;
import br.com.nsfatima.gestao.fiel.api.v1.dto.UpdateFielRequest;
import br.com.nsfatima.gestao.fiel.domain.model.Parentesco;
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
public class UpdateFielUseCase {

    private final FielJpaRepository fielRepository;
    private final FielMapper fielMapper;

    public UpdateFielUseCase(FielJpaRepository fielRepository, FielMapper fielMapper) {
        this.fielRepository = fielRepository;
        this.fielMapper = fielMapper;
    }

    @Transactional
    public FielResponse execute(UUID id, UpdateFielRequest request, String actor) {
        FielEntity entity = fielRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Fiel não encontrado: " + id));

        if (request.nome() != null) {
            entity.setNome(request.nome());
        }
        if (request.cpf() != null) {
            entity.setCpf(request.cpf());
        }
        if (request.rg() != null) {
            entity.setRg(request.rg());
        }
        if (request.dataNascimento() != null) {
            entity.setDataNascimento(request.dataNascimento());
        }
        if (request.sexo() != null) {
            entity.setSexo(request.sexo());
        }
        if (request.profissao() != null) {
            entity.setProfissao(request.profissao());
        }
        if (request.email() != null) {
            entity.setEmail(request.email());
        }
        if (request.telefone() != null) {
            entity.setTelefone(request.telefone());
        }
        if (request.telefoneFixo() != null) {
            entity.setTelefoneFixo(request.telefoneFixo());
        }
        if (request.celularWhatsapp() != null) {
            entity.setCelularWhatsapp(request.celularWhatsapp());
        }
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

        if (request.estadoCivil() != null) {
            entity.setEstadoCivil(request.estadoCivil());
        }
        if (request.conjugeId() != null) {
            entity.setConjugeId(request.conjugeId());
        }
        if (request.vocacao() != null) {
            entity.setVocacao(request.vocacao());
        }
        if (request.ativo() != null) {
            entity.setAtivo(request.ativo());
        }

        // Vida Sacramental
        if (request.vidaSacramental() != null) {
            var vs = request.vidaSacramental();
            if (vs.dataBatismo() != null) {
                entity.setDataBatismo(vs.dataBatismo());
            }
            if (vs.paroquiaBatismo() != null) {
                entity.setParoquiaBatismo(vs.paroquiaBatismo());
            }
            if (vs.celebranteBatismo() != null) {
                entity.setCelebranteBatismo(vs.celebranteBatismo());
            }
            if (vs.livroBatismo() != null) {
                entity.setLivroBatismo(vs.livroBatismo());
            }
            if (vs.folhaBatismo() != null) {
                entity.setFolhaBatismo(vs.folhaBatismo());
            }
            if (vs.termoBatismo() != null) {
                entity.setTermoBatismo(vs.termoBatismo());
            }
            if (vs.padrinhosBatismo() != null) {
                entity.setPadrinhosBatismo(vs.padrinhosBatismo());
            }

            if (vs.dataPrimeiraComunhao() != null) {
                entity.setDataPrimeiraComunhao(vs.dataPrimeiraComunhao());
            }
            if (vs.paroquiaPrimeiraComunhao() != null) {
                entity.setParoquiaPrimeiraComunhao(vs.paroquiaPrimeiraComunhao());
            }
            if (vs.turmaCatequese() != null) {
                entity.setTurmaCatequese(vs.turmaCatequese());
            }

            if (vs.dataCrisma() != null) {
                entity.setDataCrisma(vs.dataCrisma());
            }
            if (vs.paroquiaCrisma() != null) {
                entity.setParoquiaCrisma(vs.paroquiaCrisma());
            }
            if (vs.celebranteCrisma() != null) {
                entity.setCelebranteCrisma(vs.celebranteCrisma());
            }
            if (vs.padrinhoCrisma() != null) {
                entity.setPadrinhoCrisma(vs.padrinhoCrisma());
            }

            if (vs.dataMatrimonio() != null) {
                entity.setDataMatrimonio(vs.dataMatrimonio());
            }
            if (vs.paroquiaMatrimonio() != null) {
                entity.setParoquiaMatrimonio(vs.paroquiaMatrimonio());
            }
            if (vs.numeroProcessoMatrimonial() != null) {
                entity.setNumeroProcessoMatrimonial(vs.numeroProcessoMatrimonial());
            }
            if (vs.certidaoMatrimonio() != null) {
                entity.setCertidaoMatrimonio(vs.certidaoMatrimonio());
            }
        }

        if (request.habilidadesVoluntariado() != null) {
            entity.setHabilidadesVoluntariado(request.habilidadesVoluntariado());
        }

        // LGPD Consentimento
        if (request.consentimentoLgpd() != null) {
            var c = request.consentimentoLgpd();
            if (c.termoConsentimentoAceito() != null) {
                entity.setTermoConsentimentoAceito(c.termoConsentimentoAceito());
            }
            if (c.dataHoraConsentimentoUtc() != null) {
                entity.setDataHoraConsentimentoUtc(c.dataHoraConsentimentoUtc());
            }
            if (c.ipConsentimento() != null) {
                entity.setIpConsentimento(c.ipConsentimento());
            }
            if (c.versaoTermoConsentimento() != null) {
                entity.setVersaoTermoConsentimento(c.versaoTermoConsentimento());
            }
            if (c.usoImagemAutorizado() != null) {
                entity.setUsoImagemAutorizado(c.usoImagemAutorizado());
            }
            if (c.permitirDivulgacaoAniversario() != null) {
                entity.setPermitirDivulgacaoAniversario(c.permitirDivulgacaoAniversario());
            }
        }

        // Dependentes
        if (request.dependentes() != null) {
            entity.getDependentes().clear();
            List<FielDependenteEntity> newDeps = new ArrayList<>();
            for (DependenteDto depDto : request.dependentes()) {
                FielDependenteEntity depEntity = new FielDependenteEntity();
                depEntity.setId(depDto.id() != null ? depDto.id() : UUID.randomUUID());
                depEntity.setFiel(entity);
                depEntity.setNome(depDto.nome());
                depEntity.setDataNascimento(depDto.dataNascimento());
                depEntity.setParentesco(depDto.parentesco() != null ? depDto.parentesco() : Parentesco.FILHO);
                depEntity.setBatizado(Boolean.TRUE.equals(depDto.batizado()));
                depEntity.setPrimeiraComunhao(Boolean.TRUE.equals(depDto.primeiraComunhao()));
                depEntity.setCrismado(Boolean.TRUE.equals(depDto.crismado()));
                newDeps.add(depEntity);
            }
            entity.getDependentes().addAll(newDeps);
        }

        entity.setAtualizadoEmUtc(Instant.now());
        entity.setAtualizadoPor(actor != null ? actor : "system");

        FielEntity saved = fielRepository.save(entity);
        return fielMapper.toResponse(saved);
    }
}
