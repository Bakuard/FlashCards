package com.bakuard.flashcards.config;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public class MutableClock extends Clock {

    private Instant instant;
    private ZoneId zoneId;

    public MutableClock(int years, int month, int days) {
        this.zoneId = ZoneId.of("America/Phoenix");
        setDate(years, month, days);
    }

    public void setDate(int years, int month, int days) {
        this.instant = LocalDate.of(years, month, days).atStartOfDay(zoneId).toInstant();
    }

    @Override
    public ZoneId getZone() {
        return zoneId;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Instant instant() {
        return instant;
    }

}
