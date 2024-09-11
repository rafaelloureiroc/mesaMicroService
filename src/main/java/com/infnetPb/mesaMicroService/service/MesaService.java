package com.infnetPb.mesaMicroService.service;


import com.infnetPb.mesaMicroService.DTO.MesaDTO;
import com.infnetPb.mesaMicroService.DTO.PedidoDTO;
import com.infnetPb.mesaMicroService.DTO.RestauranteDTO;
import com.infnetPb.mesaMicroService.client.PedidoClient;
import com.infnetPb.mesaMicroService.client.ReservaClient;
import com.infnetPb.mesaMicroService.client.RestauranteClient;
import com.infnetPb.mesaMicroService.event.MesaCadastradaEvent;
import com.infnetPb.mesaMicroService.model.Mesa;
import com.infnetPb.mesaMicroService.model.history.MesaHistory;
import com.infnetPb.mesaMicroService.repository.MesaRepository;
import com.infnetPb.mesaMicroService.repository.historyRepository.MesaHistoryRepository;
import jakarta.transaction.Transactional;
import org.apache.log4j.Logger;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Transactional
public class MesaService {

    @Autowired
    private MesaRepository mesaRepository;

    @Autowired
    private RestauranteClient restauranteClient;

    @Autowired
    private PedidoClient pedidoClient;

    @Autowired
    private ReservaClient reservaClient;

    @Autowired
    private MesaHistoryRepository mesaHistoryRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private final static Logger logger = Logger.getLogger(MesaService.class);
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 2000;

    @Transactional
    public MesaDTO createMesa(MesaDTO mesaDTO) {
        RestauranteDTO restaurante = restauranteClient.getRestauranteById(mesaDTO.getRestauranteId());
        if (restaurante == null) {
            logger.error("Restaurante não encontrado");
            throw new RuntimeException("Restaurante não encontrado");
        }

        Mesa mesa = new Mesa();
        mesa.setQtdAssentosMax(mesaDTO.getQtdAssentosMax());
        mesa.setInfoAdicional(mesaDTO.getInfoAdicional());
        mesa.setRestauranteId(restaurante.getId());
        mesa.setNomeRestaurante(restaurante.getNome());

        Mesa savedMesa = mesaRepository.save(mesa);
        saveMesaHistory(savedMesa, "CREATE");

        MesaCadastradaEvent event = new MesaCadastradaEvent(
                savedMesa.getId(),
                restaurante.getId(),
                savedMesa.getQtdAssentosMax(),
                savedMesa.getInfoAdicional(),
                "CREATED"
        );

        logger.info("Tentando enviar evento MesaCadastrada: " + event);

        CompletableFuture.runAsync(() -> {
            boolean success = sendEventWithRetry(event, "mesaExchange", "mesaCadastrada");
            if (success) {
                logger.info("Evento MesaCadastrada enviado com sucesso.");
            } else {
                logger.error("Falha ao enviar evento MesaCadastrada após " + MAX_RETRIES + " tentativas.");
            }
        });

        return mapToDTO(savedMesa);
    }

    private boolean sendEventWithRetry(Object event, String exchange, String routingKey) {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                rabbitTemplate.convertAndSend(exchange, routingKey, event);
                return true;
            } catch (Exception e) {
                logger.error("Erro ao enviar evento (tentativa " + attempt + "): " + e.getMessage());
                if (attempt < MAX_RETRIES) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
        return false;
    }


    public List<MesaDTO> getAllMesas() {
        List<Mesa> mesas = mesaRepository.findAll();
        return mesas.stream()
                .map(this::mapToDTOWithPedidos)
                .collect(Collectors.toList());
    }

    public MesaDTO getMesaById(UUID id) {
        Mesa mesa = mesaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mesa não encontrada"));
        return mapToDTOWithPedidos(mesa);
    }

    public MesaDTO updateMesa(UUID id, MesaDTO mesaDTO) {
        Mesa mesa = mesaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mesa não encontrada"));

        mesa.setQtdAssentosMax(mesaDTO.getQtdAssentosMax());
        mesa.setInfoAdicional(mesaDTO.getInfoAdicional());
        mesa.setReservaId(mesaDTO.getReservaId());
        //mesa.setStatus(mesaDTO.getReservaId() != null ? "Reservada" : "Disponível");

        if (mesaDTO.getPedidos() != null) {
            if (!mesaDTO.getPedidos().isEmpty()) {
                mesa.setPedidoId(UUID.fromString(mesaDTO.getPedidos().get(0)));
            } else {
                mesa.setPedidoId(null);
            }
        }

        Mesa updatedMesa = mesaRepository.save(mesa);
        saveMesaHistory(updatedMesa, "UPDATE");

        return mapToDTO(updatedMesa);
    }

    public void deleteMesaById(UUID id) {
        Mesa mesa = mesaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mesa não encontrada"));

        mesaRepository.deleteById(id);
        saveMesaHistory(mesa, "DELETE");
    }

    public List<MesaHistory> getAllMesaHistories() {
        return mesaHistoryRepository.findAll();
    }

    private void saveMesaHistory(Mesa mesa, String operation) {
        MesaHistory mesaHistory = new MesaHistory();
        mesaHistory.setMesa(mesa);
        mesaHistory.setQtdAssentosMax(mesa.getQtdAssentosMax());
        mesaHistory.setInfoAdicional(mesa.getInfoAdicional());
        mesaHistory.setStatus(mesa.getReservaId() != null ? "Reservada" : "Disponível");
        mesaHistory.setTimestamp(LocalDateTime.now());
        mesaHistory.setOperation(operation);
        mesaHistoryRepository.save(mesaHistory);
    }

    private MesaDTO mapToDTO(Mesa mesa) {
        MesaDTO mesaDTO = new MesaDTO();
        mesaDTO.setId(mesa.getId());
        mesaDTO.setQtdAssentosMax(mesa.getQtdAssentosMax());
        mesaDTO.setInfoAdicional(mesa.getInfoAdicional());
        mesaDTO.setNomeRestaurante(mesa.getNomeRestaurante());
        mesaDTO.setRestauranteId(mesa.getRestauranteId());
        mesaDTO.setReservaId(mesa.getReservaId());

        return mesaDTO;
    }

    private MesaDTO mapToDTOWithPedidos(Mesa mesa) {
        MesaDTO mesaDTO = mapToDTO(mesa);

        if (mesaDTO.getReservaId() != null) {
            mesaDTO.setStatus("Reservada");
        } else {
            mesaDTO.setStatus("Disponível");
        }

        if (mesa.getPedidoId() != null) {
            PedidoDTO pedido = pedidoClient.getPedidoById(mesa.getPedidoId());
            if (pedido != null) {
                List<String> descricoesPedidos = List.of(
                        pedido.getDescricaoPedido() + " - Valor total: " + pedido.getValorTotal()
                );
                mesaDTO.setPedidos(descricoesPedidos);
            }
        } else {
            mesaDTO.setPedidos(List.of());
        }

        return mesaDTO;
    }
}