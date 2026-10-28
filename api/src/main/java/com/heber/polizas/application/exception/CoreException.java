package com.heber.polizas.application.exception;

public class CoreException extends RuntimeException {
  public CoreException(String mensaje, Throwable causa) {
    super(mensaje, causa);
  }
}
