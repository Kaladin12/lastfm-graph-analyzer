package kaladin.zwolf.projects.lastfm.graph.analyzer.adapters.out;

import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.response.LastfmTrackResponse;
import kaladin.zwolf.projects.lastfm.graph.analyzer.ports.out.LastFmApiAdapter;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

@Component
public class LastFmTrackApiAdapter extends LastFmApiAdapter {

    public LastFmTrackApiAdapter(RestClient lastfmRestClient) {
        super(LoggerFactory.getLogger(LastFmTrackApiAdapter.class));
        this.lastfmRestClient = lastfmRestClient;
    }

    public ResponseEntity<LastfmTrackResponse> getTracks(String user, String period, String page) {
        return lastfmRestClient.post()
                .uri(uriBuilder -> {
                    uriBuilder = getBaseUriBuilder(uriBuilder)
                            .queryParam("user", user)
                            .queryParam("period", period != null ? period : "overall")
                            .queryParam("limit", 100);
                    if (page != null) {
                        return uriBuilder.queryParam("page", page).build();
                    }
                    return uriBuilder.build();
                })
                .retrieve().toEntity(LastfmTrackResponse.class);
    }

    private UriBuilder getBaseUriBuilder(UriBuilder uriBuilder) {
        return uriBuilder
                .queryParam("method", "user.getTopTracks")
                .queryParam("api_key", lastfmApiKey)
                .queryParam("format", "json");
    }
}
