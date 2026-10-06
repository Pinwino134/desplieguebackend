package com.pinwino.microservices.ventasservice.repository;

import com.pinwino.microservices.ventasservice.model.CampanaMarketing;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampanaMarketingRepository extends JpaRepository<CampanaMarketing, Long> {
}
