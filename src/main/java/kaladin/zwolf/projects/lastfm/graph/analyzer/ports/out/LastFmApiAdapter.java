package kaladin.zwolf.projects.lastfm.graph.analyzer.ports.out;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClient;

public abstract class LastFmApiAdapter {
    protected final Logger log;

    @Value("${lastfm_api_key}")
    protected String lastfmApiKey;

    @Value("${lastfm_api_secret}")
    protected String lastfmApiSecret;

    protected RestClient lastfmRestClient;

    protected LastFmApiAdapter(Logger log) {
        this.log = log;
    }
}
