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
    public static class PageNumberResponse {
        private Integer number;
        private boolean current;

        public PageNumberResponse(Integer number, Integer currentPage) {
            this.number = number;
            this.current = number.equals(currentPage);
        }
    }

    @Data
    public static class PagingResponse {
        private Integer currentPage;
        private Integer pageSize;
        private Integer offset;
        private Integer boardCount;
        private Integer totalCount;
        private Integer totalPageCount;
        private boolean hasPrev;
        private Integer prevPage;
        private boolean hasNext;
        private Integer nextPage;
        private List<PageNumberResponse> pageNumbers;

        public PagingResponse(Integer currentPage, Integer pageSize, Integer offset, Integer boardCount,
                Integer totalCount, Integer totalPageCount,
                boolean hasPrev, Integer prevPage,
                boolean hasNext, Integer nextPage,
                List<PageNumberResponse> pageNumbers) {
            this.currentPage = currentPage;
            this.pageSize = pageSize;
            this.offset = offset;
            this.boardCount = boardCount;
            this.totalCount = totalCount;
            this.totalPageCount = totalPageCount;
            this.hasPrev = hasPrev;
            this.prevPage = prevPage;
            this.hasNext = hasNext;
            this.nextPage = nextPage;
            this.pageNumbers = pageNumbers;
        }
    }

    @Data
    public static class ListResponse {
        private List<Min> boards;
        private PagingResponse paging;

        public ListResponse(List<Board> boards, PagingResponse paging) {
            this.boards = boards.stream()
                    .map(Min::new)
                    .toList();
            this.paging = paging;
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

    @Data
    public static class DetailResponse {
        private Detail board;
        private Integer page;

        public DetailResponse(Board board, Integer page) {
            this.board = new Detail(board);
            this.page = page;
        }
    }
}
