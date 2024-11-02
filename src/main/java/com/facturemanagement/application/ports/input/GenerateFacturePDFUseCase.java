package com.facturemanagement.application.ports.input;

import com.facturemanagement.domain.model.Facture;

public interface GenerateFacturePDFUseCase {
    byte[] generetePDFFacture(Facture facture);
    byte[] generateFactureQR(Facture facture);
}
