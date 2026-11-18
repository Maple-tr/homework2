package com.library.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "loan")
public class Loan extends BaseEntity {

    private String bookCopyId; // 副本ID
    private String bookId;     // 图书ID（新增，方便查询）
    private String userId;     // 用户ID

    private LocalDateTime borrowDate;
    private LocalDateTime returnDate;
    private LocalDateTime dueDate;

    private String status; // 未还、已还
}