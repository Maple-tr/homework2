package com.library.controller;

import com.library.entity.Book;
import com.library.entity.BookCopy;
import com.library.entity.BookImage;
import com.library.repository.BookCopyRepository;
import com.library.repository.BookImageRepository;
import com.library.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/books")
public class BookController {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookImageRepository bookImageRepository;

    @Autowired
    private BookCopyRepository bookCopyRepository;

    // 1. 图书分页 + 多条件查询
    @GetMapping("/page")
    public Map<String, Object> getBookPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type) {

        // 按创建时间倒序排序
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));

        // 动态组合查询条件
        Specification<Book> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null && !keyword.trim().isEmpty()) {
                String like = "%" + keyword.trim() + "%";
                predicates.add(cb.or(
                        cb.like(root.get("title"), like),
                        cb.like(root.get("author"), like)
                ));
            }
            if (type != null && !type.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("type"), type));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Book> bookPage = bookRepository.findAll(spec, pageable);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", bookPage.getContent());
        result.put("total", bookPage.getTotalElements());
        result.put("totalPages", bookPage.getTotalPages());
        return result;
    }

    // 2. 查看单本图书详情及图片
    @GetMapping("/{id}")
    public Map<String, Object> getBookDetail(@PathVariable String id) {
        Map<String, Object> result = new HashMap<>();
        Book book = bookRepository.findById(id).orElse(null);
        if (book == null) {
            result.put("code", 404);
            result.put("msg", "图书不存在");
            return result;
        }
        // 查询附件表里的图片
        List<BookImage> images = bookImageRepository.findByBookId(id);
        result.put("code", 200);
        result.put("book", book);
        result.put("images", images);
        return result;
    }

    // 3. 新增图书（带图片 + 自动生成副本）
    @PostMapping
    @Transactional // 保证事务一致性（图书、图片、副本要么全成功，要么全失败）
    public Map<String, Object> addBook(@RequestBody Map<String, Object> payload) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 保存图书基础信息
            Book book = new Book();
            book.setTitle((String) payload.get("title"));
            book.setAuthor((String) payload.get("author"));
            book.setType((String) payload.get("type"));
            book.setPublisher((String) payload.get("publisher"));

            // 处理可能为空的数字类型
            if(payload.get("days") != null) book.setDays(Integer.parseInt(payload.get("days").toString()));
            if(payload.get("price") != null) book.setPrice(Double.parseDouble(payload.get("price").toString()));
            if(payload.get("stock") != null) book.setStock(Integer.parseInt(payload.get("stock").toString()));

            book.setIntroduction((String) payload.get("introduction"));
            book.setStatus("正常"); // 默认状态

            bookRepository.save(book);

            // 2. 处理图片（前端传来的 coverUrl）
            String coverUrl = (String) payload.get("coverUrl");
            if (coverUrl != null && !coverUrl.isEmpty()) {
                BookImage image = new BookImage();
                image.setBookId(book.getId());
                image.setImageUrl(coverUrl);
                bookImageRepository.save(image);
            }

            // 3. 核心逻辑：根据库存 stock 自动生成对应数量的图书副本
            int stock = book.getStock() != null ? book.getStock() : 0;
            for (int i = 0; i < stock; i++) {
                BookCopy copy = new BookCopy();
                copy.setBookId(book.getId());
                copy.setIsBorrowed(false); // 默认未借出
                bookCopyRepository.save(copy);
            }

            result.put("code", 200);
            result.put("msg", "图书添加成功，已自动生成 " + stock + " 个副本");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "添加失败：" + e.getMessage());
            // 抛出异常触发 @Transactional 回滚
            throw new RuntimeException("添加图书失败");
        }
        return result;
    }

    // 4. 修改图书（根据作业要求，不允许修改库存和状态）
    @PutMapping("/update")
    @Transactional
    public Map<String, Object> updateBook(@RequestBody Book bookData) {
        Map<String, Object> result = new HashMap<>();
        Book book = bookRepository.findById(bookData.getId()).orElse(null);

        if (book == null) {
            result.put("code", 404);
            result.put("msg", "图书不存在");
            return result;
        }

        // 只允许更新特定字段
        book.setTitle(bookData.getTitle());
        book.setAuthor(bookData.getAuthor());
        book.setType(bookData.getType());
        book.setPublisher(bookData.getPublisher());
        book.setDays(bookData.getDays());
        book.setPrice(bookData.getPrice());

        bookRepository.save(book);
        result.put("code", 200);
        result.put("msg", "修改成功");
        return result;
    }
}