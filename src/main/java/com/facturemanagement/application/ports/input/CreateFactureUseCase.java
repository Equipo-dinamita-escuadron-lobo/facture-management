package com.facturemanagement.application.ports.input;

import com.facturemanagement.domain.model.Facture;

public interface CreateFactureUseCase {
    Facture createFacture(Facture facture);
}
