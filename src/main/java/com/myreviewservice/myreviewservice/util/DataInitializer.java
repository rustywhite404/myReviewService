package com.myreviewservice.myreviewservice.util;

import com.myreviewservice.myreviewservice.entity.Product;
import com.myreviewservice.myreviewservice.repository.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class DataInitializer implements CommandLineRunner {
    //CommandLineRunner : 애플리케이션 초기화 또는 실행 후 특정 작업을 수행하기 위해 사용되는 인터페이스.
    //시작 후 초기 데이터 설정이나 외부 API 호출, 캐시 초기화 등의 작업에 유용하다.

    private final ProductRepository productRepository;
    @Override
    public void run(String... args) throws Exception {
        //이미 데이터가 존재하면 삽입하지 않음
        if(productRepository.count()==0){
            for(int i=0;i<10;i++) {
                productRepository.save(Product.builder()
                        .reviewCount(0L)
                        .score(0.0).build());
            }
        }
    }
}
