package com.guilherme.minigrupocompras.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class GrupoNaoEncontradoException extends RuntimeException {
    public GrupoNaoEncontradoException(Long id) {
        super("Grupo de compra não encontrado: " + id);
    }
}