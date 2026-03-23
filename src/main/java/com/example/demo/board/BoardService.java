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
    // 한 페이지에 보여줄 개수수
    private static final int PAGE_SIZE = 3;

    private final BoardRepository boardRepository;

    public BoardResponse.ListResponse 게시글목록보기(Integer page) {
        // 페이지 보정하기
        int 보정페이지 = 페이지보정하기(page);
        // 시작 인덱스 계산하기
        int offset = (보정페이지 - 1) * PAGE_SIZE;
        // 전체 게시글 개수 조회하기
        int totalCount = boardRepository.countAll().intValue();
        // 총 페이지 수 계산하기
        int totalPageCount = 총페이지수구하기(totalCount);
        var boards = boardRepository.findByPaging(offset);
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

        return new BoardResponse.ListResponse(boards, paging);
    }

    public BoardResponse.DetailResponse 게시글상세보기(Integer id, Integer page) {
        var board = boardRepository.findById(id)
                .orElseThrow(() -> new Exception400("게시글을 찾을 수 없습니다."));

        return new BoardResponse.DetailResponse(board, 페이지보정하기(page));
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
        return (totalCount + PAGE_SIZE - 1) / PAGE_SIZE;
    }

    private List<BoardResponse.PageNumberResponse> 페이지번호목록구하기(int totalPageCount, int currentPage) {
        if (totalPageCount == 0) {
            return List.of();
        }

        return IntStream.rangeClosed(1, totalPageCount)
                .mapToObj(number -> new BoardResponse.PageNumberResponse(number, currentPage))
                .toList();
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
