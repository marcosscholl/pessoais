package io.sicredi.aberturadecontaslegadooriginacao.config;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.sicredi.capital.acquisition.grpc.AcquisitionConfigurationServiceGrpc;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcClientConfig {

    @Value("${sicredi.aberturadecontas-legado-originacao.client.grpc.capital-account-acquisition.url}")
    private String url;
    @Value("${sicredi.aberturadecontas-legado-originacao.client.grpc.capital-account-acquisition.port}")
    private int port;
    @Bean
    public ManagedChannel managedChannel() {
        return ManagedChannelBuilder.forAddress(url, port)
                .useTransportSecurity()
                .build();
    }

    @Bean
    public AcquisitionConfigurationServiceGrpc.AcquisitionConfigurationServiceBlockingStub acquisitionConfigurationServiceBlockingStub(ManagedChannel managedChannel) {
        return AcquisitionConfigurationServiceGrpc.newBlockingStub(managedChannel);
    }
}