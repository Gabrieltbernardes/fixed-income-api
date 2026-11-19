package com.exemplo.fixedincome.api;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Converte erros em Problem Details (RFC 9457). Para erros de validação acrescenta
 * a propriedade {@code errors} com a lista de campos inválidos.
 */
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    static final String TITULO_REQUISICAO_INVALIDA = "Requisição inválida";

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    public record ErroCampo(String campo, String mensagem) {
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        List<ErroCampo> erros = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> new ErroCampo(e.getField(), e.getDefaultMessage()))
                .distinct()
                .sorted(Comparator.comparing(ErroCampo::campo).thenComparing(ErroCampo::mensagem))
                .toList();

        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, "Um ou mais campos são inválidos.");
        problema.setTitle(TITULO_REQUISICAO_INVALIDA);
        problema.setProperty("errors", erros);
        return handleExceptionInternal(ex, problema, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, "Corpo da requisição malformado ou com valor inválido.");
        problema.setTitle(TITULO_REQUISICAO_INVALIDA);

        ErroCampo erro = erroDeFormato(ex.getCause());
        if (erro != null) {
            problema.setProperty("errors", List.of(erro));
        }
        return handleExceptionInternal(ex, problema, headers, status, request);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleInesperado(Exception ex) {
        log.error("Erro inesperado", ex);
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu um erro inesperado.");
        problema.setTitle("Erro interno");
        return problema;
    }

    /** Para enum desconhecido ou tipo errado, aponta o campo e, se for enum, os valores aceitos. */
    @Nullable
    private static ErroCampo erroDeFormato(@Nullable Throwable causa) {
        if (!(causa instanceof InvalidFormatException ife) || ife.getPath().isEmpty()) {
            return null;
        }
        String campo = ife.getPath().stream()
                .map(JsonMappingException.Reference::getFieldName)
                .filter(nome -> nome != null)
                .collect(Collectors.joining("."));
        Class<?> tipo = ife.getTargetType();
        String mensagem = tipo != null && tipo.isEnum()
                ? "valor inválido; aceitos: " + Arrays.stream(tipo.getEnumConstants())
                        .map(Object::toString).collect(Collectors.joining(", "))
                : "valor em formato inválido";
        return new ErroCampo(campo, mensagem);
    }
}
