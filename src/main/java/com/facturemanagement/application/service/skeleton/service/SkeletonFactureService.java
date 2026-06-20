package com.facturemanagement.application.service.skeleton.service;

import com.facturemanagement.application.service.skeleton.audit.annotation.DocumentAuditable;
import com.facturemanagement.application.service.skeleton.audit.annotation.DocumentOperationType;
import com.facturemanagement.application.service.skeleton.dto.*;
import com.facturemanagement.application.service.skeleton.mapper.SkeletonFactureMapper;
import com.facturemanagement.application.service.skeleton.model.SkeletonFacture;
import com.facturemanagement.application.service.skeleton.model.SkeletonProduct;
import com.facturemanagement.application.service.skeleton.model.SkeletonReturn;
import com.facturemanagement.application.service.skeleton.repository.SkeletonFactureRepository;
import com.facturemanagement.application.service.skeleton.repository.SkeletonReturnRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SkeletonFactureService {

    private final SkeletonFactureRepository factureRepository;
    private final SkeletonReturnRepository returnRepository;
    private final SkeletonFactureMapper mapper;

    @Transactional
    @DocumentAuditable(operationType = DocumentOperationType.CREATE, moduleName = "INVOICES")
    public SkeletonFactureDetailDto createFacture(SkeletonFactureRequestDto request) {
        log.info("Creando factura con código: {}", request.getFactCode());

        if (factureRepository.existsByFactCode(request.getFactCode())) {
            throw new IllegalArgumentException("Ya existe una factura con el código: " + request.getFactCode());
        }

        SkeletonFacture facture = mapper.toEntity(request);
        SkeletonFacture savedFacture = factureRepository.save(facture);

        log.info("Factura creada exitosamente con ID: {}", savedFacture.getId());
        return mapper.toDetailDto(savedFacture);
    }

    @Transactional(readOnly = true)
    public List<SkeletonFactureSummaryDto> getAllFactures() {
        return factureRepository.findAll().stream()
                .map(mapper::toSummaryDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SkeletonFactureDetailDto getFactureById(Long id) {
        SkeletonFacture facture = factureRepository.findByIdWithProducts(id)
                .orElseThrow(() -> new IllegalArgumentException("Factura no encontrada con ID: " + id));
        return mapper.toDetailDto(facture);
    }

    @Transactional(readOnly = true)
    public SkeletonFactureDetailDto getFactureByFactCode(Long factCode) {
        SkeletonFacture facture = factureRepository.findByFactCodeWithProducts(factCode)
                .orElseThrow(() -> new IllegalArgumentException("Factura no encontrada con código: " + factCode));
        return mapper.toDetailDto(facture);
    }

    @Transactional
    @DocumentAuditable(operationType = DocumentOperationType.CREATE, moduleName = "INVOICES")
    public void processReturn(ReturnRequestDto returnRequest, SkeletonReturn.ReturnType returnType) {
        log.info("Procesando devolución para factura: {}", returnRequest.getFactCode());

        // Validar que la factura existe
        SkeletonFacture facture = factureRepository.findByFactCodeWithProducts(returnRequest.getFactCode())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró la factura con código: " + returnRequest.getFactCode()));

        // Procesar cada producto a devolver
        for (ReturnRequestDto.ProductReturnDto productReturn : returnRequest.getProducts()) {
            validateAndProcessProductReturn(facture, productReturn, returnType);
        }

        log.info("Devolución procesada exitosamente");
    }

    private void validateAndProcessProductReturn(
            SkeletonFacture facture,
            ReturnRequestDto.ProductReturnDto productReturn,
            SkeletonReturn.ReturnType returnType) {

        // Buscar el producto en la factura
        SkeletonProduct product = facture.getProducts().stream()
                .filter(p -> p.getProductId().equals(productReturn.getProductId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "El producto " + productReturn.getProductId() + " no existe en la factura "
                                + facture.getFactCode()));

        // Obtener la cantidad total devuelta previamente
        Integer totalReturned = returnRepository.getTotalReturnedQuantity(
                facture.getFactCode(),
                productReturn.getProductId());

        if (totalReturned == null) {
            totalReturned = 0;
        }

        // Validar que no se exceda la cantidad original
        int newTotal = totalReturned + productReturn.getQuantity();
        if (newTotal > product.getAmount()) {
            throw new IllegalArgumentException(
                    String.format("No se puede devolver %d unidades del producto %d. " +
                            "Cantidad original: %d, Ya devueltos: %d, Disponible para devolver: %d",
                            productReturn.getQuantity(),
                            productReturn.getProductId(),
                            product.getAmount(),
                            totalReturned,
                            product.getAmount() - totalReturned));
        }

        // Guardar el registro de devolución
        SkeletonReturn returnRecord = SkeletonReturn.builder()
                .originalFactCode(facture.getFactCode())
                .productId(productReturn.getProductId())
                .returnedQuantity(productReturn.getQuantity())
                .returnType(returnType)
                .details(productReturn.getReason())
                .build();

        returnRepository.save(returnRecord);

        log.info("Registro de devolución guardado - Factura: {}, Producto: {}, Cantidad: {}",
                facture.getFactCode(), productReturn.getProductId(), productReturn.getQuantity());
    }

    @Transactional(readOnly = true)
    public List<SkeletonReturn> getReturnsByFactCode(Long factCode) {
        return returnRepository.findByOriginalFactCode(factCode);
    }

    @Transactional(readOnly = true)
    public Integer getTotalReturnedQuantity(Long factCode, Long productId) {
        Integer total = returnRepository.getTotalReturnedQuantity(factCode, productId);
        return total != null ? total : 0;
    }
}
