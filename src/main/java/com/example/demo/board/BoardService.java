package com.example.demo.board;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo._core.web.exception.Exception400;

import lombok.RequiredArgsConstructor;

/**
 * DTO는 Service에서 만든다. Entity를 Controller에 전달하지 않는다.
 */
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class BoardService {
    private final BoardRepository boardRepository;

    public List<BoardResponse.Min> 게시글목록보기() {
        // Step 1에서는 LIMIT/OFFSET 없이 전체 데이터를 먼저 가져와 목록 구조를 익힌다.
        return boardRepository.findAllByOrderByIdDesc().stream()
                .map(BoardResponse.Min::new)
                .toList();
    }

    public BoardResponse.Detail 게시글상세보기(Integer id) {
        var board = boardRepository.findById(id)
                .orElseThrow(() -> new Exception400("게시글을 찾을 수 없습니다."));

        return new BoardResponse.Detail(board);
    }
}
