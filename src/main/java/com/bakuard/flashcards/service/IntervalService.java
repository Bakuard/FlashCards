package com.bakuard.flashcards.service;

import com.bakuard.flashcards.dal.IntervalRepository;
import com.google.common.collect.ImmutableList;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Сервис для редактирования списка всех интервалов повторения конкретного пользователя. <br/><br/>
 * Интервал повторения - это кол-во дней через которое нужно будет повторить слово(или устойчивое выражение)
 * с момента его последнего повторения. У пользователя, как правило, есть несколько интервалов повторений
 * разной длины. После каждого успешного повторения слова или устойчивого выражения - выбирается следующий
 * интервал повторения, который больше текущего (если таких несколько, то выбирается наименьший из них). <br/>
 * Например: <br/>
 * 1. У пользователя есть интервалы повторения 1, 3, 5 и 11. <br/>
 * 2. Также у пользователя есть слово wordA, текущий интервал повторения которого равен 3. <br/>
 * В случае успешного повторения слова wordA, в качестве нового текущего интервала повторения будет взято
 * значение 5.
 */
@Transactional
public class IntervalService {

    private IntervalRepository intervalRepository;

    public IntervalService(IntervalRepository intervalRepository) {
        this.intervalRepository = intervalRepository;
    }

    public void add(UUID userId, int interval) {
        intervalRepository.add(userId, interval);
    }

    public void replace(UUID userId, int oldInterval, int newInterval) {
        intervalRepository.replace(userId, oldInterval, newInterval);
    }

    public ImmutableList<Integer> findAll(UUID userId) {
        return intervalRepository.findAll(userId);
    }

    public int getLowestInterval(UUID userId) {
        return intervalRepository.findAll(userId).get(0);
    }

}
