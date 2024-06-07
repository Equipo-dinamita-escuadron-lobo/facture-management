package com.facturemanagement.application.ports.output;

import com.facturemanagement.domain.model.Facture;

public interface FactureCreatedOutputPort {
    Facture saveFacture(Facture facture);
}
