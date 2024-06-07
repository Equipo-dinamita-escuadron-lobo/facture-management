package com.facturemanagement.application.ports.input;

import com.facturemanagement.domain.model.Facture;

public interface GetFactureUseCase {
    Facture getFactureBy(Long factId);
}
