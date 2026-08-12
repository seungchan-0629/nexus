package com.example.nexus.domain.orders;

import com.example.nexus.common.enums.OrdersStatus;
import com.example.nexus.common.enums.OrdersType;
import com.example.nexus.domain.orders.model.Orders;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;

public class OrdersSpecification {

    public static Specification<Orders> statusIn(List<OrdersStatus> statuses) {
        return (root, query, cb) -> root.get("ordersStatus").in(statuses);
    }

    public static Specification<Orders> ordersTypeEquals(OrdersType ordersType) {
        return (root, query, cb) -> cb.equal(root.get("ordersType"), ordersType);
    }

    public static Specification<Orders> createdAfter(LocalDate startDate) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), startDate.atStartOfDay());
    }

    public static Specification<Orders> createdBefore(LocalDate endDate) {
        return (root, query, cb) -> cb.lessThan(root.get("createdAt"), endDate.plusDays(1).atStartOfDay());
    }

    public static Specification<Orders> isDangerTrue() {
        return (root, query, cb) -> cb.isTrue(root.get("isDanger"));
    }


    public static Specification<Orders> keywordLike(String keyword) {
        return (root, query, cb) -> {
            String pattern = "%" + keyword + "%";
            return cb.or(
                    cb.like(root.get("idx").as(String.class), pattern),
                    cb.like(root.get("store").get("storeName"), pattern)
            );
        };
    }
}
