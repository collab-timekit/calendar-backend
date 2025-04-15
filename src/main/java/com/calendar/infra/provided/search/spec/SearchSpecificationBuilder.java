package com.calendar.infra.provided.search.spec;

import com.calendar.infra.provided.search.model.SearchFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

@RequiredArgsConstructor
public class SearchSpecificationBuilder<T> {
    private final SearchFilter searchFilter;

    public Specification<T> buildSpecification() {
        return new SearchSpecification<>(searchFilter);
    }

    public PageRequest buildPageRequest() {
        var sort = Sort.unsorted();

        if (searchFilter.getSortRequest() != null) {
            Sort.Direction direction = Sort.Direction.fromString(searchFilter.getSortRequest().getDirection());
            sort = Sort.by(direction, searchFilter.getSortRequest().getField());
        }

        if (searchFilter.getPageRequest() == null) {
            return PageRequest.of(0, 10, sort);
        }

        return PageRequest.of(
                searchFilter.getPageRequest().getPage(),
                searchFilter.getPageRequest().getSize(),
                sort
        );
    }
}