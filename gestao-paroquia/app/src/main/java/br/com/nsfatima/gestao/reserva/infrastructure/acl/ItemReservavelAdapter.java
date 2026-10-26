package br.com.nsfatima.gestao.reserva.infrastructure.acl;

import br.com.nsfatima.gestao.local.infrastructure.persistence.repository.LocalJpaRepository;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoPatrimonialId;
import br.com.nsfatima.gestao.patrimonio.domain.repository.AtivoPatrimonialRepository;
import br.com.nsfatima.gestao.reserva.application.port.out.ItemReservavelPort;
import br.com.nsfatima.gestao.reserva.domain.model.TipoItemReserva;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Anti-Corruption Layer: traduz locais e ativos patrimoniais para o conceito {@link ItemReservavel} do módulo de
 * Reservas e bloqueia a linha do item (ver {@link ItemReservavelPort}). O envio do ativo para manutenção
 * bloqueia a mesma linha, serializando as duas operações.
 */
@Component
public class ItemReservavelAdapter implements ItemReservavelPort {

    private final LocalJpaRepository localRepository;
    private final AtivoPatrimonialRepository ativoRepository;

    public ItemReservavelAdapter(LocalJpaRepository localRepository, AtivoPatrimonialRepository ativoRepository) {
        this.localRepository = localRepository;
        this.ativoRepository = ativoRepository;
    }

    @Override
    public Optional<ItemReservavel> bloquear(TipoItemReserva tipoItem, UUID itemId) {
        if (tipoItem == TipoItemReserva.LOCAL) {
            return localRepository.findByIdComBloqueio(itemId)
                    .map(l -> new ItemReservavel(l.getId(), l.getNome(), l.isAtivo(), true));
        }
        return ativoRepository.findByIdComBloqueio(new AtivoPatrimonialId(itemId))
                .map(a -> new ItemReservavel(a.getId().value(), a.getNome(), !a.estaExcluido(), a.isReservavel()));
    }
}
