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

    // 1. 图书分页 + 多条件查询 (包含封面图片)
    @GetMapping("/page")
    public Map<String, Object> getBookPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));

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

        // 将 Book 转换为包含 coverUrl 的 Map，方便前端直接读取
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (Book book : bookPage.getContent()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", book.getId());
            map.put("title", book.getTitle());
            map.put("author", book.getAuthor());
            map.put("type", book.getType());
            map.put("publisher", book.getPublisher());
            map.put("stock", book.getStock());
            map.put("status", book.getStatus());
            map.put("price", book.getPrice());
            map.put("days", book.getDays());
            map.put("introduction", book.getIntroduction());

            // 查询附件表，取第一张图作为封面
            List<BookImage> images = bookImageRepository.findByBookId(book.getId());
            if (!images.isEmpty()) {
                map.put("coverUrl", images.get(0).getImageUrl());
            } else {
                map.put("coverUrl", null);
            }
            resultList.add(map);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", resultList);
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
        List<BookImage> images = bookImageRepository.findByBookId(id);
        result.put("code", 200);
        result.put("book", book);
        result.put("images", images);
        return result;
    }

    // 3. 新增图书（带图片 + 自动生成副本）
    @PostMapping
    @Transactional
    public Map<String, Object> addBook(@RequestBody Map<String, Object> payload) {
        Map<String, Object> result = new HashMap<>();

        try {
            Book book = new Book();
            book.setTitle((String) payload.get("title"));
            book.setAuthor((String) payload.get("author"));
            book.setType((String) payload.get("type"));
            book.setPublisher((String) payload.get("publisher"));

            if(payload.get("days") != null && !payload.get("days").toString().isEmpty())
                book.setDays(Integer.parseInt(payload.get("days").toString()));
            if(payload.get("price") != null && !payload.get("price").toString().isEmpty())
                book.setPrice(Double.parseDouble(payload.get("price").toString()));
            if(payload.get("stock") != null && !payload.get("stock").toString().isEmpty())
                book.setStock(Integer.parseInt(payload.get("stock").toString()));

            book.setIntroduction((String) payload.get("introduction"));
            book.setStatus("正常");

            bookRepository.save(book);

            // 保存图片
            String coverUrl = (String) payload.get("coverUrl");
            if (coverUrl != null && !coverUrl.isEmpty()) {
                BookImage image = new BookImage();
                image.setBookId(book.getId());
                image.setImageUrl(coverUrl);
                bookImageRepository.save(image);
            }

            // 自动生成对应数量的副本
            int stock = book.getStock() != null ? book.getStock() : 0;
            for (int i = 0; i < stock; i++) {
                BookCopy copy = new BookCopy();
                copy.setBookId(book.getId());
                copy.setIsBorrowed(false);
                bookCopyRepository.save(copy);
            }

            result.put("code", 200);
            result.put("msg", "图书添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "添加失败：" + e.getMessage());
            throw new RuntimeException("添加图书失败"); // 触发回滚
        }
        return result;
    }

    // 4. 修改图书（根据要求，不修改库存和状态）
    @PutMapping("/update")
    @Transactional
    public Map<String, Object> updateBook(@RequestBody Map<String, Object> payload) {
        Map<String, Object> result = new HashMap<>();
        String id = (String) payload.get("id");
        Book book = bookRepository.findById(id).orElse(null);

        if (book == null) {
            result.put("code", 404);
            result.put("msg", "图书不存在");
            return result;
        }

        // 只更新允许修改的字段
        book.setTitle((String) payload.get("title"));
        book.setAuthor((String) payload.get("author"));
        book.setType((String) payload.get("type"));
        book.setPublisher((String) payload.get("publisher"));

        if(payload.get("days") != null && !payload.get("days").toString().isEmpty())
            book.setDays(Integer.parseInt(payload.get("days").toString()));
        if(payload.get("price") != null && !payload.get("price").toString().isEmpty())
            book.setPrice(Double.parseDouble(payload.get("price").toString()));

        bookRepository.save(book);

        // 如果前端传了新图片，更新附件表
        String coverUrl = (String) payload.get("coverUrl");
        if (coverUrl != null && !coverUrl.isEmpty()) {
            List<BookImage> existingImages = bookImageRepository.findByBookId(id);
            if (existingImages.isEmpty()) {
                BookImage image = new BookImage();
                image.setBookId(id);
                image.setImageUrl(coverUrl);
                bookImageRepository.save(image);
            } else {
                BookImage image = existingImages.get(0);
                image.setImageUrl(coverUrl);
                bookImageRepository.save(image);
            }
        }

        result.put("code", 200);
        result.put("msg", "修改成功");
        return result;
    }
}