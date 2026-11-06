package com.example.demo.exception;

public class SemilleroNoEncontradoException extends RuntimeException {
    public SemilleroNoEncontradoException(Long id) {
        super("No existe el semillero con id " + id);
    }
}
