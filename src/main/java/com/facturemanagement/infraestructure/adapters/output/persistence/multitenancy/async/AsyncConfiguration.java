package com.facturemanagement.infraestructure.adapters.output.persistence.multitenancy.async;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class AsyncConfiguration implements AsyncConfigurer {

/**
 * Devuelve el ejecutor a utilizar para ejecutar métodos asíncronos. Este
 * ejecutor es un ejecutor de tareas de grupo de hilos con un tamaño de grupo de hilos principal de 7,
 * un tamaño de grupo de hilos máximo de 42 y una capacidad de cola de 11. El prefijo de nombre de hilo es
 * "TenantAwareTaskExecutor-" y el decorador de tarea es una instancia de TenantAwareTaskDecorator.
 * El ejecutor se inicializa antes de ser devuelto.
 *
 * @return el ejecutor a utilizar para ejecutar métodos asíncronos
 */
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(7);
        executor.setMaxPoolSize(42);
        executor.setQueueCapacity(11);
        executor.setThreadNamePrefix("TenantAwareTaskExecutor-");
        executor.setTaskDecorator(new TenantAwareTaskDecorator());
        executor.initialize();

        return executor;
    }

}
