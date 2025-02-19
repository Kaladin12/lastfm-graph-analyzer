package kaladin.zwolf.projects.lastfm.graph.analyzer.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class ConcurrentConfiguration {
    @Value("${thread.count}")
    private int threadCount;

    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(threadCount);
        executor.setMaxPoolSize(threadCount);
        executor.setQueueCapacity(5);
        executor.setThreadNamePrefix("lastfm-graph-analyzer-");
        executor.initialize();
        return executor;
    }
}
