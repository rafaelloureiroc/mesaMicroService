package com.infnetPb.mesaMicroService.client;

import com.infnetPb.mesaMicroService.DTO.ReservaDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "reserva-service", url = "http://reserva-service:8084")
public interface ReservaClient {

    @GetMapping("/reservas/{id}")
    ReservaDTO getReservaById(@PathVariable("id") UUID id);
}