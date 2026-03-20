package com.example.demo.board;

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

    public BoardResponse.Page 게시글목록보기(Integer page) {
        // 페이지 보정하기
        int 보정페이지 = 페이지보정하기(page);
        // 시작 인덱스 계산하기
        int offset = 보정페이지 * PAGE_SIZE;

        return new BoardResponse.Page(보정페이지, PAGE_SIZE, offset, boardRepository.findByPaging(offset));
    }

    public BoardResponse.Detail 게시글상세보기(Integer id) {
        var board = boardRepository.findById(id)
                .orElseThrow(() -> new Exception400("게시글을 찾을 수 없습니다."));

        return new BoardResponse.Detail(board);
    }

    private int 페이지보정하기(Integer page) {
        if (page == null || page < 0) {
            return 0;
        }
        return page;
    }
}
