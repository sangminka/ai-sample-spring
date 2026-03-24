# 작업 보고서 [게시글 검색 Phase 3]

- **작업 일시**: 2026-03-24
- **진행 상태**: 완료

## 1. 전체 작업 흐름 (Workflow)

1. Phase 2의 실시간 검색 구조를 유지한 채, Phase 3 목표를 `300ms 디바운스 + 마지막 요청만 반영`으로 정리했다.
2. 현재 `board-search.js`가 입력마다 바로 `fetch`를 보내고 있었기 때문에, 요청 시점과 로딩 시점을 분리하는 방향으로 수정하기로 했다.
3. `DEBOUNCE_DELAY = 300` 기준으로 타이머를 두고, 입력 직후에는 요청을 보내지 않도록 바꿨다.
4. 새 입력이 들어오면 기존 디바운스 타이머를 취소하고, 이미 진행 중인 요청이 있으면 `AbortController`로 중단하도록 만들었다.
5. 디바운스 대기 중에는 로딩 메시지를 숨기고, 실제 네트워크 요청이 시작되는 순간에만 `검색 중...`을 보이게 했다.
6. 요청마다 `requestSequence`를 증가시켜, 늦게 도착한 오래된 응답이 최신 화면을 덮어쓰지 못하도록 보호 로직을 추가했다.
7. 요청이 성공하면 기존 Phase 2와 동일하게 검색 결과 영역과 URL을 갱신하고, 요청이 실패하면 기존 목록은 유지한 채 오류 메시지만 노출하도록 유지했다.
8. `node --check`로 JS 문법을 확인하고, `./gradlew.bat test --tests com.example.demo.board.*`로 서버/템플릿 쪽 회귀가 없는지 검증했다.
9. 체크리스트에서 Phase 3 구현 항목을 완료 처리하고, 브라우저에서 직접 봐야 하는 수동 확인 항목은 남겨두었다.

## 2. 변경한 코드 정리

### 2-1. 디바운스 상수와 상태 변수 추가

```javascript
const DEBOUNCE_DELAY = 300;
let debounceTimerId = null;
let activeController = null;
let requestSequence = 0;
```

- 파일: `src/main/resources/static/js/board-search.js`
- 역할: 입력마다 즉시 요청을 보내지 않고, 일정 시간 동안 입력이 멈췄을 때만 검색을 시작하기 위한 상태를 보관한다.

### 2-2. input 이벤트를 즉시 요청에서 디바운스 예약 방식으로 변경

```javascript
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
```

- 핵심:
  - 입력 직후에는 `fetch`를 보내지 않는다
  - 마지막 입력 후 `300ms`가 지나야 실제 요청을 보낸다
  - 디바운스 대기 중에는 로딩을 띄우지 않는다

### 2-3. 이전 타이머와 이전 요청을 취소하는 정리 함수 추가

```javascript
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
```

- 핵심:
  - 아직 시작되지 않은 디바운스 타이머는 취소한다
  - 이미 날아간 요청은 `AbortController`로 끊는다
  - 새 입력이 들어오면 이전 검색 흐름을 즉시 정리한다

### 2-4. 실제 요청 시점에만 로딩을 보여주는 검색 함수로 분리

```javascript
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
```

- 핵심:
  - 실제 요청 시작 시점에만 `showLoading()`
  - 실패 시에도 `finally`에서 로딩을 정리
  - `AbortError`는 조용히 무시
  - `requestSequence`가 다르면 오래된 응답으로 간주하고 화면 반영을 건너뜀

### 2-5. 마지막 요청만 반영되도록 만든 보호 장치

```javascript
if (requestId !== requestSequence) {
    return;
}
```

- 이 조건이 성공 처리와 실패 처리 양쪽에 들어가 있다.
- 덕분에 예전 검색 응답이 늦게 도착해도 최신 검색 결과를 덮어쓰지 못한다.

## 3. 쉬운 비유 (Easy Analogy)

- 이번 작업은 누군가에게 질문을 아주 빠르게 여러 번 보낼 때, 마지막 질문만 유효하게 만드는 비서 시스템을 붙인 것과 비슷하다.
- 예전에는 사용자가 글자를 하나 칠 때마다 바로 전화해서 물어보는 구조였다.
- 이제는 `300ms` 동안 더 입력이 들어오는지 잠깐 기다렸다가, 정말 마지막 질문만 전달한다.
- 만약 이미 이전 질문에 대한 전화가 진행 중이면 끊고, 새 질문이 더 중요하다고 판단해 마지막 질문 결과만 사용자에게 보여주는 방식이다.

## 4. 기술 딥다이브 (Technical Deep-dive)

### 왜 디바운스와 로딩 시점을 분리했는가

- 사용자가 타이핑하는 순간마다 로딩이 깜빡이면 UX가 불안정해 보인다.
- 디바운스는 “아직 요청도 안 보낸 대기 시간”이므로, 이 구간은 로딩이 아니라 단순 입력 중 상태에 가깝다.
- 그래서 입력 직후에는 `hideLoading()`, 실제 `performSearch()`가 시작될 때만 `showLoading()` 하도록 분리했다.

### 왜 `AbortController`와 `requestSequence`를 함께 썼는가

- `AbortController`는 이전 요청을 끊는 가장 직접적인 방법이다.
- 하지만 네트워크 타이밍에 따라 요청 취소 직전 응답이 도착하거나, 로직상 오래된 응답이 뒤늦게 처리될 가능성까지 완전히 배제하려면 추가 보호가 필요하다.
- 그래서 요청마다 번호를 붙이고, 현재 최신 요청 번호와 다르면 화면 반영 자체를 막는 방식을 함께 사용했다.
- 즉, `요청 취소`와 `응답 무시`를 둘 다 갖춘 이중 안전장치로 본 셈이다.

### 왜 실패 시에도 기존 목록을 유지하는가

- Phase 2에서 잡아둔 원칙을 그대로 유지했다.
- 디바운스가 들어가도 네트워크 실패 가능성은 여전히 있으므로, 실패했다고 결과 목록을 비우면 사용자가 더 불안해진다.
- 그래서 오류 메시지만 갱신하고, 기존 목록은 그대로 남겨두었다.

## 5. 검증 결과

### 실행 명령

```bash
node --check src/main/resources/static/js/board-search.js
```

```bash
./gradlew.bat test --tests com.example.demo.board.*
```

### 결과

- `board-search.js` 문법 검사 통과
- `board` 범위 테스트 통과

### 자동 검증으로 확인한 것

- 디바운스/AbortController 추가 이후 JS 문법 이상 없음
- 서버 API/SSR 템플릿/기존 게시글 테스트 회귀 없음

### 아직 수동 확인이 필요한 것

- 빠르게 타이핑할 때 결과가 뒤섞이지 않는지
- 입력 삭제 후 전체 목록 복귀가 자연스러운지
- 네트워크 실패 시 기존 목록이 그대로 유지되는지
- 빠른 입력에서 실제 요청 수가 줄어드는지
- 마지막 입력 기준으로만 결과가 반영되는지
- 로딩, 성공, 실패 상태가 자연스럽게 보이는지

## 6. 다음 단계

1. 브라우저에서 실제로 빠르게 타이핑해 보면서 Phase 2, Phase 3의 남은 체크 항목을 확인한다.
2. 필요하면 한글 조합 입력(`isComposing`) 대응을 추가로 검토한다.
3. 원하면 다음 단계로 커밋 정리 또는 전체 테스트 실행으로 이어간다.
