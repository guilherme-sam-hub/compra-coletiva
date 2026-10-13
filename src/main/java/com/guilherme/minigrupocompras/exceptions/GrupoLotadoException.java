package com.guilherme.minigrupocompras.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class GrupoLotadoException extends RuntimeException {
    public GrupoLotadoException(Long grupoId, int maxPessoas ) {
        super(
                "Grupo: " + grupoId + " atingiu o número máximo de " + maxPessoas + " pessoas.");
    }
}

