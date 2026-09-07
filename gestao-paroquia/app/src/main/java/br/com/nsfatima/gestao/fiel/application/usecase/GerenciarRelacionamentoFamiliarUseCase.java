package br.com.nsfatima.gestao.fiel.application.usecase;
import br.com.nsfatima.gestao.fiel.api.v1.dto.RelacionamentoFamiliarResponse;
import br.com.nsfatima.gestao.fiel.domain.model.*;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.RelacionamentoFamiliarEntity;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.*;
import br.com.nsfatima.gestao.fiel.infrastructure.observability.FielAuditPublisher;
import java.time.Instant;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service public class GerenciarRelacionamentoFamiliarUseCase {
 private final FielJpaRepository fieis; private final RelacionamentoFamiliarJpaRepository relacoes; private final FielAuditPublisher auditoria;
 public GerenciarRelacionamentoFamiliarUseCase(FielJpaRepository f,RelacionamentoFamiliarJpaRepository r,FielAuditPublisher p){fieis=f;relacoes=r;auditoria=p;}
 @Transactional public UUID criar(UUID origemId,UUID destinoId,TipoRelacionamentoFamiliar tipo,UUID atorId){if(origemId.equals(destinoId))throw new IllegalArgumentException("Um fiel não pode relacionar-se consigo mesmo.");var origem=fieis.findById(origemId).orElseThrow(()->new IllegalArgumentException("Fiel de origem não encontrado."));var destino=fieis.findById(destinoId).orElseThrow(()->new IllegalArgumentException("Fiel de destino não encontrado."));validarSexo(origem.getSexo(),destino.getSexo());if(tipo==TipoRelacionamentoFamiliar.CONJUGE&&origem.getSexo()==destino.getSexo())throw new IllegalArgumentException("Cônjuges devem ter sexos distintos.");UUID par=UUID.randomUUID();salvar(par,origemId,destinoId,tipo);salvar(par,destinoId,origemId,inversa(tipo));auditoria.publicarCriacaoRelacionamento(par,origemId,atorId,origem.getUnidadeTerritorialId(),tipo.name());return par;}
 @Transactional(readOnly=true) public List<RelacionamentoFamiliarResponse> listar(UUID fielId){return relacoes.findByFielOrigemIdAndDeletedAtIsNull(fielId).stream().map(r->{var destino=fieis.findById(r.getFielDestinoId()).orElseThrow();return new RelacionamentoFamiliarResponse(r.getId(),destino.getId(),destino.getNome(),rotulo(r.getTipo(),destino.getSexo()));}).toList();}
 @Transactional public void remover(UUID relacionamentoId,UUID atorId){var relacao=relacoes.findById(relacionamentoId).orElseThrow(()->new IllegalArgumentException("Relacionamento não encontrado."));var fiel=fieis.findById(relacao.getFielOrigemId()).orElseThrow(()->new IllegalArgumentException("Fiel não encontrado."));Instant agora=Instant.now();relacoes.findByParIdAndDeletedAtIsNull(relacao.getParId()).forEach(parte->{parte.setDeletedAt(agora);relacoes.save(parte);});auditoria.publicarInativacaoRelacionamento(relacao.getParId(),fiel.getId(),atorId,fiel.getUnidadeTerritorialId());}
 private void salvar(UUID par,UUID origem,UUID destino,TipoRelacionamentoFamiliar tipo){var r=new RelacionamentoFamiliarEntity();r.setId(UUID.randomUUID());r.setParId(par);r.setFielOrigemId(origem);r.setFielDestinoId(destino);r.setTipo(tipo);r.setCriadoEmUtc(Instant.now());relacoes.save(r);}
 private TipoRelacionamentoFamiliar inversa(TipoRelacionamentoFamiliar tipo){return switch(tipo){case GENITOR->TipoRelacionamentoFamiliar.FILHO;case FILHO->TipoRelacionamentoFamiliar.GENITOR;default->tipo;};}
 private void validarSexo(Sexo a,Sexo b){if(a==Sexo.NAO_INFORMADO||b==Sexo.NAO_INFORMADO)throw new IllegalStateException("Os dois fiéis precisam ter sexo M ou F.");}
 private String rotulo(TipoRelacionamentoFamiliar tipo,Sexo sexo){return switch(tipo){case CONJUGE->sexo==Sexo.MASCULINO?"Marido":"Esposa";case GENITOR->sexo==Sexo.MASCULINO?"Pai":"Mãe";case FILHO->sexo==Sexo.MASCULINO?"Filho":"Filha";case IRMAO->sexo==Sexo.MASCULINO?"Irmão":"Irmã";};}
}
