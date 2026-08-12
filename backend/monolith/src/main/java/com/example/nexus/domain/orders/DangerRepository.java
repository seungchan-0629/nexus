package com.example.nexus.domain.orders;

import com.example.nexus.domain.orders.model.Danger;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DangerRepository extends JpaRepository<Danger, Long> {
}
