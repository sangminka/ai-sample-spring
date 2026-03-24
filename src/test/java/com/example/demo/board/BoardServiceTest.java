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

    @DisplayName("게시글목록보기는 첫 페이지에서 게시글 3개와 페이지 번호 5개를 반환한다")
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
        when(boardRepository.countAll()).thenReturn(21L);
        when(boardRepository.findByPaging(0)).thenReturn(List.of(newestBoard, middleBoard, olderBoard));

        var boardPage = boardService.게시글목록보기(검색요청("1", null));

        assertThat(boardPage.getPaging().getCurrentPage()).isEqualTo(1);
        assertThat(boardPage.getPaging().getPageSize()).isEqualTo(3);
        assertThat(boardPage.getPaging().getOffset()).isEqualTo(0);
        assertThat(boardPage.getPaging().getBoardCount()).isEqualTo(3);
        assertThat(boardPage.getPaging().getTotalCount()).isEqualTo(21);
        assertThat(boardPage.getPaging().getTotalPageCount()).isEqualTo(7);
        assertThat(boardPage.getPaging().isHasPrev()).isFalse();
        assertThat(boardPage.getPaging().isHasNext()).isTrue();
        assertThat(boardPage.getPaging().getNextPage()).isEqualTo(2);
        assertThat(boardPage.getPaging().getPageNumbers()).hasSize(5);
        assertThat(boardPage.getPaging().getPageNumbers().getFirst().getNumber()).isEqualTo(1);
        assertThat(boardPage.getPaging().getPageNumbers().getFirst().isCurrent()).isTrue();
        assertThat(boardPage.getPaging().getPageNumbers().getLast().getNumber()).isEqualTo(5);
        assertThat(boardPage.getPaging().getPageNumbers().get(1).isCurrent()).isFalse();
        assertThat(boardPage.getBoards()).hasSize(3);
        assertThat(boardPage.getBoards().getFirst().getId()).isEqualTo(23);
        assertThat(boardPage.getBoards().getFirst().getTitle()).isEqualTo("학습용 게시글 23");
        assertThat(boardPage.getBoards().getFirst().getUsername()).isEqualTo("ssar");
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
        when(boardRepository.countAll()).thenReturn(21L);
        when(boardRepository.findByPaging(3)).thenReturn(List.of(pageOneBoard));

        var boardPage = boardService.게시글목록보기(검색요청("2", null));

        verify(boardRepository).findByPaging(3);
        assertThat(boardPage.getPaging().getCurrentPage()).isEqualTo(2);
        assertThat(boardPage.getPaging().getOffset()).isEqualTo(3);
        assertThat(boardPage.getPaging().getTotalCount()).isEqualTo(21);
        assertThat(boardPage.getPaging().getTotalPageCount()).isEqualTo(7);
        assertThat(boardPage.getPaging().isHasPrev()).isTrue();
        assertThat(boardPage.getPaging().getPrevPage()).isEqualTo(1);
        assertThat(boardPage.getPaging().isHasNext()).isTrue();
        assertThat(boardPage.getPaging().getNextPage()).isEqualTo(3);
        assertThat(boardPage.getPaging().getPageNumbers()).hasSize(5);
        assertThat(boardPage.getPaging().getPageNumbers().get(1).isCurrent()).isTrue();
        assertThat(boardPage.getBoards()).hasSize(1);
        assertThat(boardPage.getBoards().getFirst().getId()).isEqualTo(20);
    }

    @DisplayName("게시글목록보기는 여섯 번째 페이지에서 6번부터 페이지 번호를 노출한다")
    @Test
    void 게시글목록보기_showsSecondPageNumberWindow() {
        var boardA = Board.builder()
                .id(3)
                .title("마지막 페이지 게시글 3")
                .content("내용 3")
                .user(User.builder().id(1).username("ssar").build())
                .createdAt(LocalDateTime.of(2026, 3, 20, 9, 0))
                .build();
        var boardB = Board.builder()
                .id(2)
                .title("마지막 페이지 게시글 2")
                .content("내용 2")
                .user(User.builder().id(2).username("cos").build())
                .createdAt(LocalDateTime.of(2026, 3, 20, 8, 30))
                .build();
        var boardC = Board.builder()
                .id(1)
                .title("마지막 페이지 게시글 1")
                .content("내용 1")
                .user(User.builder().id(1).username("ssar").build())
                .createdAt(LocalDateTime.of(2026, 3, 20, 8, 0))
                .build();
        when(boardRepository.countAll()).thenReturn(21L);
        when(boardRepository.findByPaging(15)).thenReturn(List.of(boardA, boardB, boardC));

        var boardPage = boardService.게시글목록보기(검색요청("6", null));

        assertThat(boardPage.getPaging().getTotalPageCount()).isEqualTo(7);
        assertThat(boardPage.getPaging().isHasPrev()).isTrue();
        assertThat(boardPage.getPaging().getPrevPage()).isEqualTo(5);
        assertThat(boardPage.getPaging().isHasNext()).isTrue();
        assertThat(boardPage.getPaging().getNextPage()).isEqualTo(7);
        assertThat(boardPage.getPaging().getPageNumbers()).hasSize(2);
        assertThat(boardPage.getPaging().getPageNumbers().getFirst().getNumber()).isEqualTo(6);
        assertThat(boardPage.getPaging().getPageNumbers().getFirst().isCurrent()).isTrue();
        assertThat(boardPage.getPaging().getPageNumbers().get(1).getNumber()).isEqualTo(7);
    }

    @DisplayName("게시글목록보기는 마지막 페이지를 넘는 요청을 빈 목록으로 렌더링한다")
    @Test
    void 게시글목록보기_keepsEmptyListWhenPageExceedsLastPage() {
        when(boardRepository.countAll()).thenReturn(21L);
        when(boardRepository.findByPaging(21)).thenReturn(List.of());

        var boardPage = boardService.게시글목록보기(검색요청("8", null));

        assertThat(boardPage.getPaging().getCurrentPage()).isEqualTo(8);
        assertThat(boardPage.getPaging().getTotalPageCount()).isEqualTo(7);
        assertThat(boardPage.getPaging().isHasPrev()).isTrue();
        assertThat(boardPage.getPaging().getPrevPage()).isEqualTo(7);
        assertThat(boardPage.getPaging().isHasNext()).isFalse();
        assertThat(boardPage.getBoards()).isEmpty();
        assertThat(boardPage.getPaging().getPageNumbers()).hasSize(2);
        assertThat(boardPage.getPaging().getPageNumbers().getFirst().getNumber()).isEqualTo(6);
        assertThat(boardPage.getPaging().getPageNumbers()).allMatch(pageNumber -> !pageNumber.isCurrent());
    }

    @DisplayName("게시글목록보기는 trim한 검색어로 제목과 내용을 함께 검색한다")
    @Test
    void 게시글목록보기_searchesByTrimmedKeyword() {
        var titleMatchedBoard = Board.builder()
                .id(30)
                .title("스프링 검색 제목")
                .content("내용 30")
                .user(User.builder().id(1).username("ssar").build())
                .createdAt(LocalDateTime.of(2026, 3, 24, 9, 0))
                .build();
        var contentMatchedBoard = Board.builder()
                .id(29)
                .title("검색되지 않는 제목")
                .content("이 내용에는 스프링 키워드가 포함됩니다.")
                .user(User.builder().id(2).username("cos").build())
                .createdAt(LocalDateTime.of(2026, 3, 24, 8, 30))
                .build();
        when(boardRepository.countByKeyword("스프링")).thenReturn(2L);
        when(boardRepository.findByPagingAndKeyword("스프링", 0)).thenReturn(List.of(titleMatchedBoard, contentMatchedBoard));

        var boardPage = boardService.게시글목록보기(검색요청("1", "  스프링  "));

        verify(boardRepository).countByKeyword("스프링");
        verify(boardRepository).findByPagingAndKeyword("스프링", 0);
        assertThat(boardPage.getKeyword()).isEqualTo("스프링");
        assertThat(boardPage.isHasKeyword()).isTrue();
        assertThat(boardPage.getPaging().getTotalCount()).isEqualTo(2);
        assertThat(boardPage.getBoards()).hasSize(2);
        assertThat(boardPage.getBoards().getFirst().getTitle()).isEqualTo("스프링 검색 제목");
        assertThat(boardPage.getBoards().get(1).getTitle()).isEqualTo("검색되지 않는 제목");
    }

    @DisplayName("게시글목록보기는 공백 검색어를 전체 목록 조회로 처리한다")
    @Test
    void 게시글목록보기_usesAllBoardsWhenKeywordIsBlank() {
        var board = Board.builder()
                .id(11)
                .title("전체 목록 게시글")
                .content("전체 내용")
                .user(User.builder().id(1).username("ssar").build())
                .createdAt(LocalDateTime.of(2026, 3, 24, 7, 0))
                .build();
        when(boardRepository.countAll()).thenReturn(1L);
        when(boardRepository.findByPaging(0)).thenReturn(List.of(board));

        var boardPage = boardService.게시글목록보기(검색요청("1", "   "));

        verify(boardRepository).countAll();
        verify(boardRepository).findByPaging(0);
        assertThat(boardPage.getKeyword()).isEmpty();
        assertThat(boardPage.isHasKeyword()).isFalse();
        assertThat(boardPage.getBoards()).hasSize(1);
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

        var boardDetail = boardService.게시글상세보기(7, 2, "spring");

        assertThat(boardDetail.getBoard().getId()).isEqualTo(7);
        assertThat(boardDetail.getBoard().getTitle()).isEqualTo("상세 제목");
        assertThat(boardDetail.getBoard().getContent()).isEqualTo("상세 내용");
        assertThat(boardDetail.getBoard().getUsername()).isEqualTo("ssar");
        assertThat(boardDetail.getPage()).isEqualTo(2);
        assertThat(boardDetail.getKeyword()).isEqualTo("spring");
    }

    @DisplayName("게시글상세보기는 잘못된 page를 1로 보정한다")
    @Test
    void 게시글상세보기_correctsInvalidPage() {
        var board = Board.builder()
                .id(8)
                .title("보정 제목")
                .content("보정 내용")
                .user(User.builder().id(1).username("ssar").build())
                .createdAt(LocalDateTime.of(2026, 3, 20, 9, 20))
                .build();
        when(boardRepository.findById(8)).thenReturn(Optional.of(board));

        var nullPageDetail = boardService.게시글상세보기(8, null, " spring ");
        var zeroPageDetail = boardService.게시글상세보기(8, 0, null);

        assertThat(nullPageDetail.getPage()).isEqualTo(1);
        assertThat(zeroPageDetail.getPage()).isEqualTo(1);
        assertThat(nullPageDetail.getKeyword()).isEqualTo("spring");
        assertThat(zeroPageDetail.getKeyword()).isEmpty();
    }

    @DisplayName("없는 게시글을 상세보기하면 예외가 발생한다")
    @Test
    void 게시글상세보기_throwsExceptionWhenBoardNotFound() {
        when(boardRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> boardService.게시글상세보기(999, 1, null))
                .isInstanceOf(Exception400.class)
                .hasMessage("게시글을 찾을 수 없습니다.");
    }

    private BoardRequest.Search 검색요청(String page, String keyword) {
        var requestDTO = new BoardRequest.Search();
        requestDTO.setPage(page);
        requestDTO.setKeyword(keyword);
        return requestDTO;
    }
}
