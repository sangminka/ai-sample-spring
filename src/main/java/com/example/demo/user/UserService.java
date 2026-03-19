package com.example.demo.user;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.board.BoardRepository;
import com.example.demo._core.web.exception.Exception400;
import com.example.demo.reply.ReplyRepository;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final ReplyRepository replyRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public boolean isUsernameAvailable(String username) {
        // 빈 값은 정상적인 아이디 검사가 아니므로 서비스 단계에서 먼저 막는다.
        if (username == null || username.isBlank()) {
            return false;
        }

        return userRepository.findByUsername(username).isEmpty();
    }

    @Transactional
    public UserResponse.Min join(UserRequest.JoinDTO requestDTO) {
        // 프론트 중복 체크를 우회해도 서버 저장 직전에 한 번 더 막아 데이터 정합성을 지킨다.
        if (!isUsernameAvailable(requestDTO.getUsername())) {
            throw new Exception400("이미 사용 중인 아이디입니다.");
        }

        var encodedPassword = bCryptPasswordEncoder.encode(requestDTO.getPassword());
        var user = User.builder()
                .username(requestDTO.getUsername())
                .password(encodedPassword)
                .email(requestDTO.getEmail())
                .postcode(requestDTO.getPostcode())
                .address(requestDTO.getAddress())
                .detailAddress(requestDTO.getDetailAddress())
                .extraAddress(requestDTO.getExtraAddress())
                .build();

        var savedUser = userRepository.save(user);
        return new UserResponse.Min(savedUser);
    }

    public UserResponse.SessionUser login(UserRequest.LoginDTO requestDTO) {
        // SSR 로그인도 서비스에 들어오기 전에 DTO 규칙을 먼저 확인해 잘못된 요청을 빠르게 차단한다.
        requestDTO.validate();

        var user = userRepository.findByUsername(requestDTO.getUsername())
                .orElseThrow(() -> new Exception400("아이디 또는 비밀번호가 올바르지 않습니다."));

        if (!bCryptPasswordEncoder.matches(requestDTO.getPassword(), user.getPassword())) {
            throw new Exception400("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        return new UserResponse.SessionUser(user);
    }

    public UserResponse.Detail getDetail(Integer userId) {
        var user = findUser(userId);
        return new UserResponse.Detail(user);
    }

    @Transactional
    public UserResponse.SessionUser update(Integer userId, UserRequest.UpdateDTO requestDTO) {
        requestDTO.validate();

        var user = findUser(userId);
        user.setEmail(requestDTO.getEmail());
        user.setPostcode(requestDTO.getPostcode());
        user.setAddress(requestDTO.getAddress());
        user.setDetailAddress(requestDTO.getDetailAddress());
        user.setExtraAddress(requestDTO.getExtraAddress());

        return new UserResponse.SessionUser(user);
    }

    @Transactional
    public void delete(Integer userId, UserRequest.DeleteDTO requestDTO) {
        requestDTO.validate();

        var user = findUser(userId);
        if (!bCryptPasswordEncoder.matches(requestDTO.getPassword(), user.getPassword())) {
            throw new Exception400("현재 비밀번호가 올바르지 않습니다.");
        }

        // 사용자 댓글을 먼저 지우고, 그 다음 사용자가 작성한 게시글에 달린 댓글과 게시글을 순서대로 제거한다.
        replyRepository.deleteByUserId(userId);

        var boards = boardRepository.findAllByUserId(userId);
        if (!boards.isEmpty()) {
            replyRepository.deleteByBoardIn(boards);
            boardRepository.deleteByUserId(userId);
        }

        userRepository.delete(user);
    }

    private User findUser(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new Exception400("사용자 정보를 찾을 수 없습니다."));
    }

}
