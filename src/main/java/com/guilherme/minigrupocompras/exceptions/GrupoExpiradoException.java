package com.guilherme.minigrupocompras.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.GONE)
public class GrupoExpiradoException extends RuntimeException {
    public GrupoExpiradoException(Long grupoId) {
        super("Grupo de compra expirado: " + grupoId);
    }
}
