package com.library.controller;

import com.library.dto.LoginRequest;
import com.library.entity.User;
import com.library.repository.UserRepository;
import com.library.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    // 1. 登录接口
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest request) {
        Map<String, Object> result = new HashMap<>();
        String token = userService.login(request);
        if (token != null) {
            result.put("code", 200);
            result.put("msg", "登录成功");
            result.put("token", token);
        } else {
            result.put("code", 400);
            result.put("msg", "用户名或密码错误");
        }
        return result;
    }

    // 2. 服务端分页 + 多条件组合查询
    // 前端调用示例: /api/users/page?page=0&size=10&keyword=张&role=学生
    @GetMapping("/page")
    public Map<String, Object> getUserPage(
            @RequestParam(defaultValue = "0") int page,       // 第几页（从0开始）
            @RequestParam(defaultValue = "10") int size,      // 每页条数
            @RequestParam(required = false) String keyword,   // 模糊搜索（姓名/手机号）
            @RequestParam(required = false) String role       // 精确匹配角色
    ) {
        // 按创建时间倒序排序，分页条件封装
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));

        // 核心：使用 JPA 的 Specification 动态拼接 SQL
        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null && !keyword.trim().isEmpty()) {
                String likePattern = "%" + keyword.trim() + "%";
                predicates.add(cb.or(
                        cb.like(root.get("name"), likePattern),
                        cb.like(root.get("phone"), likePattern)
                ));
            }
            if (role != null && !role.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("role"), role));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<User> userPage = userRepository.findAll(spec, pageable);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", userPage.getContent());     // 当前页数据
        result.put("total", userPage.getTotalElements()); // 总条数
        result.put("totalPages", userPage.getTotalPages()); // 总页数
        return result;
    }
}