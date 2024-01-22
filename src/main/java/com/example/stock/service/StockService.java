package com.example.stock.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.stock.domain.Stock;
import com.example.stock.repository.StockRepository;

@Service
public class StockService {

	private final StockRepository stockRepository;

	public StockService(StockRepository stockRepository) {
		this.stockRepository = stockRepository;
	}

	//@Transactional //방법 1) @Transactional 주석처리하기
	//synchronized: 메소드에 하나의 Thread만 접근 가능
	////synchronized 문제점: 서버가 2대 이상일 경우, 데이터 접근이 여러 대에서 가능

	public synchronized void decrease(Long id, Long quantity) {
		// 1. Stock 조회
		// 2. 재고를 감소한 뒤
		// 3. 갱신된 값을 저장

		Stock stock = stockRepository.findById(id).orElseThrow();
		stock.decrease(quantity);
		stockRepository.saveAndFlush(stock);
	}
}
