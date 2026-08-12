package com.example.statistics.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Redis 설정.
 *
 * 사전 집계(INCRBY / ZINCRBY / HINCRBY) + 조회 모두
 * StringRedisTemplate 한 빈으로 처리한다.
 *
 * Spring Boot 의 RedisAutoConfiguration 이 동일한 빈을 자동 등록하지만,
 * 명시적으로 둠으로써 의도 명확 + 미래 확장 (JSON Serializer 등) 자리 마련.
 */
@Configuration
public class RedisConfig {

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }
}
