package com.bakuard.flashcards.dal;

import com.bakuard.flashcards.model.Word;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public interface WordsRepository extends PagingAndSortingRepository<Word, UUID> {

    @Query("select * from words where value = :value;")
    public Optional<Word> findByValue(String value);

}
