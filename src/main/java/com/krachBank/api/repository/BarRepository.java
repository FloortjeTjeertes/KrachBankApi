package com.krachbank.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.krachbank.api.models.BarTestObject;


@Repository
public interface BarRepository extends JpaRepository<BarTestObject, Long> {

    // Add any custom query methods here if needed


}
