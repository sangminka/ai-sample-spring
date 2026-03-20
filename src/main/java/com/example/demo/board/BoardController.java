package com.example.demo.board;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

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
    public String list(@RequestParam(defaultValue = "0") Integer page, Model model) {
        var boardPage = boardService.게시글목록보기(page);
        model.addAttribute("boardList", boardPage.getBoardList());
        model.addAttribute("currentPage", boardPage.getCurrentPage());
        model.addAttribute("pageSize", boardPage.getPageSize());
        model.addAttribute("offset", boardPage.getOffset());
        model.addAttribute("boardCount", boardPage.getBoardCount());
        return "list";
    }

    @GetMapping("/board/{id}")
    public String detail(@PathVariable("id") Integer id,
            @RequestParam(defaultValue = "0") Integer page,
            Model model) {
        model.addAttribute("board", boardService.게시글상세보기(id));
        model.addAttribute("page", page < 0 ? 0 : page);
        return "detail";
    }
}
