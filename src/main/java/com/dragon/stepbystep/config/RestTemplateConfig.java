package com.dragon.stepbystep.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate 설정
 * AI 서버 호출용
 */
@Configuration
public class RestTemplateConfig {

    /**
     * RestTemplate Bean 생성
     * 기본 설정으로 충분함
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}