package com.techstud.schedule_university.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "jwt")
@Configuration("BeanTokenPropertiesBug")
@Getter
@Setter
public class TokenProperties {
    private long accessTokenExpiration;
    private long refreshTokenExpiration;
}
