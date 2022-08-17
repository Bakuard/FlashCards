package com.bakuard.flashcards.controller;

import com.bakuard.flashcards.dto.DtoMapper;
import com.bakuard.flashcards.dto.word.WordAddRequest;
import com.bakuard.flashcards.dto.word.WordForDictionaryListResponse;
import com.bakuard.flashcards.dto.word.WordResponse;
import com.bakuard.flashcards.dto.word.WordUpdateRequest;
import com.bakuard.flashcards.service.WordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/dictionary/words")
public class DictionaryOfWordsController {

    private WordService wordService;
    private DtoMapper dtoMapper;

    @Autowired
    public DictionaryOfWordsController(WordService wordService,
                                       DtoMapper dtoMapper) {
        this.wordService = wordService;
        this.dtoMapper = dtoMapper;
    }

    @PostMapping
    public ResponseEntity<WordResponse> add(@RequestBody WordAddRequest dto) {
        return ResponseEntity.ok(null);
    }

    @PutMapping
    public ResponseEntity<WordResponse> update(@RequestBody WordUpdateRequest dto) {
        return ResponseEntity.ok(null);
    }

    @GetMapping
    public ResponseEntity<Page<WordForDictionaryListResponse>> findAllBy(@RequestParam("page") int page,
                                                                         @RequestParam(value = "size", required = false) int size,
                                                                         @RequestParam(value = "sort", required = false) String sort) {
        return ResponseEntity.ok(null);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WordResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(null);
    }

    @GetMapping("/value/{value}")
    public ResponseEntity<WordResponse> findByValue(@PathVariable String value) {
        return ResponseEntity.ok(null);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable UUID id) {
        return ResponseEntity.ok(null);
    }

}
