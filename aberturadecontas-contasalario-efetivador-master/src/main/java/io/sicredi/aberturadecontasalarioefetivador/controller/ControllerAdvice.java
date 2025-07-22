package io.sicredi.aberturadecontasalarioefetivador.controller;

import io.sicredi.aberturadecontasalarioefetivador.exceptions.*;
import io.sicredi.engineering.libraries.idempotent.transaction.IdempotentTransactionDuplicatedException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class ControllerAdvice {

    public static final String REGEX_INDICE_CADASTRO = "cadastros\\[(\\d+)]";
    public static final String STRING_ERRO_REQUISICAO_SEM_CORPO = "{\"error\": \"O corpo da requisição é obrigatório e está ausente.\"}";

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public void handleException(Exception e){
        log.error(e.getMessage(), e);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    protected ResponseEntity<Object> handleHttpMessageNotReadable() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body((STRING_ERRO_REQUISICAO_SEM_CORPO));
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(IdempotentTransactionDuplicatedException.class)
    public void idempotentTransactionDuplicatedException(Exception e){
        log.error(e.getMessage());
    }

    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler({CanalNaoEncontradoOuInatvoException.class, CanalSemCorrespondenciaException.class, TransactionIDObrigatorioException.class,
            TransactionIDComDataInvalidaException.class, TransactionIDForaDePadraoException.class})
    public ResponseEntity<Map<String, String>> handleTransactionIDHeaderExceptions(Exception e){
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", e.getMessage());
        return ResponseEntity.unprocessableEntity().body(errorResponse);
    }

    @ExceptionHandler(WebhookException.class)
    public ResponseEntity<Map<String, String>> handleWebhookValidationException(WebhookException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("urlWebhook", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorResponse);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<Map<String, String>> handleMissingRequesHeaderException(MissingRequestHeaderException e){
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("header", String.format("%s é obrigatório", e.getHeaderName()));
        return ResponseEntity.badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorResponse);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e){
        Map<String, String> criticas = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField, //nome do campo
                        FieldError::getDefaultMessage, //mensagem de erro
                        (msg1, msg2) -> msg1,
                        LinkedHashMap::new
                ));
        Map<String, String> criticasOrdenadas = criticas.entrySet()
                .stream()
                .sorted((entry1, entry2) -> comparaKeys(entry1.getKey(), entry2.getKey()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (msg1, msg2) -> msg1,
                        LinkedHashMap::new
                ));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(criticasOrdenadas);
    }

    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(AgenciaECooperativaNaoCorrespondentesException.class)
    public ResponseEntity<Map<String, String>> handleGestentException(AgenciaECooperativaNaoCorrespondentesException e){
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("numCooperativa - numAgencia", e.getMessage());
        return ResponseEntity.unprocessableEntity()
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorResponse);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolationException(ConstraintViolationException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        String mensagens = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(";"));
        errorResponse.put("error", mensagens);
        return ResponseEntity.badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorResponse);
    }

    private int comparaKeys(String key1, String key2) {
        int index1 = obtemIndice(key1);
        int index2 = obtemIndice(key2);
        return Integer.compare(index1, index2);
    }

    private int obtemIndice(String key) {
        Pattern pattern = Pattern.compile(REGEX_INDICE_CADASTRO);
        Matcher matcher = pattern.matcher(key);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return Integer.MIN_VALUE;
    }
}
