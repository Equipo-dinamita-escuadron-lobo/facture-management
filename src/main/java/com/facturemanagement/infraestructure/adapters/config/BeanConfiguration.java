package com.facturemanagement.infraestructure.adapters.config;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.facturemanagement.domain.service.CreateFactureService;
import com.facturemanagement.domain.service.GetFactureService;
import com.facturemanagement.domain.service.ListFacturesService;
import com.facturemanagement.infraestructure.adapters.output.eventpublisher.FactureEventPublisherAdapter;
import com.facturemanagement.infraestructure.adapters.output.persistence.FacturePersistenceAdapter;
import com.facturemanagement.infraestructure.adapters.output.persistence.mapper.FacturePersistenceMapper;
import com.facturemanagement.infraestructure.adapters.output.persistence.mapper.ProductPersistenceMapper;
import com.facturemanagement.infraestructure.adapters.output.persistence.repository.FactureRepository;
import com.facturemanagement.infraestructure.adapters.output.persistence.repository.ProductRepository;

@Configuration
public class BeanConfiguration{

    @Bean
    public FacturePersistenceAdapter facturePersistenceAdapter(FactureRepository factureRepository, FacturePersistenceMapper facturePersistenceMapper, ProductPersistenceMapper productPersistenceMapper) {
        return new FacturePersistenceAdapter(factureRepository, facturePersistenceMapper,productPersistenceMapper);
    }

    @Bean
    public FactureEventPublisherAdapter factureEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        return new FactureEventPublisherAdapter(applicationEventPublisher);
    }

    @Bean
    public CreateFactureService createFactureService(FacturePersistenceAdapter facturePersistenceAdapter, FactureEventPublisherAdapter factureEventPublisherAdapter) {
        return new CreateFactureService(facturePersistenceAdapter, factureEventPublisherAdapter);
    }

    @Bean GetFactureService getFactureService(FacturePersistenceAdapter facturePersistenceAdapter){
        return new GetFactureService(facturePersistenceAdapter);
    }

    @Bean ListFacturesService listFacturesService(FacturePersistenceAdapter facturePersistenceAdapter){
        return new ListFacturesService(facturePersistenceAdapter);
    }
}
