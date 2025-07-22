package io.sicredi.aberturadecontasalarioefetivador;

import io.sicredi.engineering.libraries.idempotent.transaction.EnableIdempotentTransaction;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@EntityScan(basePackages = {"io.sicredi.aberturadecontasalarioefetivador"})
@EnableJpaRepositories(basePackages = {"io.sicredi.aberturadecontasalarioefetivador"})
@EnableIdempotentTransaction
@EnableFeignClients
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
