// ReservationRepository.java
package com.library.repository;

import com.library.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, String> {
    // 查找该书正在等待中的预约记录，按时间先后排序
    List<Reservation> findByBookIdAndStatusOrderByReserveTimeAsc(String bookId, String status);
}