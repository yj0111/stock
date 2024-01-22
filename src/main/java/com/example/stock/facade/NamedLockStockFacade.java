package com.example.stock.facade;

import org.springframework.stereotype.Component;
import com.example.stock.repository.LockRepository;
import com.example.stock.service.StockService;

@Component
public class NamedLockStockFacade {
	// NamedLock은 실제 로직 전후로 락 획득 해제를 해줘야하기 때문에 Facade 클래스 생성
	private final LockRepository lockRepository;

	private final StockService stockService;

	public NamedLockStockFacade(LockRepository lockRepository, StockService stockService) {
		this.lockRepository = lockRepository;
		this.stockService = stockService;
	}

	public void decrease(Long id, Long quantity) {
		try {
			lockRepository.getLock(id.toString()); //락 획득
			stockService.decrease(id, quantity); //재고 감소
		} finally {
			lockRepository.releaseLock(id.toString()); //락 해제
		}
	}
}
