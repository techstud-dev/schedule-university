package com.techstud.schedule_university.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@EnableConfigurationProperties(JwtProperties.class)
@ConfigurationProperties(prefix = "jwt")
@Configuration("JwtProperties")
@Getter
@Setter
public class JwtProperties {
    private long accessTokenExpiration;
    private long refreshTokenExpiration;
}
