package com.misim.mitube_v1.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

@Configuration
public class RedisConfig {

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory();
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(LettuceConnectionFactory f) {
        return new StringRedisTemplate(f);
    }

    @Bean
    public DefaultRedisScript<Long> incrAndGetScript() {
        DefaultRedisScript<Long> s = new DefaultRedisScript<>();
        s.setResultType(Long.class);
        s.setScriptText(
            // KEYS[1]=views:counter, KEYS[2]=views:base, ARGV[1]=videoId, ARGV[2]=delta
            "local d = redis.call('HINCRBY', KEYS[1], ARGV[1], tonumber(ARGV[2])) " +
                "local b = redis.call('HGET', KEYS[2], ARGV[1]) " +
                "if not b then b = '0' end " +
                "return d + tonumber(b)"
        );
        return s;
    }
}
