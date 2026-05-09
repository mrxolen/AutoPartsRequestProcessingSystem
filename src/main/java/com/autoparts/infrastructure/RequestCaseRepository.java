package com.autoparts.infrastructure;

import com.autoparts.domain.RequestCase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequestCaseRepository extends JpaRepository<RequestCase, Long> {
}
