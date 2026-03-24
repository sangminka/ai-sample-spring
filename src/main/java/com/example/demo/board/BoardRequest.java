package com.example.demo.board;

import lombok.Data;

public class BoardRequest {

    // RULE: 요청 DTO는 기능명을 적는다.
    @Data
    public static class Search {
        private String page;
        private String keyword;
    }

    // RULE: 요청 DTO는 기능명을 적는다.
    @Data
    public static class Save {
        private String title;
        private String content;
    }
}
