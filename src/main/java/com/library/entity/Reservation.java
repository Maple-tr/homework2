package com.library.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "book_reservation")
public class Reservation extends BaseEntity {

    private String bookId;
    private String userId;
    private LocalDateTime reserveTime;

    // 状态：等待中、已通知、已取消
    private String status;
}