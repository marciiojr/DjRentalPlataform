package com.locadj.repository;

import com.locadj.model.Kit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KitRepository extends JpaRepository<Kit, Long> {
}
