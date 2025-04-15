package com.calendar.infra.provided.search.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchFilter {
    private List<FilterRequest> filters;
    private PageRequest pageRequest;
    private SortRequest sortRequest;
}