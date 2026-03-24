package com.example.demo.board;

import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriUtils;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class BoardController {

    private final BoardService boardService;

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/board/list")
    public String list(BoardRequest.Search requestDTO, Model model) {
        var 페이지번호 = 페이지번호파싱하기(requestDTO.getPage());
        if (페이지번호 == null || 페이지번호 < 1) {
            return 목록첫페이지주소(requestDTO.getKeyword());
        }

        var listResponse = boardService.게시글목록보기(requestDTO);
        model.addAttribute("model", listResponse);
        return "list";
    }

    @GetMapping("/board/{id}")
    public String detail(@PathVariable("id") Integer id,
            @RequestParam(required = false) String page,
            @RequestParam(required = false) String keyword,
            Model model) {
        var detailResponse = boardService.게시글상세보기(id, 페이지번호파싱하기(page), keyword);
        model.addAttribute("model", detailResponse);
        return "detail";
    }

    private String 목록첫페이지주소(String keyword) {
        var 정리키워드 = 키워드정리하기(keyword);
        if (정리키워드 == null) {
            return "redirect:/board/list?page=1";
        }

        return "redirect:/board/list?page=1&keyword=" + UriUtils.encode(정리키워드, StandardCharsets.UTF_8);
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
}
