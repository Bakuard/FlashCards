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
public interface WordsRepository extends PagingAndSortingRepository<Word, UUID> {

    @Query("select * from words where user_id = :userId and word_id = :wordId;")
    public Optional<Word> findById(UUID userId, UUID wordId);

    @Query("""
            select * from words
                where user_id = :userId and distance(:value, value, :maxDistance) != -1;
            """)
    public List<Word> findByValue(UUID userId, String value, int maxDistance, int limit, int offset);

    @Modifying
    @Query("delete from words where user_id = :userId and word_id = :wordId;")
    public void deleteById(UUID userId, UUID wordId);

    @Query("select exists(select 1 from words where user_id = :userId and word_id = :wordId);")
    public boolean existsById(UUID userId, UUID wordId);

    @Query("select count(*) from words where user_id = :userId")
    public long count(UUID userId);

    @Query("""
            select count(*) from words
             where user_id = :userId and (last_date_of_repeat + repeat_interval) <= :date;
            """)
    public long countForRepeat(UUID userId, LocalDate date);

    @Query("""
            select count(*) from (
               select value
                   from words
                   where user_id = :userId and distance(:value, value, :maxDistance) != -1
            )
            """)
    public long countForValue(UUID userId, String value, int maxDistance);

    public Page<Word> findByUserId(UUID userId, Pageable pageable);

    @Query("""
            select * from words
             where user_id = :userId and (last_date_of_repeat + repeat_interval) <= :date
             order by value limit :limit offset :offset;
            """)
    public List<Word> findAllForRepeat(UUID userId, LocalDate date, int limit, int offset);

    @Modifying
    @Query("""
            update words set repeat_interval = :newInterval
             where repeat_interval = :oldInterval and user_id = :userId;
            """)
    public void replaceRepeatInterval(UUID userId, int oldInterval, int newInterval);

}
