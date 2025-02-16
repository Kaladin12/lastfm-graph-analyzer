package kaladin.zwolf.projects.lastfm.graph.analyzer.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfiguration {
    // http://localhost:8080/api/v1/lastfm/callback

    @Value("${lastfm_api_url}")
    private String lastfmApiUrl;

    @Bean
    public RestClient lastfmRestClient() {
        return RestClient.builder()
                .baseUrl(lastfmApiUrl)
                .build();
    }
}
