package com.example.demo.board;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.ui.Model;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class BoardController {

    private final BoardService boardService;
    private final HttpSession session;

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/board/list")
    public String list(Model model) {
        model.addAttribute("boardList", boardService.게시글목록보기());
        return "list";
    }

    @GetMapping("/board/{id}")
    public String detail(@PathVariable("id") Integer id, Model model) {
        model.addAttribute("board", boardService.게시글상세보기(id));
        return "detail";
    }
}
