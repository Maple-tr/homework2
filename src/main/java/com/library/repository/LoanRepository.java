package com.library.repository;

import com.library.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, String> {

    // 自动实现：根据用户ID查询借阅记录，并按借阅时间倒序排列
    List<Loan> findByUserIdOrderByBorrowDateDesc(String userId);
}