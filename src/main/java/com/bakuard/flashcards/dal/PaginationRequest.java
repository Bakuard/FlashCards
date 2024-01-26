package com.bakuard.flashcards.dal;

import com.bakuard.flashcards.config.configData.ConfigData;
import com.bakuard.flashcards.validation.exception.InvalidParameter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.Objects;

public class PaginationRequest implements Pageable {

    public static Pageable toUnsortedPageRequest(int pageNumber,
                                                 int pageSize,
                                                 ConfigData config) {
        return new PaginationRequest(pageNumber, pageSize, Sort.unsorted(), config);
    }

    public static Pageable toUserPageRequest(int pageNumber,
                                             int pageSize,
                                             String sortParams,
                                             ConfigData config) {
        return new PaginationRequest(
                pageNumber,
                pageSize,
                sortParams,
                Sort.by("id").ascending(),
                "id",
                config,
                "id", "email"
        );
    }

    public static Pageable toWordPageRequest(int pageNumber,
                                             int pageSize,
                                             String sortParams,
                                             ConfigData config) {
        return new PaginationRequest(
                pageNumber,
                pageSize,
                sortParams,
                Sort.by("value").ascending(),
                "value",
                config,
                "value",
                "repeat_interval_from_english",
                "last_date_of_repeat_from_english",
                "repeat_interval_from_native",
                "last_date_of_repeat_from_native"
        );
    }

    public static Pageable toExpressionPageRequest(int pageNumber,
                                                   int pageSize,
                                                   String sortParams,
                                                   ConfigData config) {
        return new PaginationRequest(
                pageNumber,
                pageSize,
                sortParams,
                Sort.by("value").ascending(),
                "value",
                config,
                "value",
                "repeat_interval_from_english",
                "last_date_of_repeat_from_english",
                "repeat_interval_from_native",
                "last_date_of_repeat_from_native"
        );
    }

    public static Pageable toWordStatisticsPageRequest(int pageNumber,
                                                       int pageSize,
                                                       String sortParams,
                                                       ConfigData config) {
        return new PaginationRequest(
                pageNumber,
                pageSize,
                sortParams,
                Sort.by("remember_from_english", "value").ascending(),
                "value",
                config,
                "value",
                "remember_from_english",
                "remember_from_native",
                "not_remember_from_english",
                "not_remember_from_native"
        );
    }

    public static Pageable toExpressionStatisticsPageRequest(int pageNumber,
                                                             int pageSize,
                                                             String sortParams,
                                                             ConfigData config) {
        return new PaginationRequest(
                pageNumber,
                pageSize,
                sortParams,
                Sort.by("remember_from_english", "value").ascending(),
                "value",
                config,
                "value",
                "remember_from_english",
                "remember_from_native",
                "not_remember_from_english",
                "not_remember_from_native"
        );
    }


    private final int pageSize;
    private final int pageNumber;
    private final Sort sort;
    private final ConfigData config;

    private PaginationRequest(int pageNumber,
                              int pageSize,
                              String sortParams,
                              Sort defaultSort,
                              String necessaryParam,
                              ConfigData config,
                              String... validParams) {
        this.pageNumber = Math.max(pageNumber, 0);
        this.pageSize = toCorrectPageSize(pageSize, config);
        this.sort = toSort(sortParams, defaultSort, necessaryParam, validParams);
        this.config = config;
    }

    public PaginationRequest(int pageNumber,
                              int pageSize,
                              Sort sort,
                              ConfigData config) {
        this.pageNumber = Math.max(pageNumber, 0);
        this.pageSize = toCorrectPageSize(pageSize, config);
        this.sort = sort;
        this.config = config;
    }

    @Override
    public int getPageNumber() {
        return pageNumber;
    }

    @Override
    public int getPageSize() {
        return pageSize;
    }

    @Override
    public long getOffset() {
        return (long)pageSize * (long)pageNumber;
    }

    @Override
    public Sort getSort() {
        return sort;
    }

    @Override
    public Pageable next() {
        return new PaginationRequest(
                getPageSize(),
                getPageNumber() + 1,
                getSort(),
                config
        );
    }

    @Override
    public Pageable previousOrFirst() {
        return hasPrevious() ?
                new PaginationRequest(pageSize, pageNumber - 1, sort, config) :
                first();
    }

    @Override
    public Pageable first() {
        return new PaginationRequest(pageSize, 0, sort, config);
    }

    @Override
    public Pageable withPage(int pageNumber) {
        return new PaginationRequest(pageSize, pageNumber, sort, config);
    }

    @Override
    public boolean hasPrevious() {
        return pageNumber > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaginationRequest that = (PaginationRequest) o;
        return pageSize == that.pageSize
                && pageNumber == that.pageNumber
                && Objects.equals(sort, that.sort)
                && Objects.equals(config, that.config);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pageSize, pageNumber, sort, config);
    }

    @Override
    public String toString() {
        return "PaginationRequest{" +
                "pageSize=" + pageSize +
                ", pageNumber=" + pageNumber +
                ", sort=" + sort +
                ", config=" + config +
                '}';
    }


    private int toCorrectPageSize(int pageSize, ConfigData config) {
        pageSize = Math.min(pageSize, config.pagination().maxPageSize());
        if(pageSize == 0) pageSize = config.pagination().defaultPageSize();
        return Math.max(config.pagination().minPageSize(), pageSize);
    }

    private void assertIsOneOf(String param, String... validParams) {
        if(Arrays.stream(validParams).noneMatch(p -> p.equalsIgnoreCase(param))) {
            throw new InvalidParameter("Invalid sort parameter '" + param +'\'',
                    "PaginationRequest.invalidParameter");
        }
    }

    private boolean containsNecessary(Sort sort, String necessaryParam) {
        return sort.stream().anyMatch(order -> order.getProperty().equalsIgnoreCase(necessaryParam));
    }

    private Sort.Direction toSortDirection(String sortDirection) {
        Sort.Direction result = null;

        switch(StringUtils.normalizeSpace(sortDirection).toUpperCase()) {
            case "ASC", "ASCENDING" -> result = Sort.Direction.ASC;
            case "DESC", "DESCENDING" -> result = Sort.Direction.DESC;
            default -> throw new InvalidParameter("Unsupported sort direction '" + sortDirection + '\'',
                    "PaginationRequest.unknownSortDirection");
        }

        return result;
    }

    private Sort toSort(String sortParams,
                        Sort defaultSort,
                        String necessaryParam,
                        String... validParams) {
        return sortParams == null || sortParams.isBlank() ?
                defaultSort :
                Arrays.stream(sortParams.split(","))
                        .map(String::trim)
                        .map(pair -> {
                            String[] order = pair.split("\\.");
                            Sort.Direction direction = order.length == 1 ?
                                    Sort.Direction.ASC :
                                    toSortDirection(order[1]);
                            assertIsOneOf(order[0], validParams);
                            return Sort.by(direction, order[0]);
                        })
                        .reduce(Sort::and)
                        .map(sort -> {
                            if(!containsNecessary(sort, necessaryParam)) {
                                sort = sort.and(Sort.by(necessaryParam).ascending());
                            }
                            return sort;
                        })
                        .orElseThrow();
    }
}
