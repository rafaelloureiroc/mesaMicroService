package com.infnetPb.mesaMicroService.controller;


import com.infnetPb.mesaMicroService.DTO.MesaDTO;
import com.infnetPb.mesaMicroService.model.history.MesaHistory;
import com.infnetPb.mesaMicroService.repository.MesaRepository;
import com.infnetPb.mesaMicroService.service.MesaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/mesas")
public class MesaController {

    @Autowired
    private MesaService mesaService;

    @Autowired
    private MesaRepository mesaRepository;

    @PostMapping
    public ResponseEntity<MesaDTO> createMesa(@RequestBody MesaDTO mesaDTO) {
        try {
            MesaDTO createdMesa = mesaService.createMesa(mesaDTO);
            return new ResponseEntity<>(createdMesa, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<MesaDTO> updateMesa(@PathVariable UUID id, @RequestBody MesaDTO mesaDTO) {
        try {
            MesaDTO updatedMesa = mesaService.updateMesa(id, mesaDTO);
            return new ResponseEntity<>(updatedMesa, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping
    public ResponseEntity<List<MesaDTO>> getAllMesas() {
        List<MesaDTO> mesas = mesaService.getAllMesas();
        return new ResponseEntity<>(mesas, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MesaDTO> getMesaById(@PathVariable UUID id) {
        try {
            MesaDTO mesaDTO = mesaService.getMesaById(id);
            return new ResponseEntity<>(mesaDTO, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMesaById(@PathVariable UUID id) {
        try {
            mesaService.deleteMesaById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/historico")
    public ResponseEntity<List<MesaHistory>> getAllMesaHistories() {
        List<MesaHistory> history = mesaService.getAllMesaHistories();
        return new ResponseEntity<>(history, HttpStatus.OK);
    }
}