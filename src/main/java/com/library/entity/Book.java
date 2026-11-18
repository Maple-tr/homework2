package com.library.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "book")
public class Book extends BaseEntity {

    private String title;
    private String author;
    private String type;
    private String publisher;
    private Integer days;
    private Double price;
    private Integer stock;
    private String status;
    private String introduction;
    private String embedding;
    private String tags;
}