package com.example.stock.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.stock.domain.Stock;
import com.example.stock.repository.StockRepository;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
class StockServiceTest {

	@Autowired
	private PessimisticLockStockService stockService;

	@Autowired
	private StockRepository stockRepository;

	@BeforeEach //테스트가 실행 되기 전에
	public void before() {
		stockRepository.saveAndFlush(new Stock(1L, 100L));
	}

	@AfterEach
	public void after() {
		stockRepository.deleteAll();
	}

	@Test
	public void 재고감소() {
		stockService.decrease(1L, 1L);
		Stock stock = stockRepository.findById(1L).orElseThrow();
		assertEquals(99, stock.getQuantity());
	}

	@Test
	public void 동시에_100개의_요청() throws InterruptedException {
		int threadCount = 100;
		//멀티쓰레드 이용, 비동기로 실행 작업을 단순화 하여 사용하게 도와주는 자바의 API
		ExecutorService executorService = Executors.newFixedThreadPool(32);

		//100개의 요청이 끝날때까지 기다려야 하므로 CountDownLatch를 활용
		//CountDownLatch : 다른 쓰레드에서 진행중인 작업이 완료될 때까지 대기할 수 있도록 도와주는 클래스
		CountDownLatch latch = new CountDownLatch(threadCount);

		for (int i = 0; i < threadCount; i++) {
			executorService.submit(() -> {
				try {
					stockService.decrease(1L, 1L);
				} finally {
					latch.countDown();
				}
			});
		}
		latch.await();
		Stock stock = stockRepository.findById(1L).orElseThrow();

		assertEquals(0, stock.getQuantity());

		// 테스트 실패 ! => 우리의 예상과는 다르게 Race condition이 일어남
		// Race condition : 둘이상의 쓰레드가 공유데이터에 Access할 수 있고,  동시에 변경하려고 할 때 발생
		// Thread 1: 100에서 1감소 = 99
		// Thread 2: Thread 1이 감소 되기 전, 재고 감소 = 100에서 1감소 = 99
		// 해결방법: 하나의 Thread 가 완료된 후에 다음 Thread 가 데이터에 접근할 수 있도록 처리
	}
}