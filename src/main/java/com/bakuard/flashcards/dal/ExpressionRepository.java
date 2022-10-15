package com.bakuard.flashcards.dal;

import com.bakuard.flashcards.model.expression.Expression;
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
public interface ExpressionRepository extends PagingAndSortingRepository<Expression, UUID> {

    @Query("select * from expressions where user_id = :userId and expression_id = :expressionId;")
    public Optional<Expression> findById(UUID userId, UUID expressionId);

    @Query("""
            select * from expressions
                where user_id = :userId and distance(:value, value, :maxDistance) != -1;
            """)
    public List<Expression> findByValue(UUID userId, String value, int maxDistance, int limit, int offset);

    @Modifying
    @Query("delete from expressions where user_id = :userId and expression_id = :expressionId;")
    public void deleteById(UUID userId, UUID expressionId);

    @Query("select exists(select 1 from expressions where user_id = :userId and expression_id = :expressionId);")
    public boolean existsById(UUID userId, UUID expressionId);

    @Query("select count(*) from expressions where user_id = :userId")
    public long count(UUID userId);

    @Query("""
            select count(*) from expressions
             where user_id = :userId and (last_date_of_repeat + repeat_interval) <= :date;
            """)
    public long countForRepeat(UUID userId, LocalDate date);

    @Query("""
            select count(*) from (
               select value
                   from expressions
                   where user_id = :userId and distance(:value, value, :maxDistance) != -1
            )
            """)
    public long countForValue(UUID userId, String value, int maxDistance);

    public Page<Expression> findByUserId(UUID userId, Pageable pageable);

    @Query("""
            select * from expressions
             where user_id = :userId and (last_date_of_repeat + repeat_interval) <= :date
             order by value limit :limit offset :offset;
            """)
    public List<Expression> findAllForRepeat(UUID userId, LocalDate date, int limit, int offset);

    @Modifying
    @Query("""
            update expressions set repeat_interval = :newInterval
             where repeat_interval = :oldInterval and user_id = :userId;
            """)
    public void replaceRepeatInterval(UUID userId, int oldInterval, int newInterval);

}
