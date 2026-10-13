package com.guilherme.minigrupocompras.scheduler;

import com.guilherme.minigrupocompras.service.GrupoCompraService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class GrupoCompraScheduler {

    private final GrupoCompraService grupoCompraService;
    //injeção via construtor
    public GrupoCompraScheduler(GrupoCompraService grupoCompraService) {
        this.grupoCompraService = grupoCompraService;
    }
    @Scheduled(fixedRate = 60000)
    public void executar() {
        int expirados = grupoCompraService.verificarEExpirarGruposVencidos();
        System.out.println("Scheduler: " + expirados + " grupo(s) expirados(s).");
    }

}
