package com.livraison.backend.controller;

import com.livraison.backend.dto.GpsWsUpdateDTO;
import com.livraison.backend.service.GpsWsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class GpsWsController {

    private final GpsWsService gpsWsService;

    @MessageMapping("/gps/update")
    public void handleGpsUpdate(GpsWsUpdateDTO payload) {
        log.info("[COLISID] STEP 2/5 backend   received colisId = \"{}\"", payload.getColisId());
        gpsWsService.processUpdate(payload);
    }
}
