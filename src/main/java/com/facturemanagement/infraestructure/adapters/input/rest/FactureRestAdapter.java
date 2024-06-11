package com.facturemanagement.infraestructure.adapters.input.rest;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import com.facturemanagement.application.ports.input.CreateFactureUseCase;
import com.facturemanagement.application.ports.input.GetFactureUseCase;
import com.facturemanagement.application.ports.input.ListFactureUseCase;
import com.facturemanagement.domain.model.Facture;
import com.facturemanagement.infraestructure.adapters.input.rest.data.request.FactureCreateRequest;
import com.facturemanagement.infraestructure.adapters.input.rest.data.request.FactureListRequest;
import com.facturemanagement.infraestructure.adapters.input.rest.data.response.FactureCreateResponse;
import com.facturemanagement.infraestructure.adapters.input.rest.data.response.FactureGetResponse;
import com.facturemanagement.infraestructure.adapters.input.rest.data.response.FactureListResponse;
import com.facturemanagement.infraestructure.adapters.input.rest.mapper.FactureRestMapper;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;



@CrossOrigin("*")
@RestController
@RequestMapping("/api/factures/")
@RequiredArgsConstructor
public class FactureRestAdapter {
    private final CreateFactureUseCase createFactureUseCase;
    private final GetFactureUseCase getFactureUseCase;
    private final ListFactureUseCase listFactureUseCase;

    private final FactureRestMapper factureRestMapper;

    @PostMapping("/")
    public ResponseEntity<FactureCreateResponse> createFacture(@RequestBody @Valid FactureCreateRequest factureCreateRequest) {
        System.out.println("\nEntrando a petición crear factura\n");

        Facture facture = this.factureRestMapper.toFacture(factureCreateRequest);
        
        facture = this.createFactureUseCase.createFacture(facture);

        //TO DO:
        //IMPLEMENTAR LA GENERACIÓN DEL PDF DE LA FACTURA

        return new ResponseEntity<>(this.factureRestMapper.toFactureCreateResponse(200, "CREATED"), HttpStatus.OK);
    }

    @GetMapping("/")
    public ResponseEntity<FactureGetResponse> getFactureBy(@RequestParam @Valid long factId) {
        System.out.println("\nEntrando a petición obtener una factura por su Id\n");

        Facture facture = this.getFactureUseCase.getFactureBy(factId);

        return new ResponseEntity<>(this.factureRestMapper.toGetFactureResponse(facture), HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity<FactureListResponse> getAllFacturesBy(@RequestBody @Valid FactureListRequest request) {
        System.out.println("\nEntrando a petición obtener todas las facturas de una empresa\n");

        Pageable page = PageRequest.of(request.getNumPage(), 10);

        Page<Facture> pageFactures = this.listFactureUseCase.getAllFacturesBy(request.getEntId(), page);  

        return new ResponseEntity<>(this.factureRestMapper.toFactureListResponse(pageFactures),HttpStatus.OK);
    }
    
    @GetMapping("/sales")
    public ResponseEntity<FactureListResponse> getAllSalesFacturesBy(@RequestBody @Valid FactureListRequest request) {
        System.out.println("\nEntrando a petición obtener todas las facturas de ventas de una empresa\n");

        Pageable page = PageRequest.of(request.getNumPage(), 10);

        Page<Facture> pageFactures = this.listFactureUseCase.getAllSalesFacturesBy(request.getEntId(), page);  

        return new ResponseEntity<>(this.factureRestMapper.toFactureListResponse(pageFactures),HttpStatus.OK);
    }
    
    @GetMapping("/shopping")
    public ResponseEntity<FactureListResponse> getAllShoppingFacturesBy(@RequestBody @Valid FactureListRequest request) {
        System.out.println("\nEntrando a petición obtener todas las facturas de compras de una empresa\n");

        Pageable page = PageRequest.of(request.getNumPage(), 10);

        Page<Facture> pageFactures = this.listFactureUseCase.getAllShoppingFacturesBy(request.getEntId(), page);  

        return new ResponseEntity<>(this.factureRestMapper.toFactureListResponse(pageFactures),HttpStatus.OK);
    }
}
