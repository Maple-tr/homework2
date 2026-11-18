package com.library.controller;

import com.library.entity.OperationLog;
import com.library.repository.OperationLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/logs")
public class LogController {

    @Autowired
    private OperationLogRepository logRepository;

    @GetMapping("/page")
    public Map<String, Object> getLogPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<OperationLog> logPage = logRepository.findAll(pageable);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", logPage.getContent());
        result.put("total", logPage.getTotalElements());
        result.put("totalPages", logPage.getTotalPages());
        return result;
    }
}