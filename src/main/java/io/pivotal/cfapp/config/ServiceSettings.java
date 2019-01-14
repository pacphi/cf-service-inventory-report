package io.pivotal.cfapp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "cf")
public class ServiceSettings {

    private String apiHost;
    private boolean sslValidationSkipped;
    private String username;
    private String password;
    private String passcode;

}
