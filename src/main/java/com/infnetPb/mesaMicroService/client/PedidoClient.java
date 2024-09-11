package com.infnetPb.mesaMicroService.client;

import com.infnetPb.mesaMicroService.DTO.PedidoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "pedido-service", url = "http://pedido-service:8085")
public interface PedidoClient {

    @GetMapping("/pedidos/{id}")
    PedidoDTO getPedidoById(@PathVariable("id") UUID id);
}