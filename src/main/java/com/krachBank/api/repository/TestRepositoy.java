package com.krachbank.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.krachbank.api.models.FooTestObject;

@Repository
public interface TestRepositoy extends JpaRepository<FooTestObject, Long> {


}
