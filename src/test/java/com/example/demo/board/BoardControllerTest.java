package com.example.demo.board;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
class BoardControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private UserRepository userRepository;

    @DisplayName("게시글 목록 첫 페이지는 최신 게시글 3개만 보여준다")
    @Test
    void list_firstPage_success() throws Exception {
        var user = userRepository.findById(1).orElseThrow();
        boardRepository.save(Board.builder().title("step2-page-0-A").content("본문 A").user(user).build());
        var boardB = boardRepository.save(Board.builder().title("step2-page-0-B").content("본문 B").user(user).build());
        var boardC = boardRepository.save(Board.builder().title("step2-page-0-C").content("본문 C").user(user).build());
        var boardD = boardRepository.save(Board.builder().title("step2-page-0-D").content("본문 D").user(user).build());
        var boardE = boardRepository.save(Board.builder().title("step2-page-0-E").content("본문 E").user(user).build());
        var boardF = boardRepository.save(Board.builder().title("step2-page-0-F").content("본문 F").user(user).build());

        mvc.perform(get("/board/list"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("LIMIT 3와 OFFSET으로 페이지가 바뀌는 원리 학습")))
                .andExpect(content().string(containsString("offset = page * limit")))
                .andExpect(content().string(containsString(boardF.getTitle())))
                .andExpect(content().string(containsString(boardE.getTitle())))
                .andExpect(content().string(containsString(boardD.getTitle())))
                .andExpect(content().string(not(containsString(boardC.getTitle()))))
                .andExpect(content().string(containsString("/board/" + boardF.getId() + "?page=0")))
                .andExpect(content().string(not(containsString("/board/" + boardB.getId() + "?page=0"))));
    }

    @DisplayName("게시글 목록은 page 파라미터에 따라 다음 3개를 보여준다")
    @Test
    void list_secondPage_success() throws Exception {
        var user = userRepository.findById(1).orElseThrow();
        var boardA = boardRepository.save(Board.builder().title("step2-page-1-A").content("본문 A").user(user).build());
        var boardB = boardRepository.save(Board.builder().title("step2-page-1-B").content("본문 B").user(user).build());
        var boardC = boardRepository.save(Board.builder().title("step2-page-1-C").content("본문 C").user(user).build());
        boardRepository.save(Board.builder().title("step2-page-1-D").content("본문 D").user(user).build());
        boardRepository.save(Board.builder().title("step2-page-1-E").content("본문 E").user(user).build());
        boardRepository.save(Board.builder().title("step2-page-1-F").content("본문 F").user(user).build());

        mvc.perform(get("/board/list").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(">1<")))
                .andExpect(content().string(containsString(">3<")))
                .andExpect(content().string(containsString(boardC.getTitle())))
                .andExpect(content().string(containsString(boardB.getTitle())))
                .andExpect(content().string(containsString(boardA.getTitle())))
                .andExpect(content().string(not(containsString("step2-page-1-F"))))
                .andExpect(content().string(containsString("/board/" + boardC.getId() + "?page=1")));
    }

    @DisplayName("게시글 상세 페이지는 목록 페이지 번호를 유지한 채 돌아가기 링크를 보여준다")
    @Test
    void detail_success() throws Exception {
        var savedBoard = boardRepository.save(Board.builder()
                .title("상세 테스트용 게시글")
                .content("상세 테스트용 본문")
                .user(userRepository.findById(2).orElseThrow())
                .build());

        mvc.perform(get("/board/" + savedBoard.getId()).param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("상세 테스트용 게시글")))
                .andExpect(content().string(containsString("상세 테스트용 본문")))
                .andExpect(content().string(containsString("/board/list?page=1")));
    }
}
