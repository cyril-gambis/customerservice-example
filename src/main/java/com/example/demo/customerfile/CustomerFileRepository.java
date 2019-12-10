package com.example.demo.customerfile;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerFileRepository extends JpaRepository<CustomerFile, Long> {

	List<CustomerFile> findByRef(String ref);
	
}
