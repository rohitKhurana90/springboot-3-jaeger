package com.spring.jaeger;

import com.spring.jaeger.feignconfig.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "example-client", url = "http://localhost:8091", configuration = FeignConfig.class)
public interface ExampleFeignClient {

    @GetMapping("/service/path3")
    String getExample();
}
