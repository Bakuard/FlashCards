package com.bakuard.flashcards.controller;

import com.bakuard.flashcards.dto.DtoMapper;
import com.bakuard.flashcards.dto.word.WordForRepetitionResponse;
import com.bakuard.flashcards.dto.word.WordRepeatRequest;
import com.bakuard.flashcards.dto.word.WordResponse;
import com.bakuard.flashcards.service.WordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/repetition/words")
public class RepetitionOfWordsController {

    private WordService wordService;
    private DtoMapper dtoMapper;

    @Autowired
    public RepetitionOfWordsController(WordService wordService,
                                       DtoMapper dtoMapper) {
        this.wordService = wordService;
        this.dtoMapper = dtoMapper;
    }

    @GetMapping
    public ResponseEntity<Page<WordForRepetitionResponse>> findAllBy(@RequestParam("page") int page,
                                                                     @RequestParam(value = "size", required = false) int size) {
        return ResponseEntity.ok(null);
    }

    @PutMapping
    public ResponseEntity<WordResponse> repeat(@RequestBody WordRepeatRequest dto) {
        return ResponseEntity.ok(null);
    }

}
