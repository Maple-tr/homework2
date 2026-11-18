package com.library.controller;

import com.library.entity.Book;
import com.library.entity.BookImage;
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

    // 1. 图书分页 + 多条件查询
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

    // 3. 新增图书并保存图片 URL
    @PostMapping
    @Transactional // 保证图书和图片一起成功或一起失败
    public Map<String, Object> addBook(@RequestBody Map<String, Object> payload) {
        Map<String, Object> result = new HashMap<>();

        // 简单映射
        Book book = new Book();
        book.setTitle((String) payload.get("title"));
        book.setAuthor((String) payload.get("author"));
        book.setType((String) payload.get("type"));
        book.setPublisher((String) payload.get("publisher"));
        book.setDays((Integer) payload.get("days"));
        book.setPrice(Double.valueOf(payload.get("price").toString()));
        book.setStock((Integer) payload.get("stock"));
        book.setIntroduction((String) payload.get("introduction"));
        book.setStatus("正常");

        bookRepository.save(book);

        // 保存图片
        List<String> imageUrls = (List<String>) payload.get("images");
        if (imageUrls != null && !imageUrls.isEmpty()) {
            for (String url : imageUrls) {
                BookImage image = new BookImage();
                image.setBookId(book.getId());
                image.setImageUrl(url);
                bookImageRepository.save(image);
            }
        }

        result.put("code", 200);
        result.put("msg", "图书添加成功");
        return result;
    }
}