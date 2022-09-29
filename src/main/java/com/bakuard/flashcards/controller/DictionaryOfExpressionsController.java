package com.bakuard.flashcards.controller;

import com.bakuard.flashcards.config.security.RequestContext;
import com.bakuard.flashcards.controller.message.Messages;
import com.bakuard.flashcards.dto.DtoMapper;
import com.bakuard.flashcards.dto.expression.ExpressionAddRequest;
import com.bakuard.flashcards.dto.expression.ExpressionForDictionaryListResponse;
import com.bakuard.flashcards.dto.expression.ExpressionResponse;
import com.bakuard.flashcards.dto.expression.ExpressionUpdateRequest;
import com.bakuard.flashcards.model.expression.Expression;
import com.bakuard.flashcards.service.ExpressionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Словарь устойчевых выражений пользователя")
@RestController
@RequestMapping("/dictionary/expressions")
public class DictionaryOfExpressionsController {

    private static final Logger logger = LoggerFactory.getLogger(DictionaryOfExpressionsController.class.getName());


    private ExpressionService expressionService;
    private DtoMapper mapper;
    private RequestContext requestContext;
    private Messages messages;

    @Autowired
    public DictionaryOfExpressionsController(ExpressionService expressionService,
                                             DtoMapper mapper,
                                             RequestContext requestContext,
                                             Messages messages) {
        this.expressionService = expressionService;
        this.mapper = mapper;
        this.requestContext = requestContext;
        this.messages = messages;
    }

    @PostMapping
    public ResponseEntity<ExpressionResponse> add(@RequestBody ExpressionAddRequest dto) {
        UUID userId = requestContext.getCurrentJwsBody();
        logger.info("user {} add word '{}'", userId, dto.getValue());

        Expression expression = mapper.toExpression(dto, userId);
        expression = expressionService.save(expression);
        return ResponseEntity.ok(mapper.toExpressionResponse(expression));
    }

    @PutMapping
    public ResponseEntity<ExpressionResponse> update(@RequestBody ExpressionUpdateRequest dto) {
        UUID userId = requestContext.getCurrentJwsBody();
        logger.info("user {} update word {}", userId, dto.getExpressionId());

        Expression expression = mapper.toExpression(dto, userId);
        expression = expressionService.save(expression);
        return ResponseEntity.ok(mapper.toExpressionResponse(expression));
    }

    @GetMapping
    public ResponseEntity<Page<ExpressionForDictionaryListResponse>> findAllBy(@RequestParam int page,
                                                                               @RequestBody(required = false) int size,
                                                                               @RequestParam(required = false) String sort) {
        UUID userId = requestContext.getCurrentJwsBody();
        logger.info("user {} get expressions by page={}, size={}, sort={}", page, size, sort);

        Pageable pageable = mapper.toPageableForDictionaryExpressions(page, size, sort);
        Page<ExpressionForDictionaryListResponse> result = mapper.toExpressionForDictionaryListResponse(
                expressionService.findByUserId(userId, pageable)
        );

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpressionResponse> findById(@PathVariable UUID id) {
        UUID userId = requestContext.getCurrentJwsBody();
        logger.info("user {} get expression by id={}", userId, id);

        Expression expression = expressionService.tryFindById(userId, id);
        return ResponseEntity.ok(mapper.toExpressionResponse(expression));
    }

    @GetMapping("/value/{value}")
    public ResponseEntity<ExpressionResponse> findByValue(@PathVariable String value) {
        UUID userId = requestContext.getCurrentJwsBody();
        logger.info("user {} get expression by value '{}'", userId, value);

        Expression expression = expressionService.tryFindByValue(userId, value);
        return ResponseEntity.ok(mapper.toExpressionResponse(expression));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable UUID id) {
        UUID userId = requestContext.getCurrentJwsBody();
        logger.info("user {} delete expression by id={}", userId, id);

        expressionService.tryDeleteById(userId, id);
        return ResponseEntity.ok(messages.getMessage("dictionary.expressions.delete"));
    }

}
