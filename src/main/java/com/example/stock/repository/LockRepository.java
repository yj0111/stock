package com.example.stock.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.stock.domain.Stock;

public interface LockRepository extends JpaRepository<Stock, Long> { // 실무에서는 별도의 JDBC 사용 해야함

	@Query(value = "select get_lock(:key, 3000)", nativeQuery = true)
	void getLock(String key);

	@Query(value = "select release_lock(:key)", nativeQuery = true)
	void releaseLock(String key);
}

