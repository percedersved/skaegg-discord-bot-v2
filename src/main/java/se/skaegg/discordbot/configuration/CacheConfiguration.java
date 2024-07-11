package se.skaegg.discordbot.configuration;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfiguration {

    /*
     * CacheManager for TriviaService. Cache will renew every 1 day.
     */
    @Bean
    public CaffeineCacheManager TriviaServiceCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("triviaServiceCache");
        cacheManager.setCaffeine(caffeineCacheBuilder(TimeUnit.DAYS, 1L));
        return cacheManager;
    }

    Caffeine<Object, Object> caffeineCacheBuilder(TimeUnit timeUnit, long duration) {
        return Caffeine.newBuilder()
                .expireAfterWrite(duration, timeUnit)
                .maximumSize(100);
    }
}
