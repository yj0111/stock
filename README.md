### 동시성 제어 해결 방법

1. **@Synchronized 이용** 

    ```java
    public **synchronized** void decrease(Long id, Long quantity) {
    
    }
    
    => synchronized를 붙여주면 하나의 Thread에만 접근 가능 
    ```

1. **MySQL 을 활용한 다양한 방법**
    1. **Pessimistic Lock**
        - **실제로 데이터에 Lock 을 걸어서 정합성을 맞추는 방법**
        - Exclusive lock 을 걸게되면 다른 트랜잭션에서는 lock 이 해제되기전에 데이터를 가져갈 수 없게됩니다.
        - 데드락이 걸릴 수 있기때문에 주의하여 사용하여야 합니다.
            
            ![사진1](https://github.com/yj0111/stock/assets/118320449/aa289fd0-338a-4f9d-b8da-2c4a9cd59c68)

        - Server 1이 Lock을 걸고 데이터를 가져가게 되면 다른 Server는 Server 1이 락을 해제하기 전까지는 데이터를 가지고 갈수 없다.
        
        ### 장점
        
        - 충돌이 빈번하게 일어난다면 Optimistic Lock 보다 성능이 좋을 수 있습니다.
        - Lock을 통해 업데이트를 제어하기 때문에 데이터 정합성이 보장됩니다.
        
        ### 단점
        
        - 별도의 락을 잡기 때문에 성능 감소가 있을 수 있습니다
    2. **Optimistic Lock**
        - 실제로 Lock 을 이용하지 않고 버전을 이용함으로써 정합성을 맞추는 방법입니다.
        - 먼저 데이터를 읽은 후에 update 를 수행할 때 현재 내가 읽은 버전이 맞는지 확인하며 업데이트 합니다.
        - 내가 읽은 버전에서 수정사항이 생겼을 경우에는 application에서 다시 읽은후에 작업을 수행해야 합니다.
            
            ![사진2](https://github.com/yj0111/stock/assets/118320449/30cb3161-349e-4148-b9d5-af48617f3e15)
            ![사진3](https://github.com/yj0111/stock/assets/118320449/abc2d7b9-9fe4-4a11-b46a-81a87de8c724)

        - Server 1이 먼저 Update 쿼리를 날리면 , version이 +1 돼서 2가 된다.
        - Server 2가 Update 쿼리를 날리면 버전이 안맞아서 Update 실패 ⇒ Application 다시 읽고 다시 처리해야함
        
        ### 장점
        
        - 별도의 락을 잡지 않아 Pessimistic Lock보다 성능상 이점
        
        ### 단점
        
        - 업데이트 실패 시 재 시도하는 로직을 개발자가 하나하나 작성해야함
        
        ### 충돌이 빈번하다면 Pessimistic Lock , 아니라면 Optimistic Lock
        
    3. **Named Lock**
        - 이름을 가진 metadata locking
        - 이름을 가진 lock 을 획득한 후 해제할때까지 다른 세션은 이 lock 을 획득할 수 없도록 합니다.
        - 주의할 점으로는 transaction 이 종료될 때 lock 이 자동으로 해제되지 않습니다.
        - 별도의 명령어로 해제를 수행해주거나 선점 시간이 끝나야 해제됩니다.
        - 분산락 구현할 때 사용
        - SQL 에서는 get-lock 명령어를 통해 named-lock을 획득 할 수 있고, release-lock을 통해 lock을 해제 할 수 있습니다.
            
            ![사진4](https://github.com/yj0111/stock/assets/118320449/c1bd5ecb-50c5-4c29-9cd3-7c579229fbe7)

        - **Pessimistic Lock은 Stock에 대해 Lock을 걸었다면, Named Lock은 Stock에 Lock을 걸지않고, 별도의 공간에 Lock을 건다.**
        - Session 1이 Lock을 걸면, 다른 Session에서는 Session1이 Lock을 해제한 뒤에, Lock을 획득 할 수 있게 됩니다.
        
        ### 장점
        
        - Pessimistic Lock은 타임아웃 구현하기 힘들지만
        - NamedLock은 타임아웃 구현하기 쉬움
        - 데이터 삽입 시에 정합성을 맞춰야 하는 경우에도 사용가능
        
        ### 단점
        
        - 트랜젝션 종료 시 락 해제, 세션 관리 잘해야함
        - 실제로 사용 할때는 구현 방법 복잡

1. **Redis를 활용하는 방법**
    1. Lettuce
        
        ![사진5](https://github.com/yj0111/stock/assets/118320449/1d890870-36ef-4318-a87f-2e8f51ad2789)

        - setnx(set if not exist) 명령어를 활용하여 분산락 구현 가능
            - 키가 없을 때만 값을 설정하고, 존재하면 설정하지 않는 방식으로 분산락을 획득할 수 있음
        - **Key-Value를 Set할 때 값이 없을 때만 Set하는 방법**
            - Key-Value를 설정할 때 값이 이미 존재하는지 여부를 확인하고, 값이 없을 때만 설정할 수 있음
        - **Spin Lock 방식**
            - Lock 을 획득하려는 Thread가 락을 사용할 수 있는지 반복적으로 확인하면서 Lock 획득을 시도하는 방식
            - Thread Sleep 을 이용하여 재시도하는 텀을 두어야 하며, 동시에 많은 Thread가 대기 상태일 경우 Redis에 부하를 줄 수 있습니다.
                - 재시도가 필요하지 않은 경우에는 은 Lettuce를 활용
            - Retry 로직은 개발자가 작성해줘야 함
            
    2. Redisson
        ![사진6](https://github.com/yj0111/stock/assets/118320449/5b8c6a28-5f12-4848-8c0b-9047af9daa96)
        
        - pub-sub 방식으로 Lock 구현이 되어있기 때문에 lettuce 와 비교했을 때 Redis 에 부하가 덜 간다.
        - 별도의 라이브러리를 사용
        - 별도의 Retry 로직을 작성하지 않아
        - Lock 을 라이브러리 차원에서 제공해주기 때문에 사용법을 공부해야 한다.
            
            => 재시도가 필요한 경우에는 Redisson를 활용
