package com.spring.jaeger.feignconfig;

import feign.RequestInterceptor;
import io.opentracing.Tracer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor feignRequestInterceptor(Tracer tracer) {
        return new OpenTracingFeignRequestInterceptor(tracer);
    }
}
