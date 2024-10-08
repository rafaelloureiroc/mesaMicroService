package com.infnetPb.mesaMicroService.client;

import com.infnetPb.mesaMicroService.DTO.RestauranteDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "restaurante-service", url = "http://restaurante-service:8083")
public interface RestauranteClient {

    @GetMapping("/restaurantes/{id}")
    RestauranteDTO getRestauranteById(@PathVariable("id") UUID id);
}