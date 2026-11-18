// BookCopyRepository.java
package com.library.repository;

import com.library.entity.BookCopy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookCopyRepository extends JpaRepository<BookCopy, String> {
    // 查找该书所有未借出的副本
    List<BookCopy> findByBookIdAndIsBorrowedFalse(String bookId);
}