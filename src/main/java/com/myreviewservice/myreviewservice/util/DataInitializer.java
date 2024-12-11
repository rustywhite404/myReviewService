package com.myreviewservice.myreviewservice.util;

import com.myreviewservice.myreviewservice.entity.Product;
import com.myreviewservice.myreviewservice.repository.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class DataInitializer implements CommandLineRunner {

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
