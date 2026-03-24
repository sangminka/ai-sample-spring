document.addEventListener("DOMContentLoaded", () => {
    const DEBOUNCE_DELAY = 300;
    const searchInput = document.querySelector("#keyword");
    const searchResults = document.querySelector("#board-search-results");
    const loadingMessage = document.querySelector("#board-search-loading");
    const errorMessage = document.querySelector("#board-search-error");
    let debounceTimerId = null;
    let activeController = null;
    let requestSequence = 0;

    if (!searchInput || !searchResults || !loadingMessage || !errorMessage) {
        return;
    }

    const apiUrl = searchResults.dataset.apiUrl;

    searchInput.addEventListener("input", (event) => {
        const keyword = event.target.value;
        requestSequence += 1;
        clearPendingSearch();
        hideError();
        hideLoading();

        const currentRequestId = requestSequence;
        debounceTimerId = window.setTimeout(() => {
            debounceTimerId = null;
            void performSearch(currentRequestId, keyword);
        }, DEBOUNCE_DELAY);
    });

    function clearPendingSearch() {
        if (debounceTimerId !== null) {
            window.clearTimeout(debounceTimerId);
            debounceTimerId = null;
        }

        if (activeController !== null) {
            activeController.abort();
            activeController = null;
        }
    }

    async function performSearch(requestId, keyword) {
        const requestController = new AbortController();
        activeController = requestController;
        showLoading();

        try {
            const response = await fetch(buildApiUrl(apiUrl, keyword), {
                method: "GET",
                headers: {
                    "Accept": "application/json",
                    "X-Requested-With": "XMLHttpRequest"
                },
                signal: requestController.signal
            });

            const payload = await response.json().catch(() => ({}));
            if (!response.ok) {
                throw new Error(payload.msg || "검색 요청에 실패했습니다.");
            }

            if (requestId !== requestSequence) {
                return;
            }

            const model = payload.body;
            searchInput.value = model.keyword;
            searchResults.innerHTML = renderSearchResults(model);
            history.replaceState(null, "", buildListUrl(1, model.keyword));
        } catch (error) {
            if (error.name === "AbortError") {
                return;
            }

            if (requestId !== requestSequence) {
                return;
            }

            showError(error.message || "검색 요청에 실패했습니다.");
        } finally {
            if (activeController === requestController) {
                activeController = null;
            }

            if (requestId === requestSequence) {
                hideLoading();
            }
        }
    }

    function renderSearchResults(model) {
        return `
            ${renderSummary(model)}
            ${renderBoardList(model)}
        `;
    }

    function renderSummary(model) {
        return `
            <section class="board-page__summary" aria-label="pagination summary">
                <div class="board-page__chip">
                    <span>page</span>
                    <strong>${escapeHtml(String(model.paging.currentPage))}</strong>
                </div>
                <div class="board-page__chip">
                    <span>limit</span>
                    <strong>${escapeHtml(String(model.paging.pageSize))}</strong>
                </div>
                <div class="board-page__chip">
                    <span>offset</span>
                    <strong>${escapeHtml(String(model.paging.offset))}</strong>
                </div>
                <div class="board-page__chip">
                    <span>rows</span>
                    <strong>${escapeHtml(String(model.paging.boardCount))}</strong>
                </div>
                <div class="board-page__chip">
                    <span>total</span>
                    <strong>${escapeHtml(String(model.paging.totalCount))}</strong>
                </div>
                <div class="board-page__chip">
                    <span>pages</span>
                    <strong>${escapeHtml(String(model.paging.totalPageCount))}</strong>
                </div>
                ${model.hasKeyword ? `
                <div class="board-page__chip">
                    <span>keyword</span>
                    <strong>${escapeHtml(model.keyword)}</strong>
                </div>
                ` : ""}
            </section>
        `;
    }

    function renderBoardList(model) {
        return `
            <section class="board-list-card" aria-labelledby="board-list-title">
                <div class="board-list-card__header">
                    <h2 id="board-list-title">현재 페이지 게시글과 최대 5개 페이지 번호 목록</h2>
                    <p>
                        이제 목록 아래에서 현재 구간의 페이지 번호를 최대 5개까지 확인할 수 있습니다.
                        현재 페이지는 강조해서 보여주고, 다른 페이지 번호를 누르면 해당 목록으로 이동합니다.
                        \`이전\`과 \`다음\` 버튼으로 다른 페이지로 이동하고, 마지막 페이지를 넘는 주소는 강제로 되돌리지 않고 빈 목록으로 렌더링합니다.
                    </p>
                    <div class="board-page__formula">
                        <span>total pages = ceil(total count / page size)</span>
                        <strong>${escapeHtml(String(model.paging.totalPageCount))} = ceil(${escapeHtml(String(model.paging.totalCount))} / ${escapeHtml(String(model.paging.pageSize))})</strong>
                    </div>
                </div>

                <div class="board-list-table" role="list">
                    <div class="board-list-table__head" aria-hidden="true">
                        <span>번호</span>
                        <span>제목</span>
                        <span>작성자</span>
                        <span>작성일</span>
                    </div>
                    ${renderBoardLines(model)}
                </div>

                <div class="board-pagination">
                    <div class="board-pagination__status">
                        <span>현재 페이지</span>
                        <strong>${escapeHtml(String(model.paging.currentPage))}</strong>
                    </div>
                    <div class="board-pagination__actions">
                        ${renderPrevForm(model)}
                        ${renderNextForm(model)}
                    </div>
                    <div class="board-pagination__numbers" aria-label="페이지 번호 목록">
                        ${renderPageNumbers(model)}
                    </div>
                </div>
            </section>
        `;
    }

    function renderBoardLines(model) {
        if (model.boards.length === 0) {
            if (model.hasKeyword) {
                return `
                    <div class="board-line board-line--empty">
                        <span>'${escapeHtml(model.keyword)}' 검색 결과가 없습니다.</span>
                    </div>
                `;
            }

            return `
                <div class="board-line board-line--empty">
                    <span>이 페이지에는 게시글이 없습니다.</span>
                </div>
            `;
        }

        return model.boards.map((board) => `
            <a class="board-line" role="listitem" href="${buildDetailUrl(board.id, model.paging.currentPage, model.keyword)}">
                <span class="board-line__id">#${escapeHtml(String(board.id))}</span>
                <span class="board-line__title">${escapeHtml(board.title)}</span>
                <span class="board-line__author">${escapeHtml(board.username)}</span>
                <span class="board-line__date">${escapeHtml(board.createdAtText)}</span>
            </a>
        `).join("");
    }

    function renderPrevForm(model) {
        if (!model.paging.hasPrev) {
            return "";
        }

        return `
            <form method="get" action="/board/list" class="board-pagination__form">
                <input type="hidden" name="page" value="${escapeAttribute(String(model.paging.prevPage))}">
                ${renderKeywordHiddenInput(model)}
                <button class="button button--secondary" type="submit">
                    <i class="fa-solid fa-arrow-left"></i>
                    이전
                </button>
            </form>
        `;
    }

    function renderNextForm(model) {
        if (!model.paging.hasNext) {
            return "";
        }

        return `
            <form method="get" action="/board/list" class="board-pagination__form">
                <input type="hidden" name="page" value="${escapeAttribute(String(model.paging.nextPage))}">
                ${renderKeywordHiddenInput(model)}
                <button class="button" type="submit">
                    다음
                    <i class="fa-solid fa-arrow-right"></i>
                </button>
            </form>
        `;
    }

    function renderKeywordHiddenInput(model) {
        if (!model.hasKeyword) {
            return "";
        }

        return `<input type="hidden" name="keyword" value="${escapeAttribute(model.keyword)}">`;
    }

    function renderPageNumbers(model) {
        return model.paging.pageNumbers.map((pageNumber) => {
            if (pageNumber.current) {
                return `<span class="page-number page-number--active">${escapeHtml(String(pageNumber.number))}</span>`;
            }

            return `
                <a href="${buildListUrl(pageNumber.number, model.keyword)}" class="page-number">
                    ${escapeHtml(String(pageNumber.number))}
                </a>
            `;
        }).join("");
    }

    function buildApiUrl(baseUrl, keyword) {
        const trimmedKeyword = keyword.trim();
        const params = new URLSearchParams();
        params.set("page", "1");
        params.set("keyword", trimmedKeyword);
        return `${baseUrl}?${params.toString()}`;
    }

    function buildListUrl(page, keyword) {
        const trimmedKeyword = keyword ? keyword.trim() : "";
        const params = new URLSearchParams();
        params.set("page", String(page));
        if (trimmedKeyword.length > 0) {
            params.set("keyword", trimmedKeyword);
        }
        return `/board/list?${params.toString()}`;
    }

    function buildDetailUrl(boardId, page, keyword) {
        const trimmedKeyword = keyword ? keyword.trim() : "";
        const params = new URLSearchParams();
        params.set("page", String(page));
        if (trimmedKeyword.length > 0) {
            params.set("keyword", trimmedKeyword);
        }
        return `/board/${boardId}?${params.toString()}`;
    }

    function showLoading() {
        loadingMessage.hidden = false;
    }

    function hideLoading() {
        loadingMessage.hidden = true;
    }

    function showError(message) {
        errorMessage.hidden = false;
        errorMessage.textContent = message;
    }

    function hideError() {
        errorMessage.hidden = true;
        errorMessage.textContent = "";
    }

    function escapeHtml(value) {
        return value
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll("\"", "&quot;")
            .replaceAll("'", "&#39;");
    }

    function escapeAttribute(value) {
        return escapeHtml(value);
    }
});
