package com.flavor.forge.Security.Bucket4j;

import com.flavor.forge.Model.ERole;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.Bucket4jLettuce;
import io.lettuce.core.codec.RedisCodec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.codec.ByteArrayCodec;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
public class RedisRateLimitBucket4jConfig {

    @Value("${forge.app.redis.uri}")
    private String redisURI;

    @Bean(destroyMethod = "shutdown")
    public RedisClient redisClient() {
        return RedisClient.create(redisURI);
    }

    @Bean
    public StatefulRedisConnection<String, byte[]> redisConnection(RedisClient redisClient) {
        RedisCodec<String, byte[]> codec = RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE);
        return redisClient.connect(codec);
    }

    @Bean
    ProxyManager<String> lettuceBasedProxyManager(StatefulRedisConnection<String, byte[]> connection) {
        return Bucket4jLettuce.casBasedBuilder(connection)
                    .expirationAfterWrite(
                            ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(
                                    Duration.ofHours(3)
                            )
                    )
                    .build();
    }

    @Bean
    public Map<String, Map<String, Supplier<BucketConfiguration>>> rateLimitPolicies() {
        Map<String, Map<String, Supplier<BucketConfiguration>>> policies = new HashMap<>();

        /*
            Authentication End Points
         */
        policies.put(
                "/api/v2/auth/signup",
                Stream.of(
                    buildPolicyMap(ERole.ANON.getRole(), 2L, 2L, Duration.ofMinutes(1))
                ).flatMap(m -> m.entrySet().stream())
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        );

        policies.put(
                "/api/v2/auth/login", Stream.of(
                    buildPolicyMap(ERole.ANON.getRole(), 5L, 5L, Duration.ofMinutes(15)),
                    buildPolicyMap(ERole.FREE.getRole(), 10L, 10L, Duration.ofMinutes(10))
                ).flatMap(m -> m.entrySet().stream())
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        );

        policies.put(
                "/api/v2/auth/refresh", Stream.of(
                        buildPolicyMap(ERole.FREE.getRole(), 10L, 10L, Duration.ofMinutes(10))
                ).flatMap(m -> m.entrySet().stream()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        );

        /*
            Search Users, recipes, comments, images
        */
        policies.put(
                "/api/v2/recipes/search", Stream.of(
                        buildPolicyMap(ERole.ANON.getRole(), 15L, 15L, Duration.ofMinutes(1)),
                        buildPolicyMap(ERole.FREE.getRole(), 30L, 30L, Duration.ofMinutes(1))
                ).flatMap(m -> m.entrySet().stream())
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        );

        policies.put(
                "/api/v2/recipes/search/**", Stream.of(
                        buildPolicyMap(ERole.ANON.getRole(), 15L, 15L, Duration.ofMinutes(1)),
                        buildPolicyMap(ERole.FREE.getRole(), 30L, 30L, Duration.ofMinutes(1))
                ).flatMap(m -> m.entrySet().stream())
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        );

        policies.put(
                "/api/v2/comments/search/**", Stream.of(
                        buildPolicyMap(ERole.ANON.getRole(), 15L, 15L, Duration.ofMinutes(1)),
                        buildPolicyMap(ERole.FREE.getRole(), 30L, 30L, Duration.ofMinutes(1))
                ).flatMap(m -> m.entrySet().stream()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        );

        policies.put(
                "/api/v2/users/search/**", Stream.of(
                        buildPolicyMap(ERole.ANON.getRole(), 15L, 15L, Duration.ofMinutes(1)),
                        buildPolicyMap(ERole.FREE.getRole(), 30L, 30L, Duration.ofMinutes(1))
                ).flatMap(m -> m.entrySet().stream()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        );

        policies.put(
                "/api/v2/images", Stream.of(
                        buildPolicyMap(ERole.ANON.getRole(), 30L, 30L, Duration.ofMinutes(1)),
                        buildPolicyMap(ERole.FREE.getRole(), 60L, 60L, Duration.ofMinutes(1))
                ).flatMap(m -> m.entrySet().stream()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        );

        policies.put(
                "/api/v2/users/profile/**", Stream.of(
                        buildPolicyMap(ERole.FREE.getRole(), 30L, 30L, Duration.ofMinutes(1))
                ).flatMap(m -> m.entrySet().stream()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        );

        policies.put(
                "/api/v2/recipes/liked/search/**", Stream.of(
                        buildPolicyMap(ERole.FREE.getRole(), 60L, 60L, Duration.ofMinutes(1))
                ).flatMap(m -> m.entrySet().stream()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        );

        policies.put(
                "/api/v2/users/followed/search/**", Stream.of(
                        buildPolicyMap(ERole.FREE.getRole(), 60L, 60L, Duration.ofMinutes(1))
                ).flatMap(m -> m.entrySet().stream()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        );

        /*
            Update, Delete User (create is sign-up)
         */
        policies.put(
                "/api/v2/users/update/**", Stream.of(
                        buildPolicyMap(ERole.FREE.getRole(), 10L, 10L, Duration.ofMinutes(1))
                ).flatMap(m -> m.entrySet().stream()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        );

        policies.put(
                "/api/v2/users/delete/**", Stream.of(
                        buildPolicyMap(ERole.FREE.getRole(), 10L, 10L, Duration.ofMinutes(1))
                ).flatMap(m -> m.entrySet().stream()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        );

        /*
            Create, Update, Delete Recipes
        */
        policies.put(
                "/api/v2/recipes/create", Stream.of(
                        buildPolicyMap(ERole.FREE.getRole(), 10L, 10L, Duration.ofMinutes(1))
                ).flatMap(m -> m.entrySet().stream()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        );

        policies.put(
                "/api/v2/recipes/update/**", Stream.of(
                        buildPolicyMap(ERole.FREE.getRole(), 10L, 10L, Duration.ofMinutes(1))
                ).flatMap(m -> m.entrySet().stream()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        );

        policies.put(
                "/api/v2/recipes/delete/**", Stream.of(
                        buildPolicyMap(ERole.FREE.getRole(), 10L, 10L, Duration.ofMinutes(1))
                ).flatMap(m -> m.entrySet().stream()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        );

        /*
            Create, Update, Delete Comments
         */
        policies.put(
                "/api/v2/comments/create", Stream.of(
                        buildPolicyMap(ERole.FREE.getRole(), 15L, 15L, Duration.ofMinutes(1))
                ).flatMap(m -> m.entrySet().stream()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        );

        policies.put(
                "/api/v2/comments/update/**", Stream.of(
                        buildPolicyMap(ERole.FREE.getRole(), 15L, 15L, Duration.ofMinutes(1))
                ).flatMap(m -> m.entrySet().stream()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        );

        policies.put(
                "/api/v2/comments/delete/**", Stream.of(
                        buildPolicyMap(ERole.FREE.getRole(), 15L, 15L, Duration.ofMinutes(1))
                ).flatMap(m -> m.entrySet().stream()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        );

        /*
            Add, Delete Liked Recipes
         */
        policies.put(
                "/api/v2/recipes/liked/add/**", Stream.of(
                        buildPolicyMap(ERole.FREE.getRole(), 20L, 20L, Duration.ofMinutes(1))
                ).flatMap(m -> m.entrySet().stream()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        );
        policies.put(
                "/api/v2/recipes/liked/delete/**", Stream.of(
                        buildPolicyMap(ERole.FREE.getRole(), 20L, 20L, Duration.ofMinutes(1))
                ).flatMap(m -> m.entrySet().stream()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        );

        /*
            Add, Delete Followed Creators
         */
        policies.put(
                "/api/v2/users/followed/add/**", Stream.of(
                        buildPolicyMap(ERole.FREE.getRole(), 20L, 20L, Duration.ofMinutes(1))
                ).flatMap(m -> m.entrySet().stream()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        );
        policies.put(
                "/api/v2/users/followed/delete/**", Stream.of(
                        buildPolicyMap(ERole.FREE.getRole(), 20L, 20L, Duration.ofMinutes(1))
                ).flatMap(m -> m.entrySet().stream()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        );

        /*
            Add, Delete Images
         */
        policies.put(
                "/api/v2/images/upload", Stream.of(
                        buildPolicyMap(ERole.FREE.getRole(), 10L, 10L, Duration.ofMinutes(1))
                ).flatMap(m -> m.entrySet().stream()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        );
        policies.put(
                "/api/v2/images/delete/**", Stream.of(
                        buildPolicyMap(ERole.FREE.getRole(), 10L, 10L, Duration.ofMinutes(1))
                ).flatMap(m -> m.entrySet().stream()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        );

        return policies;
    }

    private Map<String, Supplier<BucketConfiguration>> buildPolicyMap(String role, Long maxCapacity, Long tokenAmountRefilled, java.time.Duration refillPeriod) {
        return Map.of(
                role,
                () -> BucketConfiguration.builder()
                        .addLimit(
                                Bandwidth.builder()
                                        .capacity(maxCapacity)
                                        .refillGreedy(tokenAmountRefilled, refillPeriod)
                                        .build()
                        )
                        .build()
        );
    }
}
