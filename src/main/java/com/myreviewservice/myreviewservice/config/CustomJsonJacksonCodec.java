package com.myreviewservice.myreviewservice.config;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.netty.buffer.Unpooled;
import org.redisson.client.codec.BaseCodec;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;
import org.redisson.codec.JsonJacksonCodec;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class CustomJsonJacksonCodec extends JsonJacksonCodec  {

    public CustomJsonJacksonCodec() {
        super(new ObjectMapper()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL) // null 값 제외
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)  // 빈 객체 허용
                .deactivateDefaultTyping() // 클래스 정보 비활성화
        );
    }

    @Override
    public Decoder<Object> getMapKeyDecoder() {
        return (buf, state) -> {
            byte[] bytes = new byte[buf.readableBytes()];
            buf.readBytes(bytes);
            return new String(bytes, StandardCharsets.UTF_8); // 키를 문자열로 디코딩
        };
    }

    @Override
    public Encoder getMapKeyEncoder() {
        return in -> {
            byte[] bytes = in.toString().getBytes(StandardCharsets.UTF_8);
            return Unpooled.wrappedBuffer(bytes); // ByteBuf로 반환
        }; // 키는 문자열 그대로 저장
    }
}
