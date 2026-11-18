package com.library.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "user")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE) // 单表继承策略
@DiscriminatorColumn(name = "role", discriminatorType = DiscriminatorType.STRING) // 区分角色的列
public class User extends BaseEntity {

    private String name;
    private String password;
    private String phone;

    @Column(insertable = false, updatable = false) // 角色由子类决定
    private String role;

    private String major;
    private LocalDateTime loginTime;

    // 以下是各个子类可能用到的公共字段
    private Integer permissionLevel; // Admin
    private LocalDate entryTime;     // Admin
    private String area;             // Operator
    private String duty;             // Operator
    private String grade;            // Student
    private String college;          // Student, Teacher
    private String title;            // Teacher
}