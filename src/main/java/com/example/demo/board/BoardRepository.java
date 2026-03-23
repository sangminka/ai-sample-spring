package com.example.demo.board;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BoardRepository extends JpaRepository<Board, Integer> {
    List<Board> findAllByOrderByIdDesc();

    @Query(value = "select * from board_tb order by id desc limit 3 offset :offset", nativeQuery = true)
    List<Board> findByPaging(@Param("offset") Integer offset);

    @Query(value = "select count(*) from board_tb", nativeQuery = true)
    Long countAll();

    List<Board> findAllByUserId(Integer userId);

    long deleteByUserId(Integer userId);

    Optional<Board> findById(Integer id);
}
