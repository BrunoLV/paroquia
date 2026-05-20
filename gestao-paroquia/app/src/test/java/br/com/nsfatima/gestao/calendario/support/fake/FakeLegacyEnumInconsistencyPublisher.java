package br.com.nsfatima.gestao.calendario.support.fake;

import br.com.nsfatima.gestao.calendario.infrastructure.observability.LegacyEnumInconsistencyPublisher;

public class FakeLegacyEnumInconsistencyPublisher extends LegacyEnumInconsistencyPublisher {
    public FakeLegacyEnumInconsistencyPublisher() {
        super(null);
    }

    @Override
    public void publish(String aggregateType, String aggregateId, String field, String rawValue) {
        // No-op for fake
    }
}
