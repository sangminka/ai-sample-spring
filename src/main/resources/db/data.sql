-- 유저 더미 데이터
INSERT INTO user_tb (username, password, created_at) VALUES ('ssar', '$2a$10$be4Db2N4Mzq5fJRu7m7DaOXe3t.pWNM1Q.WUVEy5JmbuDV8n2i8oO', NOW());
INSERT INTO user_tb (username, password, created_at) VALUES ('cos', '$2a$10$be4Db2N4Mzq5fJRu7m7DaOXe3t.pWNM1Q.WUVEy5JmbuDV8n2i8oO', NOW());

-- 게시글 더미 데이터
INSERT INTO board_tb (title, content, user_id, created_at) VALUES ('첫 번째 게시글', '안녕하세요. ssar의 첫 번째 글입니다.', 1, NOW());
INSERT INTO board_tb (title, content, user_id, created_at) VALUES ('두 번째 게시글', '안녕하세요. ssar의 두 번째 글입니다.', 1, NOW());
INSERT INTO board_tb (title, content, user_id, created_at) VALUES ('세 번째 게시글', '안녕하세요. cos의 첫 번째 글입니다.', 2, NOW());
INSERT INTO board_tb (title, content, user_id, created_at) VALUES ('학습용 게시글 04', '페이징 학습을 위해 준비한 네 번째 게시글입니다.', 1, NOW());
INSERT INTO board_tb (title, content, user_id, created_at) VALUES ('학습용 게시글 05', '페이징 학습을 위해 준비한 다섯 번째 게시글입니다.', 2, NOW());
INSERT INTO board_tb (title, content, user_id, created_at) VALUES ('학습용 게시글 06', '페이징 학습을 위해 준비한 여섯 번째 게시글입니다.', 1, NOW());
INSERT INTO board_tb (title, content, user_id, created_at) VALUES ('학습용 게시글 07', '페이징 학습을 위해 준비한 일곱 번째 게시글입니다.', 2, NOW());
INSERT INTO board_tb (title, content, user_id, created_at) VALUES ('학습용 게시글 08', '페이징 학습을 위해 준비한 여덟 번째 게시글입니다.', 1, NOW());
INSERT INTO board_tb (title, content, user_id, created_at) VALUES ('학습용 게시글 09', '페이징 학습을 위해 준비한 아홉 번째 게시글입니다.', 2, NOW());
INSERT INTO board_tb (title, content, user_id, created_at) VALUES ('학습용 게시글 10', '페이징 학습을 위해 준비한 열 번째 게시글입니다.', 1, NOW());
INSERT INTO board_tb (title, content, user_id, created_at) VALUES ('학습용 게시글 11', '페이징 학습을 위해 준비한 열한 번째 게시글입니다.', 2, NOW());
INSERT INTO board_tb (title, content, user_id, created_at) VALUES ('학습용 게시글 12', '페이징 학습을 위해 준비한 열두 번째 게시글입니다.', 1, NOW());
INSERT INTO board_tb (title, content, user_id, created_at) VALUES ('학습용 게시글 13', '페이징 학습을 위해 준비한 열세 번째 게시글입니다.', 2, NOW());
INSERT INTO board_tb (title, content, user_id, created_at) VALUES ('학습용 게시글 14', '페이징 학습을 위해 준비한 열네 번째 게시글입니다.', 1, NOW());
INSERT INTO board_tb (title, content, user_id, created_at) VALUES ('학습용 게시글 15', '페이징 학습을 위해 준비한 열다섯 번째 게시글입니다.', 2, NOW());
INSERT INTO board_tb (title, content, user_id, created_at) VALUES ('학습용 게시글 16', '페이징 학습을 위해 준비한 열여섯 번째 게시글입니다.', 1, NOW());
INSERT INTO board_tb (title, content, user_id, created_at) VALUES ('학습용 게시글 17', '페이징 학습을 위해 준비한 열일곱 번째 게시글입니다.', 2, NOW());
INSERT INTO board_tb (title, content, user_id, created_at) VALUES ('학습용 게시글 18', '페이징 학습을 위해 준비한 열여덟 번째 게시글입니다.', 1, NOW());
INSERT INTO board_tb (title, content, user_id, created_at) VALUES ('학습용 게시글 19', '페이징 학습을 위해 준비한 열아홉 번째 게시글입니다.', 2, NOW());
INSERT INTO board_tb (title, content, user_id, created_at) VALUES ('학습용 게시글 20', '페이징 학습을 위해 준비한 스무 번째 게시글입니다.', 1, NOW());
INSERT INTO board_tb (title, content, user_id, created_at) VALUES ('학습용 게시글 21', '페이징 학습을 위해 준비한 스물한 번째 게시글입니다.', 2, NOW());
INSERT INTO board_tb (title, content, user_id, created_at) VALUES ('학습용 게시글 22', '페이징 학습을 위해 준비한 스물두 번째 게시글입니다.', 1, NOW());
INSERT INTO board_tb (title, content, user_id, created_at) VALUES ('학습용 게시글 23', '페이징 학습을 위해 준비한 스물세 번째 게시글입니다.', 2, NOW());

-- 댓글 더미 데이터
INSERT INTO reply_tb (comment, user_id, board_id, created_at) VALUES ('첫 번째 게시글에 ssar이 작성한 댓글입니다.', 1, 1, NOW());
INSERT INTO reply_tb (comment, user_id, board_id, created_at) VALUES ('첫 번째 게시글에 cos가 작성한 댓글입니다.', 2, 1, NOW());
INSERT INTO reply_tb (comment, user_id, board_id, created_at) VALUES ('두 번째 게시글에 cos가 작성한 댓글입니다.', 2, 2, NOW());
