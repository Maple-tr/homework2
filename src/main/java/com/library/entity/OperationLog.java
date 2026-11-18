package com.library.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "operation_log")
public class OperationLog extends BaseEntity {
    private String username;  // 操作人
    private String operation; // 操作方法
    private String method;    // 请求路径和方法
    private String params;    // 请求参数
}