package com.example.demo.board;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.user.UserRepository;

@AutoConfigureMockMvc
@SpringBootTest
class BoardControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private UserRepository userRepository;

    @DisplayName("게시글 목록 페이지는 한 줄형 목록과 상세 링크를 렌더링한다")
    @Test
    void list_success() throws Exception {
        var savedBoard = boardRepository.save(Board.builder()
                .title("목록 테스트용 게시글")
                .content("목록 테스트용 본문")
                .user(userRepository.findById(1).orElseThrow())
                .build());

        mvc.perform(get("/board/list"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("게시글 전체 목록")))
                .andExpect(content().string(containsString("목록 테스트용 게시글")))
                .andExpect(content().string(containsString("/board/" + savedBoard.getId())));
    }

    @DisplayName("게시글 상세 페이지는 제목과 본문을 렌더링한다")
    @Test
    void detail_success() throws Exception {
        var savedBoard = boardRepository.save(Board.builder()
                .title("상세 테스트용 게시글")
                .content("상세 테스트용 본문")
                .user(userRepository.findById(2).orElseThrow())
                .build());

        mvc.perform(get("/board/" + savedBoard.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("상세 테스트용 게시글")))
                .andExpect(content().string(containsString("상세 테스트용 본문")))
                .andExpect(content().string(containsString("목록으로 돌아가기")));
    }
}
