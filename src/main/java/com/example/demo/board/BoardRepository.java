package com.example.demo.board;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, Integer> {
    List<Board> findAllByOrderByIdDesc();

    List<Board> findAllByUserId(Integer userId);

    long deleteByUserId(Integer userId);

    Optional<Board> findById(Integer id);
}
