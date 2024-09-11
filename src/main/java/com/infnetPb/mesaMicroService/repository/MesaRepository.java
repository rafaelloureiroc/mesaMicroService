package com.infnetPb.mesaMicroService.repository;

import com.infnetPb.mesaMicroService.model.Mesa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MesaRepository extends JpaRepository<Mesa, UUID> {
}