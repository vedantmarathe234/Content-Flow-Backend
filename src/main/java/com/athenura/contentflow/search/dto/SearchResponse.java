package com.athenura.contentflow.search.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
public class SearchResponse {
    private List<SearchResultItem> users;
    private List<SearchResultItem> teams;
    private List<SearchResultItem> departments;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SearchResultItem {
        private Long id;
        private String name;
        private Long departmentId;


        public SearchResultItem(Long id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}