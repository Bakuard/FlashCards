package com.bakuard.flashcards.controller;

import com.bakuard.flashcards.config.SpringConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = SpringConfig.class)
@TestPropertySource(locations = "classpath:application.properties")
class DictionaryOfWordsControllerTest {



}