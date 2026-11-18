package com.library.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@MappedSuperclass // 告诉 JPA 这是一个父类，不生成单独的表
public abstract class BaseEntity {

    @Id
    @Column(length = 36)
    private String id;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    private LocalDateTime updateTime;

    // 在持久化之前自动生成 32 位 UUID
    @PrePersist
    public void generateId() {
        if (this.id == null) {
            this.id = java.util.UUID.randomUUID().toString().replace("-", "");
        }
    }
}