package com.data.warehouse.repository;


import com.data.warehouse.models.Deal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface DealRepository extends JpaRepository<Deal, Long> {
    Optional<Deal> findByDealId(String dealId);
    boolean existsByDealId(String dealId);
}
