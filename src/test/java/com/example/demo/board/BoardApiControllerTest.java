package com.example.demo.board;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.user.UserRepository;

@AutoConfigureMockMvc
@SpringBootTest
@Transactional
class BoardApiControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private UserRepository userRepository;

    @DisplayName("게시글 검색 API는 trim한 검색어로 제목과 내용을 함께 조회한다")
    @Test
    void search_success() throws Exception {
        var user = userRepository.findById(1).orElseThrow();
        var titleMatchedBoard = boardRepository
                .save(Board.builder().title("phase2-api-title").content("일반 본문").user(user).build());
        var contentMatchedBoard = boardRepository
                .save(Board.builder().title("일반 제목").content("phase2-api-content 포함").user(user).build());

        mvc.perform(get("/api/board/search").param("page", "1").param("keyword", " phase2-api "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.body.keyword").value("phase2-api"))
                .andExpect(jsonPath("$.body.hasKeyword").value(true))
                .andExpect(jsonPath("$.body.paging.currentPage").value(1))
                .andExpect(jsonPath("$.body.paging.totalCount").value(2))
                .andExpect(jsonPath("$.body.boards[0].id").value(contentMatchedBoard.getId()))
                .andExpect(jsonPath("$.body.boards[1].id").value(titleMatchedBoard.getId()));
    }

    @DisplayName("게시글 검색 API는 빈 검색어를 전체 목록 조회로 처리한다")
    @Test
    void search_blankKeyword_returnsAllBoards() throws Exception {
        var user = userRepository.findById(1).orElseThrow();
        boardRepository.save(Board.builder().title("phase2-all-board").content("전체 목록 본문").user(user).build());

        mvc.perform(get("/api/board/search").param("page", "1").param("keyword", "   "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.body.hasKeyword").value(false))
                .andExpect(jsonPath("$.body.keyword").value(""));
    }
}
