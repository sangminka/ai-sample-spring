package com.example.demo.board;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo._core.utils.Resp;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
public class BoardApiController {

    private final BoardService boardService;

    @GetMapping("/api/board/search")
    public Object search(BoardRequest.Search requestDTO) {
        return Resp.ok(boardService.게시글목록보기(requestDTO));
    }
}
