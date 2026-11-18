package com.library.service;

import com.library.entity.*;
import com.library.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoanService {

    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private BookCopyRepository bookCopyRepository;
    @Autowired
    private LoanRepository loanRepository;
    @Autowired
    private ReservationRepository reservationRepository;

    // 临时存放推送消息 (userId -> 消息文本)
    public static final Map<String, String> MESSAGE_CACHE = new ConcurrentHashMap<>();

    /**
     * 借书逻辑
     */
    @Transactional
    public String borrowBook(String bookId, User user) {
        Book book = bookRepository.findById(bookId).orElse(null);
        if (book == null || "已废弃".equals(book.getStatus())) {
            return "图书不存在或已废弃";
        }

        // 1. 查找是否有未借出的副本
        List<BookCopy> availableCopies = bookCopyRepository.findByBookIdAndIsBorrowedFalse(bookId);
        if (availableCopies.isEmpty()) {
            // 没有库存，提示去预约
            return "NO_COPY";
        }

        // 2. 取出一个副本，标记为已借出
        BookCopy copy = availableCopies.get(0);
        copy.setIsBorrowed(true);
        bookCopyRepository.save(copy);

        // 3. 扣减库存
        book.setStock(book.getStock() - 1);
        bookRepository.save(book);

        // 4. 生成借阅记录
        Loan loan = new Loan();
        loan.setBookCopyId(copy.getId());
        loan.setUserId(user.getId());
        loan.setBorrowDate(LocalDateTime.now());
        loan.setDueDate(LocalDateTime.now().plusDays(book.getDays() != null ? book.getDays() : 30));
        loan.setStatus("未还");
        loanRepository.save(loan);

        return "SUCCESS";
    }

    /**
     * 还书逻辑
     */
    @Transactional
    public String returnBook(String copyId, String bookId) {
        BookCopy copy = bookCopyRepository.findById(copyId).orElse(null);
        if (copy == null || !copy.getIsBorrowed()) {
            return "该副本不在借出状态";
        }

        // 1. 更新副本状态
        copy.setIsBorrowed(false);
        bookCopyRepository.save(copy);

        // 2. 增加库存
        Book book = bookRepository.findById(bookId).orElse(null);
        if (book != null) {
            book.setStock(book.getStock() + 1);
            bookRepository.save(book);
        }

        // 3. 更新借阅记录
        // (这里为了简化，直接用副本ID查未还记录，实际应有 loanId)
        // 业务中根据 copyId 查对应的 loan 记录更新状态...

        // 4. 检查预约队列并通知
        List<Reservation> reservations = reservationRepository.findByBookIdAndStatusOrderByReserveTimeAsc(bookId, "等待中");
        if (!reservations.isEmpty()) {
            Reservation first = reservations.get(0);
            first.setStatus("已通知");
            reservationRepository.save(first);

            // 模拟推送到前端：存一条消息
            MESSAGE_CACHE.put(first.getUserId(), "您预约的图书《" + book.getTitle() + "》已有库存，请尽快借阅！");
        }

        return "SUCCESS";
    }

    /**
     * 预约逻辑
     */
    @Transactional
    public String reserveBook(String bookId, User user) {
        List<Reservation> exists = reservationRepository.findByBookIdAndStatusOrderByReserveTimeAsc(bookId, "等待中");
        for(Reservation r : exists) {
            if(r.getUserId().equals(user.getId())) return "您已经预约过该书了";
        }

        Reservation res = new Reservation();
        res.setBookId(bookId);
        res.setUserId(user.getId());
        res.setReserveTime(LocalDateTime.now());
        res.setStatus("等待中");
        reservationRepository.save(res);
        return "SUCCESS";
    }
}