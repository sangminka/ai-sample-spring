package com.example.demo.board;

import java.time.format.DateTimeFormatter;

import lombok.Data;

public class BoardResponse {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Data
    public static class Min {
        private Integer id;
        private String title;
        private String username;
        private String createdAtText;

        public Min(Board board) {
            this.id = board.getId();
            this.title = board.getTitle();
            this.username = board.getUser().getUsername();
            this.createdAtText = board.getCreatedAt().format(DATE_TIME_FORMATTER);
        }
    }

    // RULE: Detail DTO는 상세 정보를 저장한다.
    @Data
    public static class Detail {
        private Integer id;
        private String title;
        private String content;
        private String username;
        private String createdAtText;

        public Detail(Board board) {
            this.id = board.getId();
            this.title = board.getTitle();
            this.content = board.getContent();
            this.username = board.getUser().getUsername();
            this.createdAtText = board.getCreatedAt().format(DATE_TIME_FORMATTER);
        }
    }
}
