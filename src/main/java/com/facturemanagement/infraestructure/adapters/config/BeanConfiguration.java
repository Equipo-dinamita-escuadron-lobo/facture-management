package com.facturemanagement.infraestructure.adapters.config;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.facturemanagement.application.service.CreateFactureService;
import com.facturemanagement.application.service.GeneratedPDFFactureService;
import com.facturemanagement.application.service.GetFactureService;
import com.facturemanagement.application.service.ListFacturesService;
import com.facturemanagement.infraestructure.adapters.output.PDFgeneration.FacturePDFAdapter;
import com.facturemanagement.infraestructure.adapters.output.eventpublisher.FactureEventPublisherAdapter;
import com.facturemanagement.infraestructure.adapters.output.persistence.FacturePersistenceAdapter;
import com.facturemanagement.infraestructure.adapters.output.persistence.mapper.FacturePersistenceMapper;
import com.facturemanagement.infraestructure.adapters.output.persistence.repository.FactureRepository;
import com.facturemanagement.infraestructure.adapters.output.persistence.repository.ProductRepository;

@Configuration
public class BeanConfiguration {

    /**
     * Bean para crear un FacturePersistenceAdapter, que es responsable de
     * manejar las operaciones con la entidad Factura en la base de datos.
     * 
     * @param factureRepository        el repositorio para la entidad Factura.
     * @param productRepository        el repositorio para la entidad Producto.
     * @param facturePersistenceMapper el mapeador para mapear una Factura a una
     *                                 FacturaEntity
     *                                 y viceversa.
     * @return el FacturePersistenceAdapter a utilizar en la aplicación.
     */
    @Bean
    public FacturePersistenceAdapter facturePersistenceAdapter(FactureRepository factureRepository,
            ProductRepository productRepository, FacturePersistenceMapper facturePersistenceMapper) {
        return new FacturePersistenceAdapter(factureRepository, productRepository, facturePersistenceMapper);
    }

    /**
     * Bean para crear un FactureEventPublisherAdapter, que es responsable de
     * publicar eventos de dominio relacionados con la creaci n de facturas.
     * 
     * @param applicationEventPublisher el publicador de eventos para la
     *                                  aplicaci n.
     * @return el FactureEventPublisherAdapter a utilizar en la aplicaci n.
     */
    @Bean
    public FactureEventPublisherAdapter factureEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        return new FactureEventPublisherAdapter(applicationEventPublisher);
    }

    /**
     * Bean para crear un servicio que se encarga de crear una factura nueva en la
     * base de datos y publicar un evento de dominio para notificar a los
     * interesados.
     * 
     * @param facturePersistenceAdapter    el adaptador para persistir la factura en
     *                                     la base de datos.
     * @param factureEventPublisherAdapter el adaptador para publicar eventos de
     *                                     dominio.
     * @return el servicio para crear una factura.
     */
    @Bean
    public CreateFactureService createFactureService(FacturePersistenceAdapter facturePersistenceAdapter,
            FactureEventPublisherAdapter factureEventPublisherAdapter) {
        return new CreateFactureService(facturePersistenceAdapter, factureEventPublisherAdapter);
    }

    /**
     * Bean para crear un servicio que se encarga de generar un PDF para una factura
     * y
     * publicar un evento de dominio para notificar a los interesados.
     * 
     * @param facturePDFAdapter            el adaptador para generar el PDF de la
     *                                     factura.
     * @param factureEventPublisherAdapter el adaptador para publicar eventos de
     *                                     dominio.
     * @return el servicio para generar el PDF de la factura.
     */
    @Bean
    public GeneratedPDFFactureService generatedPDFFactureService(FacturePDFAdapter facturePDFAdapter,
            FactureEventPublisherAdapter factureEventPublisherAdapter) {
        return new GeneratedPDFFactureService(facturePDFAdapter, factureEventPublisherAdapter);
    }

    /**
     * Bean para crear un servicio que se encarga de recuperar una factura por su
     * identificador.
     * 
     * @param facturePersistenceAdapter el adaptador para acceder a la base de
     *                                  datos de facturas.
     * @return el servicio para recuperar una factura por su identificador.
     */
    @Bean
    GetFactureService getFactureService(FacturePersistenceAdapter facturePersistenceAdapter) {
        return new GetFactureService(facturePersistenceAdapter);
    }

    /**
     * Bean para crear un servicio que se encarga de listar todas las facturas
     * asociadas
     * a una empresa determinada, permitiendo la paginación de los resultados.
     * 
     * @param facturePersistenceAdapter el adaptador para acceder a la base de
     *                                  datos de facturas.
     * @return el servicio para listar las facturas de una empresa.
     */
    @Bean
    ListFacturesService listFacturesService(FacturePersistenceAdapter facturePersistenceAdapter) {
        return new ListFacturesService(facturePersistenceAdapter);
    }

    /**
     * Bean para crear un FacturePDFAdapter, que es responsable de
     * generar PDFs para las facturas.
     * 
     * @return el FacturePDFAdapter a utilizar en la aplicación.
     */
    @Bean
    public FacturePDFAdapter facturePDFAdapter() {
        return new FacturePDFAdapter();
    }
}
