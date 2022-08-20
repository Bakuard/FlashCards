package com.bakuard.flashcards.controller;

import com.bakuard.flashcards.config.security.QueryContext;
import com.bakuard.flashcards.controller.message.Messages;
import com.bakuard.flashcards.dto.DtoMapper;
import com.bakuard.flashcards.dto.word.WordAddRequest;
import com.bakuard.flashcards.dto.word.WordForDictionaryListResponse;
import com.bakuard.flashcards.dto.word.WordResponse;
import com.bakuard.flashcards.dto.word.WordUpdateRequest;
import com.bakuard.flashcards.model.word.Word;
import com.bakuard.flashcards.service.WordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/dictionary/words")
public class DictionaryOfWordsController {

    private WordService wordService;
    private DtoMapper dtoMapper;
    private QueryContext queryContext;
    private Messages messages;

    @Autowired
    public DictionaryOfWordsController(WordService wordService,
                                       DtoMapper dtoMapper,
                                       QueryContext queryContext,
                                       Messages messages) {
        this.wordService = wordService;
        this.dtoMapper = dtoMapper;
        this.queryContext = queryContext;
        this.messages = messages;
    }

    @PostMapping
    public ResponseEntity<WordResponse> add(@RequestBody WordAddRequest dto) {
        Word word = dtoMapper.toWord(dto, queryContext.getAndClearUserId());
        word = wordService.save(word);
        return ResponseEntity.ok(dtoMapper.toWordResponse(word));
    }

    @PutMapping
    public ResponseEntity<WordResponse> update(@RequestBody WordUpdateRequest dto) {
        Word word = dtoMapper.toWord(dto, queryContext.getAndClearUserId());
        word = wordService.save(word);
        return ResponseEntity.ok(dtoMapper.toWordResponse(word));
    }

    @GetMapping
    public ResponseEntity<Page<WordForDictionaryListResponse>> findAllBy(@RequestParam("page") int page,
                                                                         @RequestParam(value = "size", required = false) int size,
                                                                         @RequestParam(value = "sort", required = false) String sort) {
        UUID userId = queryContext.getAndClearUserId();
        Pageable pageable = dtoMapper.toPageableForDictionaryWords(page, size, sort);

        Page<WordForDictionaryListResponse> result = dtoMapper.toWordsForDictionaryListResponse(
                wordService.findByUserId(userId, pageable)
        );

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WordResponse> findById(@PathVariable UUID id) {
        UUID userId = queryContext.getAndClearUserId();
        Word word = wordService.tryFindById(userId, id);
        return ResponseEntity.ok(dtoMapper.toWordResponse(word));
    }

    @GetMapping("/value/{value}")
    public ResponseEntity<WordResponse> findByValue(@PathVariable String value) {
        UUID userId = queryContext.getAndClearUserId();
        Word word = wordService.tryFindByValue(userId, value);
        return ResponseEntity.ok(dtoMapper.toWordResponse(word));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable UUID id) {
        UUID userId = queryContext.getAndClearUserId();
        wordService.tryDeleteById(userId, id);
        return ResponseEntity.ok(messages.getMessage("dictionary.words.delete"));
    }

}
