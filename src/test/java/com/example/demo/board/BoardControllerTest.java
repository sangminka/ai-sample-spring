package com.example.demo.board;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
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

    @DisplayName("게시글 목록 요청에 page가 없으면 정식 첫 페이지 주소로 이동한다")
    @Test
    void list_redirectsWhenPageMissing() throws Exception {
        mvc.perform(get("/board/list"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/board/list?page=1"));
    }

    @DisplayName("게시글 목록 요청에 숫자가 아닌 page가 오면 첫 페이지로 이동한다")
    @Test
    void list_redirectsWhenPageIsNotNumber() throws Exception {
        mvc.perform(get("/board/list").param("page", "abc"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/board/list?page=1"));
    }

    @DisplayName("게시글 목록 첫 페이지는 최신 게시글 3개와 전체 페이지 번호 목록을 보여준다")
    @Test
    void list_firstPage_success() throws Exception {
        var user = userRepository.findById(1).orElseThrow();
        boardRepository.save(Board.builder().title("step3-page-1-A").content("본문 A").user(user).build());
        var boardB = boardRepository.save(Board.builder().title("step3-page-1-B").content("본문 B").user(user).build());
        var boardC = boardRepository.save(Board.builder().title("step3-page-1-C").content("본문 C").user(user).build());
        var boardD = boardRepository.save(Board.builder().title("step3-page-1-D").content("본문 D").user(user).build());
        var boardE = boardRepository.save(Board.builder().title("step3-page-1-E").content("본문 E").user(user).build());
        var boardF = boardRepository.save(Board.builder().title("step3-page-1-F").content("본문 F").user(user).build());
        long totalPages = (boardRepository.count() + 2) / 3;

        mvc.perform(get("/board/list").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("COUNT(*)와 전체 개수로 총 페이지 수를 계산하는 페이징 학습")))
                .andExpect(content().string(containsString("COUNT(*)")))
                .andExpect(content().string(containsString(boardF.getTitle())))
                .andExpect(content().string(containsString(boardE.getTitle())))
                .andExpect(content().string(containsString(boardD.getTitle())))
                .andExpect(content().string(not(containsString(boardC.getTitle()))))
                .andExpect(content().string(containsString("/board/" + boardF.getId() + "?page=1")))
                .andExpect(content().string(containsString("value=\"2\"")))
                .andExpect(content().string(containsString("page-number page-number--active\">1</span>")))
                .andExpect(content().string(containsString("href=\"/board/list?page=" + totalPages + "\"")))
                .andExpect(content().string(not(containsString("fa-arrow-left"))))
                .andExpect(content().string(not(containsString("/board/" + boardB.getId() + "?page=1"))));
    }

    @DisplayName("게시글 목록은 page 파라미터에 따라 페이지 번호 강조와 이전 버튼을 보여준다")
    @Test
    void list_secondPage_success() throws Exception {
        var user = userRepository.findById(1).orElseThrow();
        var boardA = boardRepository.save(Board.builder().title("step3-page-2-A").content("본문 A").user(user).build());
        var boardB = boardRepository.save(Board.builder().title("step3-page-2-B").content("본문 B").user(user).build());
        var boardC = boardRepository.save(Board.builder().title("step3-page-2-C").content("본문 C").user(user).build());
        boardRepository.save(Board.builder().title("step3-page-2-D").content("본문 D").user(user).build());
        boardRepository.save(Board.builder().title("step3-page-2-E").content("본문 E").user(user).build());
        boardRepository.save(Board.builder().title("step3-page-2-F").content("본문 F").user(user).build());
        long totalPages = (boardRepository.count() + 2) / 3;

        mvc.perform(get("/board/list").param("page", "2"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("page-number page-number--active\">2</span>")))
                .andExpect(content().string(containsString(boardC.getTitle())))
                .andExpect(content().string(containsString(boardB.getTitle())))
                .andExpect(content().string(containsString(boardA.getTitle())))
                .andExpect(content().string(not(containsString("step3-page-2-F"))))
                .andExpect(content().string(containsString("/board/" + boardC.getId() + "?page=2")))
                .andExpect(content().string(containsString("value=\"1\"")))
                .andExpect(content().string(containsString("value=\"3\"")))
                .andExpect(content().string(containsString("href=\"/board/list?page=3\"")))
                .andExpect(content().string(containsString("href=\"/board/list?page=" + totalPages + "\"")));
    }

    @DisplayName("게시글 목록 요청에 0 이하 page가 오면 첫 페이지로 이동한다")
    @Test
    void list_redirectsWhenPageIsLessThanOne() throws Exception {
        mvc.perform(get("/board/list").param("page", "0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/board/list?page=1"));
    }

    @DisplayName("게시글 목록은 마지막 페이지를 넘는 요청에 빈 목록과 페이지 번호 목록을 보여준다")
    @Test
    void list_outOfRangePage_rendersEmptyList() throws Exception {
        long totalPages = (boardRepository.count() + 2) / 3;
        long outOfRangePage = totalPages + 1;

        mvc.perform(get("/board/list").param("page", String.valueOf(outOfRangePage)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("이 페이지에는 게시글이 없습니다.")))
                .andExpect(content().string(containsString("href=\"/board/list?page=" + totalPages + "\"")))
                .andExpect(content().string(not(containsString("page-number page-number--active"))));
    }

    @DisplayName("게시글 상세 페이지는 목록 페이지 번호를 유지한 채 돌아가기 링크를 보여준다")
    @Test
    void detail_success() throws Exception {
        var savedBoard = boardRepository.save(Board.builder()
                .title("상세 테스트용 게시글")
                .content("상세 테스트용 본문")
                .user(userRepository.findById(2).orElseThrow())
                .build());

        mvc.perform(get("/board/" + savedBoard.getId()).param("page", "2"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("상세 테스트용 게시글")))
                .andExpect(content().string(containsString("상세 테스트용 본문")))
                .andExpect(content().string(containsString("/board/list?page=2")));
    }

    @DisplayName("게시글 상세 페이지는 잘못된 page가 와도 첫 페이지 복귀 링크를 사용한다")
    @Test
    void detail_fallsBackToFirstPageWhenPageInvalid() throws Exception {
        var savedBoard = boardRepository.save(Board.builder()
                .title("상세 잘못된 페이지 테스트")
                .content("상세 잘못된 페이지 본문")
                .user(userRepository.findById(2).orElseThrow())
                .build());

        mvc.perform(get("/board/" + savedBoard.getId()).param("page", "abc"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("상세 잘못된 페이지 테스트")))
                .andExpect(content().string(containsString("/board/list?page=1")));
    }
}
