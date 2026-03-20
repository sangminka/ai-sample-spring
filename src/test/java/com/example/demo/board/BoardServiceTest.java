package com.example.demo.board;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo._core.web.exception.Exception400;
import com.example.demo.user.User;

@ExtendWith(MockitoExtension.class)
class BoardServiceTest {

    @Mock
    private BoardRepository boardRepository;

    @InjectMocks
    private BoardService boardService;

    @DisplayName("게시글목록보기는 첫 페이지에서 limit 3 기준 목록과 메타 정보를 반환한다")
    @Test
    void 게시글목록보기_returnsFirstPage() {
        var newestBoard = Board.builder()
                .id(23)
                .title("학습용 게시글 23")
                .content("내용 23")
                .user(User.builder().id(1).username("ssar").build())
                .createdAt(LocalDateTime.of(2026, 3, 20, 10, 30))
                .build();
        var middleBoard = Board.builder()
                .id(22)
                .title("학습용 게시글 22")
                .content("내용 22")
                .user(User.builder().id(2).username("cos").build())
                .createdAt(LocalDateTime.of(2026, 3, 20, 10, 0))
                .build();
        var olderBoard = Board.builder()
                .id(21)
                .title("학습용 게시글 21")
                .content("내용 21")
                .user(User.builder().id(1).username("ssar").build())
                .createdAt(LocalDateTime.of(2026, 3, 20, 9, 30))
                .build();
        when(boardRepository.findByPaging(0)).thenReturn(List.of(newestBoard, middleBoard, olderBoard));

        var boardPage = boardService.게시글목록보기(0);

        assertThat(boardPage.getCurrentPage()).isEqualTo(0);
        assertThat(boardPage.getPageSize()).isEqualTo(3);
        assertThat(boardPage.getOffset()).isEqualTo(0);
        assertThat(boardPage.getBoardCount()).isEqualTo(3);
        assertThat(boardPage.getBoardList()).hasSize(3);
        assertThat(boardPage.getBoardList().getFirst().getId()).isEqualTo(23);
        assertThat(boardPage.getBoardList().getFirst().getTitle()).isEqualTo("학습용 게시글 23");
        assertThat(boardPage.getBoardList().getFirst().getUsername()).isEqualTo("ssar");
    }

    @DisplayName("게시글목록보기는 page 값에 따라 offset을 바꿔 조회한다")
    @Test
    void 게시글목록보기_changesOffsetByPage() {
        var pageOneBoard = Board.builder()
                .id(20)
                .title("학습용 게시글 20")
                .content("내용 20")
                .user(User.builder().id(2).username("cos").build())
                .createdAt(LocalDateTime.of(2026, 3, 20, 9, 0))
                .build();
        when(boardRepository.findByPaging(3)).thenReturn(List.of(pageOneBoard));

        var boardPage = boardService.게시글목록보기(1);

        verify(boardRepository).findByPaging(3);
        assertThat(boardPage.getCurrentPage()).isEqualTo(1);
        assertThat(boardPage.getOffset()).isEqualTo(3);
        assertThat(boardPage.getBoardList()).hasSize(1);
        assertThat(boardPage.getBoardList().getFirst().getId()).isEqualTo(20);
    }

    @DisplayName("게시글상세보기는 단건 게시글을 상세 DTO로 변환한다")
    @Test
    void 게시글상세보기_returnsBoardDetail() {
        var board = Board.builder()
                .id(7)
                .title("상세 제목")
                .content("상세 내용")
                .user(User.builder().id(1).username("ssar").build())
                .createdAt(LocalDateTime.of(2026, 3, 20, 9, 15))
                .build();
        when(boardRepository.findById(7)).thenReturn(Optional.of(board));

        var boardDetail = boardService.게시글상세보기(7);

        assertThat(boardDetail.getId()).isEqualTo(7);
        assertThat(boardDetail.getTitle()).isEqualTo("상세 제목");
        assertThat(boardDetail.getContent()).isEqualTo("상세 내용");
        assertThat(boardDetail.getUsername()).isEqualTo("ssar");
    }

    @DisplayName("없는 게시글을 상세보기하면 예외가 발생한다")
    @Test
    void 게시글상세보기_throwsExceptionWhenBoardNotFound() {
        when(boardRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> boardService.게시글상세보기(999))
                .isInstanceOf(Exception400.class)
                .hasMessage("게시글을 찾을 수 없습니다.");
    }
}
