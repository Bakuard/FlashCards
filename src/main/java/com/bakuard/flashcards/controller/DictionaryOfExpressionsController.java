package com.bakuard.flashcards.controller;

import com.bakuard.flashcards.config.security.RequestContext;
import com.bakuard.flashcards.controller.message.Messages;
import com.bakuard.flashcards.dto.DtoMapper;
import com.bakuard.flashcards.service.ExpressionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Словарь устойчевых выражений пользователя")
@RestController
@RequestMapping("/dictionary/expressions")
public class DictionaryOfExpressionsController {

    private static final Logger logger = LoggerFactory.getLogger(DictionaryOfExpressionsController.class.getName());


    private ExpressionService expressionService;
    private DtoMapper dtoMapper;
    private RequestContext requestContext;
    private Messages messages;

    @Autowired
    public DictionaryOfExpressionsController(ExpressionService expressionService,
                                             DtoMapper dtoMapper,
                                             RequestContext requestContext,
                                             Messages messages) {
        this.expressionService = expressionService;
        this.dtoMapper = dtoMapper;
        this.requestContext = requestContext;
        this.messages = messages;
    }



}
