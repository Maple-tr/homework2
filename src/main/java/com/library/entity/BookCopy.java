package com.library.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "bookcopy")
public class BookCopy extends BaseEntity {

    @Column(nullable = false)
    private String bookId; // 属于哪本书

    @Column(nullable = false)
    private Boolean isBorrowed = false; // 是否已借出
}