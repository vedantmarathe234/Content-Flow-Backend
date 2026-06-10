package com.athenura.contentflow.search.controller;

import com.athenura.contentflow.search.dto.SearchResponse;
import com.athenura.contentflow.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public SearchResponse search(
            @RequestParam String query
    ) {
        return searchService.search(query);
    }
}
