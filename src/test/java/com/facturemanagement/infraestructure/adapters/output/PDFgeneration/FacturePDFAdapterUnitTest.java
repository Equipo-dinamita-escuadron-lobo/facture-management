package com.facturemanagement.infraestructure.adapters.output.PDFgeneration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.Set;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import com.facturemanagement.domain.model.Facture;
import com.facturemanagement.domain.model.Product;
import com.facturemanagement.domain.model.eFactureType;
import com.facturemanagement.infraestructure.adapters.security.IJwtUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class FacturePDFAdapterUnitTest {

    private static final String BASE_URL = "http://gateway-svc:8080";
    private static final String ENTERPRISE_ID = "23420850-8547-44d4-9abf-04e9783ed400";
    private static final long THIRD_ID = 41L;

    @Test
    void shouldUseGatewayApiPathsAndGenerateSalePdf(@TempDir Path tempDir) throws Exception {
        HttpRequest request = mock(HttpRequest.class);
        IJwtUtils jwtUtils = mock(IJwtUtils.class);
        ObjectMapper objectMapper = new ObjectMapper();

        Path logoPath = tempDir.resolve("logo.png");
        ImageIO.write(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB), "png", logoPath.toFile());
        JsonNode enterprise = objectMapper.createObjectNode()
                .put("email", "empresa@example.com")
                .put("phone", "3000000000")
                .put("name", "Empresa PP8")
                .put("nit", "900123456")
                .put("logo", logoPath.toUri().toURL().toString());
        JsonNode third = objectMapper.readTree("""
                {
                  "verificationNumber": 7,
                  "typeId": { "typeId": "NIT" },
                  "idNumber": 901234567,
                  "names": "Cliente",
                  "lastNames": "PP8",
                  "address": "Calle 1",
                  "country": "Colombia",
                  "province": "Cauca",
                  "city": "Popayan",
                  "phoneNumber": "3100000000",
                  "email": "cliente@example.com",
                  "personType": "JURIDICA"
                }
                """);

        when(request.getRequest(anyString(), same(jwtUtils))).thenAnswer(invocation -> {
            String url = invocation.getArgument(0);
            return url.contains("/api/enterprises/") ? enterprise : third;
        });

        FacturePDFAdapter adapter = new FacturePDFAdapter();
        ReflectionTestUtils.setField(adapter, "baseUrl", BASE_URL);
        ReflectionTestUtils.setField(adapter, "request", request);
        ReflectionTestUtils.setField(adapter, "jwtUtils", jwtUtils);

        Product product = Product.builder()
                .productId(1L)
                .amount(1D)
                .description("Servicio")
                .descount(0D)
                .vat(0.19D)
                .unitPrice(1000D)
                .subtotal(1000D)
                .build();
        Facture facture = Facture.builder()
                .factId(10L)
                .entId(ENTERPRISE_ID)
                .thId(THIRD_ID)
                .factCode(100L)
                .factObservations("Prueba")
                .factureType(eFactureType.Venta)
                .factProducts(Set.of(product))
                .descounts(0D)
                .factSubtotals(1000D)
                .facSalesTax(190D)
                .facWithholdingSource(0D)
                .build();

        byte[] pdf = adapter.generatePDF(facture);

        assertThat(pdf).isNotNull().isNotEmpty();
        verify(request, atLeastOnce()).getRequest(
                BASE_URL + "/api/enterprises/enterprise/" + ENTERPRISE_ID,
                jwtUtils);
        verify(request, atLeastOnce()).getRequest(
                BASE_URL + "/api/thirds/third?thId=" + THIRD_ID,
                jwtUtils);
    }
}
