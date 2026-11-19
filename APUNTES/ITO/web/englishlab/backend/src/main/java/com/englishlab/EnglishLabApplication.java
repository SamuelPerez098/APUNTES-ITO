package com.englishlab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Excluye UserDetailsServiceAutoConfiguration: usamos autenticacion JWT propia
 * (sin formLogin/httpBasic ni UserDetailsService), asi no se genera el usuario
 * en memoria con "Using generated security password".
 */
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@ConfigurationPropertiesScan
@EnableScheduling
@EnableAsync
public class EnglishLabApplication {

    public static void main(String[] args) {
        SpringApplication.run(EnglishLabApplication.class, args);
    }
}
