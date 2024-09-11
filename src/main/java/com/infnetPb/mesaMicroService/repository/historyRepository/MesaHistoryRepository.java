package com.infnetPb.mesaMicroService.repository.historyRepository;


import com.infnetPb.mesaMicroService.model.history.MesaHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MesaHistoryRepository extends JpaRepository<MesaHistory, UUID> {
}