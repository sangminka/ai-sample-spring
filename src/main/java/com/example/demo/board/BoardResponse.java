package com.example.demo.board;

import java.time.format.DateTimeFormatter;
import java.util.List;

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

    @Data
    public static class Page {
        private Integer currentPage;
        private Integer pageSize;
        private Integer offset;
        private Integer boardCount;
        private List<Min> boardList;

        public Page(Integer currentPage, Integer pageSize, Integer offset, List<Board> boardList) {
            this.currentPage = currentPage;
            this.pageSize = pageSize;
            this.offset = offset;
            this.boardCount = boardList.size();
            this.boardList = boardList.stream()
                    .map(Min::new)
                    .toList();
        }
    }

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
