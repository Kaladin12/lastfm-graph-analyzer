package kaladin.zwolf.projects.lastfm.graph.analyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class LastfmGraphAnalyzerApplication {

    public static void main(String[] args) {
        SpringApplication.run(LastfmGraphAnalyzerApplication.class, args);
    }
}
