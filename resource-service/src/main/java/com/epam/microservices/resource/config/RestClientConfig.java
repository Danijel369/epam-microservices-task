package com.epam.microservices.resource.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    /**
     * A load-balanced {@link RestClient.Builder}. The {@code @LoadBalanced} marker
     * tells Spring Cloud LoadBalancer to add an interceptor that resolves a logical
     * service id (e.g. {@code song-service}) to a concrete instance obtained from
     * Eureka, then round-robins across the available instances.
     */
    @Bean
    @LoadBalanced
    public RestClient.Builder loadBalancedRestClientBuilder() {
        return RestClient.builder();
    }

    /**
     * The Song Service client. Its base URL is the logical service id, so every
     * request is routed through the load balancer to one of the registered
     * song-service instances.
     */
    @Bean
    public RestClient songServiceRestClient(@LoadBalanced RestClient.Builder loadBalancedRestClientBuilder,
                                            @Value("${song-service.base-url}") String baseUrl) {
        return loadBalancedRestClientBuilder.baseUrl(baseUrl).build();
    }
}
