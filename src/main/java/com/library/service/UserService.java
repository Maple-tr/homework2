package com.library.service;

import com.library.dto.LoginRequest;
import com.library.entity.User;
import com.library.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // 充当临时缓存：Token -> User对象
    public static final Map<String, User> TOKEN_CACHE = new ConcurrentHashMap<>();

    public String login(LoginRequest request) {
        Optional<User> userOpt = userRepository.findByName(request.getUsername());

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // 这里为了兼容作业2，直接比对明文密码
            if (user.getPassword().equals(request.getPassword())) {
                // 登录成功，生成 Token
                String token = UUID.randomUUID().toString().replace("-", "");
                TOKEN_CACHE.put(token, user);
                return token;
            }
        }
        return null; // 登录失败
    }
}