package com.shipment.shipmentservice.domain.exception;

import lombok.Getter;

@Getter
public abstract class DomainException extends RuntimeException {

  private final String errorCode;

  protected DomainException(String message, String errorCode) {
    super(message);
    this.errorCode = errorCode;
  }
}