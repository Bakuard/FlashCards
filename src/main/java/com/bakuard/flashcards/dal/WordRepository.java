package com.bakuard.flashcards.dal;

import com.bakuard.flashcards.model.word.Word;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WordRepository extends PagingAndSortingRepository<Word, UUID> {

    @Query("select * from words where user_id = :userId and word_id = :wordId;")
    public Optional<Word> findById(UUID userId, UUID wordId);

    @Query("""
            select * from words
                where user_id = :userId and distance(:value, value, :maxDistance) != -1;
            """)
    public List<Word> findByValue(UUID userId, String value, int maxDistance, long limit, long offset);

    @Query("""
            select * from words
                inner join words_translations
                    on words.word_id = words_translations.word_id
                       and words.user_id = :userId
                       and words_translations.value = :translate
                order by words.value
                limit :limit offset :offset;
            """)
    public List<Word> findByTranslate(UUID userId, String translate, long limit, long offset);

    @Modifying
    @Query("delete from words where user_id = :userId and word_id = :wordId;")
    public void deleteById(UUID userId, UUID wordId);

    @Query("select exists(select 1 from words where user_id = :userId and word_id = :wordId);")
    public boolean existsById(UUID userId, UUID wordId);

    @Query("select count(*) from words where user_id = :userId")
    public long count(UUID userId);

    @Query("""
            select count(*) from words
             where user_id = :userId and (last_date_of_repeat_from_english + repeat_interval_from_english) <= :date;
            """)
    public long countForRepeatFromEnglish(UUID userId, LocalDate date);

    @Query("""
            select count(*) from words
             where user_id = :userId and (last_date_of_repeat_from_native + repeat_interval_from_native) <= :date;
            """)
    public long countForRepeatFromNative(UUID userId, LocalDate date);

    @Query("""
            select count(*) from (
               select value
                   from words
                   where user_id = :userId and distance(:value, value, :maxDistance) != -1
            )
            """)
    public long countForValue(UUID userId, String value, int maxDistance);

    @Query("""
            select count(*)
                from words
                inner join words_translations
                    on words_translations.word_id = words.word_id
                       and words_translations.value = :translate
                       and words.user_id = :userId;
            """)
    public long countForTranslate(UUID userId, String translate);

    @Query("""
            select COALESCE(min(row_number) - 1, -1) as result
                from (
                    select words.value as word_value, row_number() over (order by value) as row_number
                        from words
                        where words.user_id = :userId
                ) as words_with_row_number
                where lower(left(word_value, 1)) = lower(:firstCharacter);
            """)
    public long getWordIndexByFirstCharacter(UUID userId, String firstCharacter);

    public Page<Word> findByUserId(UUID userId, Pageable pageable);

    @Query("""
            select * from words
             where user_id = :userId and (last_date_of_repeat_from_english + repeat_interval_from_english) <= :date
             order by value limit :limit offset :offset;
            """)
    public List<Word> findAllForRepeatFromEnglish(UUID userId, LocalDate date, long limit, long offset);

    @Query("""
            select * from words
             where user_id = :userId and (last_date_of_repeat_from_native + repeat_interval_from_native) <= :date
             order by value limit :limit offset :offset;
            """)
    public List<Word> findAllForRepeatFromNative(UUID userId, LocalDate date, long limit, long offset);

}
