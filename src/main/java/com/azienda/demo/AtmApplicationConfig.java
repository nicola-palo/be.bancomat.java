package com.azienda.demo;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.azienda.demo.config.JwtProperties;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class AtmApplicationConfig {
}
