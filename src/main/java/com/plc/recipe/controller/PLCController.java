package com.plc.recipe.controller;

import com.plc.recipe.service.EthernetIPService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/plc")
@Slf4j
public class PLCController {

    private final EthernetIPService ethernetIPService;

    public PLCController(EthernetIPService ethernetIPService) {
        this.ethernetIPService = ethernetIPService;
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getPLCStatus() {
        log.info("REST request to get PLC status");

        boolean connected = ethernetIPService.isPLCConnected();
        boolean offline = ethernetIPService.isOfflineMode();

        return ResponseEntity.ok(Map.of(
                "connected", connected,
                "offlineMode", offline,
                "message", offline ? "PLC running in offline mode" : "PLC connected"
        ));
    }

    @PostMapping("/mode/offline")
    public ResponseEntity<Map<String, Object>> enableOfflineMode() {
        log.info("REST request to enable offline mode");

        ethernetIPService.setOfflineMode(true);

        return ResponseEntity.ok(Map.of(
                "message", "Offline mode enabled",
                "offlineMode", true
        ));
    }

    @PostMapping("/mode/online")
    public ResponseEntity<Map<String, Object>> enableOnlineMode() {
        log.info("REST request to enable online mode");

        ethernetIPService.setOfflineMode(false);

        return ResponseEntity.ok(Map.of(
                "message", "Online mode enabled (attempting to connect to PLC)",
                "offlineMode", false
        ));
    }
}
