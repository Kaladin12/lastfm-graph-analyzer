package kaladin.zwolf.projects.lastfm.graph.analyzer.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.web.client.RestClient;

import java.util.List;

@Configuration
public class RestClientConfiguration {
    // http://localhost:8080/api/v1/lastfm/callback

    @Value("${lastfm_api_url}")
    private String lastfmApiUrl;

    @Bean
    public RestClient lastfmRestClient() {
        var messageConverter = new MappingJackson2XmlHttpMessageConverter();
        messageConverter.setSupportedMediaTypes(List.of(MediaType.APPLICATION_XML, MediaType.TEXT_XML));

        var jsonMessageConverter = new MappingJackson2HttpMessageConverter();
        jsonMessageConverter.setSupportedMediaTypes(List.of(MediaType.APPLICATION_JSON));
        return RestClient.builder()
                .baseUrl(lastfmApiUrl)
                .messageConverters(List.of(messageConverter,jsonMessageConverter))
                .build();
    }
}
