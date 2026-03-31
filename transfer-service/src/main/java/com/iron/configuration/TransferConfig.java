package com.iron.configuration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;

@Configuration
public class TransferConfig {

    @Bean("transferAccountsClientBuilder")
    @LoadBalanced
    public RestClient.Builder accountsClientBuilder() {
        return RestClient.builder();
    }

    @Bean("transferNotificationsClientBuilder")
    @LoadBalanced
    public RestClient.Builder notificationsClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService authorizedClientService) {
        AuthorizedClientServiceOAuth2AuthorizedClientManager manager =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                        clientRegistrationRepository, authorizedClientService);
        manager.setAuthorizedClientProvider(
                OAuth2AuthorizedClientProviderBuilder.builder()
                        .clientCredentials()
                        .build());
        return manager;
    }

    @Bean
    public RestClient accountsRestClient(
            @Qualifier("transferAccountsClientBuilder") RestClient.Builder builder,
            OAuth2AuthorizedClientManager manager) {
        OAuth2ClientHttpRequestInterceptor interceptor = new OAuth2ClientHttpRequestInterceptor(manager);
        interceptor.setClientRegistrationIdResolver(request -> "transfer-service-client");
        return builder
                .baseUrl("lb://accounts-service")
                .requestInterceptor(interceptor)
                .build();
    }

    @Bean
    public RestClient notificationsRestClient(
            @Qualifier("transferNotificationsClientBuilder") RestClient.Builder builder,
            OAuth2AuthorizedClientManager manager) {
        OAuth2ClientHttpRequestInterceptor interceptor = new OAuth2ClientHttpRequestInterceptor(manager);
        interceptor.setClientRegistrationIdResolver(request -> "transfer-service-client");
        return builder
                .baseUrl("lb://notifications-service")
                .requestInterceptor(interceptor)
                .build();
    }
}