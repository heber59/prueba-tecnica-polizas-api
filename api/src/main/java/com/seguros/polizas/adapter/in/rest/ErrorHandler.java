package com.seguros.polizas.adapter.in.rest;

import com.seguros.polizas.application.exception.*;
import com.seguros.polizas.domain.exception.ReglaNegocioException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ErrorHandler {
  @ExceptionHandler(ReglaNegocioException.class)
  ProblemDetail negocio(ReglaNegocioException ex) {
    return error(HttpStatus.CONFLICT, ex.getMessage());
  }

  @ExceptionHandler(NoEncontradoException.class)
  ProblemDetail noEncontrado(NoEncontradoException ex) {
    return error(HttpStatus.NOT_FOUND, ex.getMessage());
  }

  @ExceptionHandler(CoreException.class)
  ProblemDetail core(CoreException ex) {
    return error(HttpStatus.BAD_GATEWAY, ex.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ProblemDetail validacion(MethodArgumentNotValidException ex) {
    return error(
        HttpStatus.BAD_REQUEST,
        ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .collect(java.util.stream.Collectors.joining("; ")));
  }

  @ExceptionHandler({
    MethodArgumentTypeMismatchException.class,
    HttpMessageNotReadableException.class
  })
  ProblemDetail entrada(Exception ex) {
    return error(HttpStatus.BAD_REQUEST, "Solicitud inválida: verifica tipos y valores");
  }

  private ProblemDetail error(HttpStatus status, String mensaje) {
    return ProblemDetail.forStatusAndDetail(status, mensaje);
  }
}
