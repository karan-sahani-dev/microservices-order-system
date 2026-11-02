package com.karan.inventory_service.config;

import com.karan.inventory_service.dto.InventoryResponse;
import org.springframework.boot.cache.autoconfigure.CacheProperties;
import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
public class RedisCacheConfig {
    @Bean
    public RedisCacheManagerBuilderCustomizer inventoryCacheCustomizer(
            CacheProperties cacheProperties
    ) {
        JacksonJsonRedisSerializer<InventoryResponse> valueSerializer =
                new JacksonJsonRedisSerializer<>(InventoryResponse.class);

        RedisCacheConfiguration inventoryConfiguration =
                RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(cacheProperties.getRedis().getTimeToLive())
                        .disableCachingNullValues()
                        .serializeKeysWith(
                                RedisSerializationContext.SerializationPair
                                        .fromSerializer(new StringRedisSerializer())
                        )
                        .serializeValuesWith(
                                RedisSerializationContext.SerializationPair
                                        .fromSerializer(valueSerializer)
                        );

        return builder -> builder
                .withCacheConfiguration("inventory-stock", inventoryConfiguration)
                .transactionAware();
    }
}
