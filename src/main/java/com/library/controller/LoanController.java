package com.library.controller;

import com.library.entity.Loan;
import com.library.entity.User;
import com.library.repository.LoanRepository;
import com.library.service.LoanService;
import com.library.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    @Autowired
    private LoanService loanService;

    @Autowired
    private LoanRepository loanRepository;

    // 辅助方法：从请求头获取当前登录用户
    private User getCurrentUser(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        return UserService.TOKEN_CACHE.get(token);
    }

    // 1. 借书接口
    @PostMapping("/borrow")
    public Map<String, Object> borrowBook(@RequestBody Map<String, String> body, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        User user = getCurrentUser(request);
        String bookId = body.get("bookId");

        String msg = loanService.borrowBook(bookId, user);

        if ("SUCCESS".equals(msg)) {
            result.put("code", 200);
            result.put("msg", "借阅成功");
        } else if ("NO_COPY".equals(msg)) {
            result.put("code", 400);
            result.put("msg", "该书暂无库存，您可以尝试预约");
        } else {
            result.put("code", 400);
            result.put("msg", msg);
        }
        return result;
    }

    // 2. 还书接口
    @PostMapping("/return")
    public Map<String, Object> returnBook(@RequestBody Map<String, String> body, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        String loanId = body.get("loanId");
        String copyId = body.get("copyId");

        String msg = loanService.returnBook(loanId, copyId);

        if ("SUCCESS".equals(msg)) {
            result.put("code", 200);
            result.put("msg", "还书成功");
        } else {
            result.put("code", 400);
            result.put("msg", msg);
        }
        return result;
    }

    // 3. 预约接口
    @PostMapping("/reserve")
    public Map<String, Object> reserveBook(@RequestBody Map<String, String> body, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        User user = getCurrentUser(request);
        String bookId = body.get("bookId");

        String msg = loanService.reserveBook(bookId, user);

        if ("SUCCESS".equals(msg)) {
            result.put("code", 200);
            result.put("msg", "预约成功，有库存后将通知您");
        } else {
            result.put("code", 400);
            result.put("msg", msg);
        }
        return result;
    }

    // 4. 获取当前用户的借阅记录
    @GetMapping("/my")
    public Map<String, Object> getMyLoans(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        User user = getCurrentUser(request);

        List<Loan> loans = loanRepository.findByUserIdOrderByBorrowDateDesc(user.getId());

        result.put("code", 200);
        result.put("data", loans);
        return result;
    }

    // 5. 获取当前用户的未读消息 (用于前端轮询预约通知)
    @GetMapping("/message")
    public Map<String, Object> getMessage(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        User user = getCurrentUser(request);
        if (user != null) {
            // 从缓存中取出消息，取完就删掉（保证只提示一次）
            String msg = LoanService.MESSAGE_CACHE.remove(user.getId());
            result.put("code", 200);
            if (msg != null) {
                result.put("hasMessage", true);
                result.put("msg", msg);
            } else {
                result.put("hasMessage", false);
            }
        } else {
            result.put("code", 401);
        }
        return result;
    }
}