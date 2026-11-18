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

    private String bookCopyId; // 为了 JPA 方便，先直接存 ID
    private String userId;     // 先直接存用户 ID

    private LocalDateTime borrowDate;
    private LocalDateTime returnDate;
    private LocalDateTime dueDate;

    private String status; // 未还、已还
}