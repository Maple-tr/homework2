package com.library.repository;

import com.library.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String>, JpaSpecificationExecutor<User> {
    // 自动实现根据姓名查找用户
    Optional<User> findByName(String name);

    // 自动实现根据手机号查找
    Optional<User> findByPhone(String phone);
}