package com.facturemanagement.application.ports.output;

import com.facturemanagement.domain.model.Facture;

public interface FactureGeneratePDFOutputPort {
    byte[] generatePDF(Facture facture);
    byte[] generateQR(Facture facture);
}
