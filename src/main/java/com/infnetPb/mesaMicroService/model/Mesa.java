package com.infnetPb.mesaMicroService.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
public class Mesa {

    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;

    private int qtdAssentosMax;
    private String infoAdicional;
    private String status;

    private UUID restauranteId;
    private String nomeRestaurante;
    private UUID pedidoId;
    private UUID reservaId;


}