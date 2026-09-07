package br.com.nsfatima.gestao.fiel.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import br.com.nsfatima.gestao.clero.domain.model.TipoPessoaFamiliar;
import br.com.nsfatima.gestao.clero.infrastructure.persistence.entity.ClerigoEntity;
import br.com.nsfatima.gestao.clero.infrastructure.persistence.repository.ClerigoJpaRepository;
import br.com.nsfatima.gestao.fiel.domain.model.Sexo;
import br.com.nsfatima.gestao.fiel.domain.model.TipoRelacionamentoFamiliar;
import br.com.nsfatima.gestao.fiel.infrastructure.observability.FielAuditPublisher;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielEntity;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.RelacionamentoFamiliarJpaRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GerenciarRelacionamentoFamiliarUseCaseTest {

    @Mock private FielJpaRepository fieis;
    @Mock private ClerigoJpaRepository clerigos;
    @Mock private RelacionamentoFamiliarJpaRepository relacionamentos;
    @Mock private FielAuditPublisher auditoria;
    @Captor private ArgumentCaptor<br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.RelacionamentoFamiliarEntity> relacaoCaptor;

    @Test
    void deveCriarVinculoReciprocoEntreFielEClerigo() {
        UUID fielId = UUID.randomUUID();
        UUID clerigoId = UUID.randomUUID();
        UUID atorId = UUID.randomUUID();
        FielEntity fiel = fiel(fielId, Sexo.FEMININO);
        ClerigoEntity clerigo = clerigo(clerigoId);
        when(fieis.findById(fielId)).thenReturn(Optional.of(fiel));
        when(clerigos.findById(clerigoId)).thenReturn(Optional.of(clerigo));

        GerenciarRelacionamentoFamiliarUseCase useCase = new GerenciarRelacionamentoFamiliarUseCase(
                fieis, clerigos, relacionamentos, auditoria);

        useCase.criar(fielId, TipoPessoaFamiliar.FIEL, clerigoId, TipoPessoaFamiliar.CLERIGO,
                TipoRelacionamentoFamiliar.CONJUGE, atorId);

        org.mockito.Mockito.verify(relacionamentos, org.mockito.Mockito.times(2)).save(relacaoCaptor.capture());
        assertThat(relacaoCaptor.getAllValues())
                .extracting(relacao -> relacao.getTipoPessoaDestino())
                .containsExactly(TipoPessoaFamiliar.CLERIGO, TipoPessoaFamiliar.FIEL);
        assertThat(relacaoCaptor.getAllValues())
                .extracting(relacao -> relacao.getTipo())
                .containsExactly(TipoRelacionamentoFamiliar.CONJUGE, TipoRelacionamentoFamiliar.CONJUGE);
    }

    private FielEntity fiel(UUID id, Sexo sexo) {
        FielEntity fiel = new FielEntity();
        fiel.setId(id);
        fiel.setSexo(sexo);
        fiel.setUnidadeTerritorialId(UUID.randomUUID());
        return fiel;
    }

    private ClerigoEntity clerigo(UUID id) {
        ClerigoEntity clerigo = new ClerigoEntity();
        clerigo.setId(id);
        clerigo.setAtivo(true);
        return clerigo;
    }
}
