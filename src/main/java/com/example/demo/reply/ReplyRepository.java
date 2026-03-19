package com.example.demo.reply;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.board.Board;

public interface ReplyRepository extends JpaRepository<Reply, Integer> {

    long deleteByUserId(Integer userId);

    long deleteByBoardIn(List<Board> boards);
}
