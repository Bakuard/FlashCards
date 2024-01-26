package com.bakuard.flashcards.service;

import com.bakuard.flashcards.dal.IntervalRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Сервис для редактирования списка всех интервалов повторения конкретного пользователя. Каждый метод этого
 * класса выполняется в отдельной транзакции.
 * @see IntervalRepository
 */
@Transactional
public class IntervalService {

    private IntervalRepository intervalRepository;

    /**
     * Создает новый сервис для редактирования списка всех интервалов повторения конкретного пользователя.
     * @param intervalRepository репозиторий интервалов повторения
     */
    public IntervalService(IntervalRepository intervalRepository) {
        this.intervalRepository = intervalRepository;
    }

    /**
     * Делегирует вызов методу {@link IntervalRepository#add(UUID, int)}.
     */
    public void add(UUID userId, int interval) {
        intervalRepository.add(userId, interval);
    }

    /**
     * Делегирует вызов методу {@link IntervalRepository#replace(UUID, int, int)}.
     */
    public void replace(UUID userId, int oldInterval, int newInterval) {
        intervalRepository.replace(userId, oldInterval, newInterval);
    }

    /**
     * Делегирует вызов методу {@link IntervalRepository#findAll(UUID)}.
     */
    public List<Integer> findAll(UUID userId) {
        return intervalRepository.findAll(userId);
    }

    /**
     * Возвращает наименьший из интервалов повторения пользователя userId.
     * @param userId идентификатор пользователя
     * @return наименьший из интервалов повторения пользователя
     */
    public int getLowestInterval(UUID userId) {
        return intervalRepository.findAll(userId).getFirst();
    }

}
