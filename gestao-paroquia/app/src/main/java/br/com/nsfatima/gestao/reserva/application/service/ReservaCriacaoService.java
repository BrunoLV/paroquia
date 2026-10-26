package br.com.nsfatima.gestao.reserva.application.service;

import br.com.nsfatima.gestao.reserva.application.event.PublicadorReservaDeAtivo;
import br.com.nsfatima.gestao.reserva.application.port.out.ConsultaManutencaoAtivoPort;
import br.com.nsfatima.gestao.reserva.application.port.out.ConsultaManutencaoAtivoPort.JanelaIndisponivel;
import br.com.nsfatima.gestao.reserva.application.port.out.ItemReservavelPort;
import br.com.nsfatima.gestao.reserva.application.port.out.ItemReservavelPort.ItemReservavel;
import br.com.nsfatima.gestao.reserva.domain.exception.ReservaConflitoException;
import br.com.nsfatima.gestao.reserva.domain.exception.ReservaConflitoException.ConflitoDetalhe;
import br.com.nsfatima.gestao.reserva.domain.exception.ReservaOperacaoInvalidaException;
import br.com.nsfatima.gestao.reserva.domain.model.IntervaloReserva;
import br.com.nsfatima.gestao.reserva.domain.model.Reserva;
import br.com.nsfatima.gestao.reserva.domain.model.ReservaId;
import br.com.nsfatima.gestao.reserva.domain.model.StatusReserva;
import br.com.nsfatima.gestao.reserva.domain.model.TipoItemReserva;
import br.com.nsfatima.gestao.reserva.domain.repository.ReservaRepository;
import br.com.nsfatima.gestao.reserva.domain.service.ReservaConflitoService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Caminho único de criação de reservas, compartilhado pelo pedido avulso, pela série recorrente e pela integração
 * com eventos. Concentra o que toda reserva precisa para nascer válida, qualquer que seja a porta de entrada:
 * item existente, em uso e elegível a reserva; ausência de choque de agenda (RN-RES-02); ausência de manutenção
 * prevista para o ativo (RN-PAT-04); e notificação do patrimônio para recalcular o status {@code RESERVADO}.
 *
 * <p>Concorrência: o primeiro passo é sempre bloquear a linha do item ({@link ItemReservavelPort}), o que
 * serializa pedidos simultâneos do mesmo item e também o envio do ativo para manutenção; só então os conflitos
 * são consultados. Por isso o serviço deve ser chamado dentro de uma transação do chamador, e quem reserva vários
 * itens os bloqueia em ordem determinística ({@link #criarTodas}) para não provocar impasse entre eventos.</p>
 *
 * <p>O serviço não abre transação própria: os chamadores que absorvem conflitos (série recorrente) usam
 * {@link #conflitosDeAgenda} e {@link #janelaDeManutencao} em vez de capturar exceções, o que evitaria marcar a
 * transação como somente-rollback.</p>
 */
@Service
public class ReservaCriacaoService {

    private static final Comparator<ItemRef> ORDEM_DE_BLOQUEIO =
            Comparator.comparing(ItemRef::tipoItem).thenComparing(ItemRef::itemId);

    private final ReservaRepository reservaRepository;
    private final ReservaConflitoService conflitoService;
    private final ConsultaManutencaoAtivoPort consultaManutencaoPort;
    private final ItemReservavelPort itemReservavelPort;
    private final PublicadorReservaDeAtivo publicadorReservaDeAtivo;

    public ReservaCriacaoService(
            ReservaRepository reservaRepository,
            ReservaConflitoService conflitoService,
            ConsultaManutencaoAtivoPort consultaManutencaoPort,
            ItemReservavelPort itemReservavelPort,
            PublicadorReservaDeAtivo publicadorReservaDeAtivo
    ) {
        this.reservaRepository = Objects.requireNonNull(reservaRepository, "ReservaRepository é obrigatório.");
        this.conflitoService = Objects.requireNonNull(conflitoService, "ReservaConflitoService é obrigatório.");
        this.consultaManutencaoPort = Objects.requireNonNull(consultaManutencaoPort,
                "ConsultaManutencaoAtivoPort é obrigatório.");
        this.itemReservavelPort = Objects.requireNonNull(itemReservavelPort, "ItemReservavelPort é obrigatório.");
        this.publicadorReservaDeAtivo = Objects.requireNonNull(publicadorReservaDeAtivo,
                "PublicadorReservaDeAtivo é obrigatório.");
    }

    /** Referência a um item reservável, usada para ordenar o bloqueio. */
    public record ItemRef(TipoItemReserva tipoItem, UUID itemId) {
    }

    /**
     * Pedido de reserva já resolvido pelo chamador: quem solicita, em nome de quem, com qual status inicial.
     * A alçada (status inicial) é decisão do chamador; este serviço não autoriza.
     */
    public record PedidoReserva(
            TipoItemReserva tipoItem,
            UUID itemId,
            String itemNome,
            IntervaloReserva intervalo,
            String descricao,
            UUID solicitanteId,
            UUID pastoralMovimentoId,
            UUID eventoId,
            UUID grupoRecorrenciaId,
            StatusReserva statusInicial
    ) {
        public PedidoReserva {
            Objects.requireNonNull(tipoItem, "Tipo do item é obrigatório.");
            Objects.requireNonNull(itemId, "Identificador do item é obrigatório.");
            Objects.requireNonNull(intervalo, "Intervalo da reserva é obrigatório.");
            Objects.requireNonNull(statusInicial, "Status inicial da reserva é obrigatório.");
            if (solicitanteId == null) {
                throw new ReservaOperacaoInvalidaException("A reserva exige um solicitante identificado.");
            }
            if (pastoralMovimentoId == null) {
                throw new ReservaOperacaoInvalidaException(
                        "A reserva exige a pastoral ou movimento em nome de quem se reserva.");
            }
        }
    }

    /**
     * Cria a reserva após validar item, agenda e manutenção.
     *
     * @throws ReservaConflitoException         se o horário colidir com outra reserva
     * @throws ReservaOperacaoInvalidaException se o item não puder ser reservado ou o ativo estiver em manutenção
     */
    public Reserva criar(PedidoReserva pedido) {
        validar(pedido);
        return registrar(pedido);
    }

    /**
     * Cria todas as reservas ou nenhuma. Os itens são bloqueados em ordem determinística e todos os pedidos são
     * validados antes de qualquer gravação; pedidos do mesmo evento não conflitam entre si.
     */
    public List<Reserva> criarTodas(List<PedidoReserva> pedidos) {
        bloquearItens(pedidos.stream().map(p -> new ItemRef(p.tipoItem(), p.itemId())).toList());
        pedidos.forEach(this::validar);
        return registrarTodas(pedidos);
    }

    /** Valida item, agenda e manutenção, lançando a exceção de domínio correspondente. */
    public void validar(PedidoReserva pedido) {
        List<ConflitoDetalhe> conflitos = conflitosDeAgenda(pedido);
        if (!conflitos.isEmpty()) {
            throw new ReservaConflitoException(
                    "Identificado conflito de agenda ou período de preparação e desocupação para o recurso.",
                    conflitos);
        }
        if (janelaDeManutencao(pedido).isPresent()) {
            throw new ReservaOperacaoInvalidaException(
                    "O ativo está em manutenção (ou com manutenção prevista) no período solicitado.");
        }
    }

    /**
     * Bloqueia e valida o item e devolve os choques de agenda com reservas ativas, sem lançar exceção de conflito.
     */
    public List<ConflitoDetalhe> conflitosDeAgenda(PedidoReserva pedido) {
        ItemReservavel item = prepararItem(pedido.tipoItem(), pedido.itemId());
        String nome = pedido.itemNome() != null ? pedido.itemNome() : item.nome();
        return conflitoService.detectarConflitos(
                pedido.tipoItem(), pedido.itemId(), nome, pedido.intervalo(), null, pedido.eventoId());
    }

    /** Janela de manutenção que bloqueia o ativo no período do pedido, se houver. Locais nunca a têm. */
    public Optional<ConflitoDetalhe> janelaDeManutencao(PedidoReserva pedido) {
        if (pedido.tipoItem() != TipoItemReserva.ATIVO) {
            return Optional.empty();
        }
        return consultaManutencaoPort.janelasDeManutencao(
                        pedido.itemId(), pedido.intervalo().dataHoraInicio(), pedido.intervalo().dataHoraFim())
                .stream()
                .findFirst()
                .map(janela -> detalheDeManutencao(pedido, janela));
    }

    /** Grava a reserva de um pedido já validado e notifica o patrimônio. */
    public Reserva registrar(PedidoReserva pedido) {
        return registrarTodas(List.of(pedido)).getFirst();
    }

    /** Grava as reservas de pedidos já validados e notifica o patrimônio uma vez por ativo confirmado. */
    public List<Reserva> registrarTodas(List<PedidoReserva> pedidos) {
        if (pedidos.isEmpty()) {
            return List.of();
        }
        Instant agora = Instant.now();
        List<Reserva> novas = pedidos.stream().map(p -> Reserva.criar(
                ReservaId.gerar(), p.tipoItem(), p.itemId(), p.intervalo(), p.descricao(), p.solicitanteId(),
                p.pastoralMovimentoId(), p.eventoId(), p.grupoRecorrenciaId(), p.statusInicial(), agora)).toList();
        List<Reserva> salvas = reservaRepository.saveAll(novas);
        publicarAtivosConfirmados(salvas);
        return salvas;
    }

    /**
     * Move uma reserva existente para outro intervalo, com as mesmas garantias de uma criação (a própria reserva
     * e as demais do seu evento não contam como choque).
     */
    public Reserva reagendar(Reserva reserva, IntervaloReserva novoIntervalo, Instant agora) {
        ItemReservavel item = prepararItem(reserva.getTipoItem(), reserva.getItemId());
        conflitoService.validarDisponibilidadeComLock(reserva.getTipoItem(), reserva.getItemId(), item.nome(),
                novoIntervalo, reserva.getId().value(), reserva.getEventoId());
        PedidoReserva comoPedido = new PedidoReserva(reserva.getTipoItem(), reserva.getItemId(), item.nome(),
                novoIntervalo, reserva.getDescricao(), reserva.getSolicitanteId(), reserva.getPastoralMovimentoId(),
                reserva.getEventoId(), reserva.getGrupoRecorrenciaId(), reserva.getStatus());
        if (janelaDeManutencao(comoPedido).isPresent()) {
            throw new ReservaOperacaoInvalidaException(
                    "O ativo está em manutenção (ou com manutenção prevista) no período solicitado.");
        }
        reserva.atualizarIntervalo(novoIntervalo, agora);
        Reserva salva = reservaRepository.save(reserva);
        publicadorReservaDeAtivo.publicarSeForAtivo(salva);
        return salva;
    }

    /** Bloqueia os itens em ordem determinística (tipo, id), evitando impasse entre transações que os compartilham. */
    public void bloquearItens(Collection<ItemRef> itens) {
        Set<ItemRef> unicos = new LinkedHashSet<>(itens);
        unicos.stream().sorted(ORDEM_DE_BLOQUEIO).forEach(i -> prepararItem(i.tipoItem(), i.itemId()));
    }

    private ItemReservavel prepararItem(TipoItemReserva tipoItem, UUID itemId) {
        ItemReservavel item = itemReservavelPort.bloquear(tipoItem, itemId)
                .orElseThrow(() -> new ReservaOperacaoInvalidaException(
                        "O item a ser reservado não foi encontrado: " + tipoItem + " " + itemId + "."));
        if (!item.ativo()) {
            throw new ReservaOperacaoInvalidaException("O item '" + item.nome() + "' não está disponível para uso.");
        }
        if (!item.reservavel()) {
            throw new ReservaOperacaoInvalidaException("O item '" + item.nome() + "' não é elegível a reserva.");
        }
        return item;
    }

    private void publicarAtivosConfirmados(Collection<Reserva> reservas) {
        Set<UUID> ativos = new LinkedHashSet<>();
        for (Reserva r : reservas) {
            if (r.getTipoItem() == TipoItemReserva.ATIVO && r.getStatus() == StatusReserva.CONFIRMADA) {
                ativos.add(r.getItemId());
            }
        }
        ativos.forEach(publicadorReservaDeAtivo::publicarParaAtivo);
    }

    private ConflitoDetalhe detalheDeManutencao(PedidoReserva pedido, JanelaIndisponivel janela) {
        return new ConflitoDetalhe(
                pedido.itemId(),
                pedido.itemNome() != null ? pedido.itemNome() : "Ativo " + pedido.itemId(),
                pedido.tipoItem(),
                janela.inicio(),
                janela.fim(),
                pedido.solicitanteId(),
                "Patrimônio",
                "Ativo em manutenção (ou com manutenção prevista) no período."
        );
    }
}
