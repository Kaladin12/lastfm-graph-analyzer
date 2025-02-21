package kaladin.zwolf.projects.lastfm.graph.analyzer.adapters.out;

import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.response.LastfmTopTracksResponse;
import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.response.LastfmTrackInfoResponse;
import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.response.enums.Period;
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

    public ResponseEntity<LastfmTopTracksResponse> getTracks(String user, Period period, String page) {
        return lastfmRestClient.post()
                .uri(uriBuilder -> {
                    uriBuilder = getBaseUriBuilder(uriBuilder, "user.getTopTracks")
                            .queryParam("user", user)
                            .queryParam("period", period.getValue())
                            .queryParam("limit", 100);
                    if (page != null) {
                        return uriBuilder.queryParam("page", page).build();
                    }
                    return uriBuilder.build();
                })
                .retrieve().toEntity(LastfmTopTracksResponse.class);
    }

    public ResponseEntity<LastfmTrackInfoResponse> getTrackInfo(String track, String mbid, String artist) {
        return lastfmRestClient.post()
                .uri(uriBuilder -> {
                    uriBuilder = getBaseUriBuilder(uriBuilder, "track.getInfo")
                            .queryParam("artist", artist)
                            .queryParam("user", "KaladinIsThe");
                    if (mbid != null && !mbid.isBlank()) {
                        return uriBuilder.queryParam("mbid", mbid).build();
                    }
                    return uriBuilder.queryParam("track", track).build();
                }).retrieve().toEntity(LastfmTrackInfoResponse.class);
    }

    private UriBuilder getBaseUriBuilder(UriBuilder uriBuilder, String method) {
        return uriBuilder
                .queryParam("method", method)
                .queryParam("api_key", lastfmApiKey)
                .queryParam("format", "json");
    }
}
