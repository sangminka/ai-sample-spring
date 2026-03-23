package com.example.demo.board;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

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
    public String list(@RequestParam(required = false) String page, Model model) {
        var 페이지번호 = 페이지번호파싱하기(page);
        if (페이지번호 == null || 페이지번호 < 1) {
            return "redirect:/board/list?page=1";
        }

        var listResponse = boardService.게시글목록보기(페이지번호);
        model.addAttribute("model", listResponse);
        return "list";
    }

    @GetMapping("/board/{id}")
    public String detail(@PathVariable("id") Integer id,
            @RequestParam(required = false) String page,
            Model model) {
        var detailResponse = boardService.게시글상세보기(id, 페이지번호파싱하기(page));
        model.addAttribute("model", detailResponse);
        return "detail";
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
}
