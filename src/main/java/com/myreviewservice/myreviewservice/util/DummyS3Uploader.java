package com.myreviewservice.myreviewservice.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Component
public class DummyS3Uploader {
    public String upload(MultipartFile file){
        log.info("Uploading file:{}",file.getOriginalFilename());
        //더미 URL 생성
        return "https://dummy-s3-url.com/"+ file.getOriginalFilename();
    }
}
