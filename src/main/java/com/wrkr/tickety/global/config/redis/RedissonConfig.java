package com.wrkr.tickety.global.config.redis;

import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "spring.redis.enabled", havingValue = "true", matchIfMissing = true)
public class RedissonConfig {

/*    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Value("${spring.data.redis.password}")
    private String password;*/

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
/*        String redisAddress = "redis://" + host + ":" + port;
        config.useSingleServer().setAddress(redisAddress).setPassword(password);*/
        config.setCodec(new StringCodec());
        return org.redisson.Redisson.create(config);
    }
}