package com.example.stock.facade;

import org.springframework.stereotype.Component;

import com.example.stock.service.OptimisticLockStockService;

@Component
public class OptimisticLockStockFacade {
	// Optimistic은 실패했을때 재시도를 해야하므로 Facade 클래스 생성
	private final OptimisticLockStockService optimisticLockStockService;

	public OptimisticLockStockFacade(OptimisticLockStockService optimisticLockStockService) {
		this.optimisticLockStockService = optimisticLockStockService;
	}

	public void decrease(Long id, Long quantity) throws InterruptedException {
		while (true) { // 업데이트 실패시 재시도 해야함
			try {
				optimisticLockStockService.decrease(id, quantity);
				break;
			} catch (Exception e) {
				Thread.sleep(50); // 50 ms 있다가 재시도
			}
		}
	}
}
