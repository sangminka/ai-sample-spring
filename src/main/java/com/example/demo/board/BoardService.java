package com.example.demo.board;

import java.util.List;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo._core.web.exception.Exception400;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class BoardService {
    // 한 페이지에 보여줄 게시글 개수
    private static final int PAGE_SIZE = 3;
    // 화면에 노출할 페이지 번호 개수
    private static final int PAGE_NUMBER_SIZE = 5;

    private final BoardRepository boardRepository;

    public BoardResponse.ListResponse 게시글목록보기(BoardRequest.Search requestDTO) {
        var 검색어 = 키워드정리하기(requestDTO.getKeyword());
        int 보정페이지 = 페이지보정하기(페이지번호파싱하기(requestDTO.getPage()));
        int offset = (보정페이지 - 1) * PAGE_SIZE;
        int totalCount = 전체개수구하기(검색어);
        int totalPageCount = 총페이지수구하기(totalCount);
        var boards = 게시글목록조회하기(검색어, offset);
        int boardCount = boards.size();
        boolean hasPrev = 이전페이지있음(보정페이지, totalPageCount);
        int prevPage = 이전페이지구하기(보정페이지, totalPageCount);
        boolean hasNext = 다음페이지있음(보정페이지, totalPageCount);
        int nextPage = 다음페이지구하기(보정페이지);
        var pageNumbers = 페이지번호목록구하기(totalPageCount, 보정페이지);
        var paging = new BoardResponse.PagingResponse(
                보정페이지,
                PAGE_SIZE,
                offset,
                boardCount,
                totalCount,
                totalPageCount,
                hasPrev,
                prevPage,
                hasNext,
                nextPage,
                pageNumbers);

        return new BoardResponse.ListResponse(boards, paging, 검색어);
    }

    public BoardResponse.DetailResponse 게시글상세보기(Integer id, Integer page, String keyword) {
        var board = boardRepository.findById(id)
                .orElseThrow(() -> new Exception400("게시글을 찾을 수 없습니다."));

        return new BoardResponse.DetailResponse(board, 페이지보정하기(page), 키워드정리하기(keyword));
    }

    private List<Board> 게시글목록조회하기(String keyword, int offset) {
        if (keyword == null) {
            return boardRepository.findByPaging(offset);
        }

        return boardRepository.findByPagingAndKeyword(keyword, offset);
    }

    private int 전체개수구하기(String keyword) {
        if (keyword == null) {
            return boardRepository.countAll().intValue();
        }

        return boardRepository.countByKeyword(keyword).intValue();
    }

    private Integer 페이지번호파싱하기(String page) {
        if (page == null || page.isBlank()) {
            return null;
        }

        try {
            return Integer.parseInt(page);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String 키워드정리하기(String keyword) {
        if (keyword == null) {
            return null;
        }

        var trimmedKeyword = keyword.trim();
        if (trimmedKeyword.isBlank()) {
            return null;
        }

        return trimmedKeyword;
    }

    private int 페이지보정하기(Integer page) {
        if (page == null || page < 1) {
            return 1;
        }
        return page;
    }

    private int 총페이지수구하기(int totalCount) {
        if (totalCount == 0) {
            return 0;
        }
        // 총페이지수 구함
        return (totalCount + PAGE_SIZE - 1) / PAGE_SIZE;
    }

    private List<BoardResponse.PageNumberResponse> 페이지번호목록구하기(int totalPageCount, int currentPage) {
        if (totalPageCount == 0) {
            return List.of();
        }

        int 기준페이지 = 페이지번호기준구하기(totalPageCount, currentPage);
        int startPage = ((기준페이지 - 1) / PAGE_NUMBER_SIZE) * PAGE_NUMBER_SIZE + 1;
        int endPage = Math.min(startPage + PAGE_NUMBER_SIZE - 1, totalPageCount);

        return IntStream.rangeClosed(startPage, endPage)
                .mapToObj(number -> new BoardResponse.PageNumberResponse(number, currentPage))
                .toList();
    }

    private int 페이지번호기준구하기(int totalPageCount, int currentPage) {
        if (currentPage < 1) {
            return 1;
        }

        if (currentPage > totalPageCount) {
            return totalPageCount;
        }

        return currentPage;
    }

    private boolean 이전페이지있음(int currentPage, int totalPageCount) {
        return totalPageCount > 0 && currentPage > 1;
    }

    private int 이전페이지구하기(int currentPage, int totalPageCount) {
        if (currentPage <= 1 || totalPageCount == 0) {
            return 1;
        }

        if (currentPage > totalPageCount) {
            return totalPageCount;
        }

        return currentPage - 1;
    }

    private boolean 다음페이지있음(int currentPage, int totalPageCount) {
        return totalPageCount > 0 && currentPage < totalPageCount;
    }

    private int 다음페이지구하기(int currentPage) {
        return currentPage + 1;
    }
}
