package com.library.repository;

import com.library.entity.BookImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookImageRepository extends JpaRepository<BookImage, String> {
    // 根据图书ID查询该图书的所有图片
    List<BookImage> findByBookId(String bookId);

    // 根据图书ID删除相关图片
    void deleteByBookId(String bookId);
}