package com.github.pzn.hellomarket.controller;

import static com.github.pzn.hellomarket.integration.appdirect.ErrorCode.UNKNOWN_ERROR;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import com.github.pzn.hellomarket.integration.appdirect.AppDirectApiResponse;
import com.github.pzn.hellomarket.integration.appdirect.processor.NotificationProcessorException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
@Slf4j
public class HelloMarketControllerAdvice {

  @ResponseStatus(INTERNAL_SERVER_ERROR)
  @ExceptionHandler(Throwable.class)
  public @ResponseBody AppDirectApiResponse generalErrorHandler(Throwable e) {

    log.error("{}", e);
    return AppDirectApiResponse.builder()
        .success(false)
        .errorCode(UNKNOWN_ERROR)
        .build();
  }

  @ResponseStatus(INTERNAL_SERVER_ERROR)
  @ExceptionHandler(DataAccessException.class)
  public @ResponseBody AppDirectApiResponse daoHandler(DataAccessException e) {

    log.error("{}", e);
    return AppDirectApiResponse.builder()
        .success(false)
        .errorCode(UNKNOWN_ERROR)
        .build();
  }

  @ResponseStatus(INTERNAL_SERVER_ERROR)
  @ExceptionHandler(NotificationProcessorException.class)
  public @ResponseBody AppDirectApiResponse processorExceptionHandler(NotificationProcessorException e) {

    log.info("{}", e);
    return AppDirectApiResponse.builder()
        .success(false)
        .errorCode(e.getErrorCode())
        .accountIdentifier(e.getAccountIdentifier())
        .userIdentifier(e.getUserIdentifier())
        .message(e.getMessage())
        .build();
  }
}
