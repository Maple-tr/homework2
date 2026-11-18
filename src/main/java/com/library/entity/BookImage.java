package com.library.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "book_image")
public class BookImage extends BaseEntity {

    @Column(nullable = false)
    private String bookId;

    @Column(nullable = false)
    private String imageUrl; // 存储图片的相对路径或访问URL
}