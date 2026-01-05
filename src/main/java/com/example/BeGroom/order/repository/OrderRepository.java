package com.example.BeGroom.order.repository;

import com.example.BeGroom.order.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    public List<Order> findAllByMemberIdOrderByCreatedAtDesc(Long memberId);

    @Query("SELECT DISTINCT o FROM Order o " +
            "JOIN FETCH o.orderProductList op " +
            "JOIN FETCH op.product p " +
            "WHERE o.member.id = :memberId " +
            "ORDER BY o.createdAt DESC")
    List<Order> findAllWithDetailsByMemberId(@Param("memberId") Long memberId);
}
