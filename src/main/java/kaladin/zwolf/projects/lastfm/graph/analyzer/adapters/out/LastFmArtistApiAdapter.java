package kaladin.zwolf.projects.lastfm.graph.analyzer.adapters.out;

import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.response.LastfmArtistInfoResponse;
import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.response.LastfmGetLibraryArtistsResponse;
import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.response.LastfmSessionTokenResponse;
import kaladin.zwolf.projects.lastfm.graph.analyzer.ports.out.LastFmApiAdapter;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

@Component
public class LastFmArtistApiAdapter extends LastFmApiAdapter {

    public LastFmArtistApiAdapter(RestClient lastfmRestClient) {
        super(LoggerFactory.getLogger(LastFmArtistApiAdapter.class));
        this.lastfmRestClient = lastfmRestClient;
    }

    public ResponseEntity<LastfmSessionTokenResponse> getWebServiceSession(String token) throws NoSuchAlgorithmException {
        StringBuilder sb = new StringBuilder();
        final String method = "auth.getSession";
        String md5Signature = getApiSignature(
                sb.append("api_key").append(lastfmApiKey)
                        .append("method").append(method)
                        .append("token").append(token)
                        .append(lastfmApiSecret)
                        .toString());

        log.info("MD5 BASE: {}", md5Signature);

        return lastfmRestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("method",method)
                        .queryParam("token", token)
                        .queryParam("api_key", lastfmApiKey)
                        .queryParam("api_sig", md5Signature)
                        .build())
                .retrieve().toEntity(LastfmSessionTokenResponse.class);
    }

    public ResponseEntity<LastfmArtistInfoResponse> getArtistInfo(String name, String mbid) {
        return lastfmRestClient.post()
                .uri(uriBuilder -> {
                    uriBuilder = uriBuilder
                            .queryParam("method", "artist.getInfo")
                            .queryParam("api_key", lastfmApiKey)
                            .queryParam("format", "json");
                    if (mbid != null) {
                       return uriBuilder.queryParam("mbid", mbid).build();
                    }
                    return uriBuilder.queryParam("artist", name).build();
                })
                .retrieve().toEntity(LastfmArtistInfoResponse.class);
    }

    public ResponseEntity<LastfmGetLibraryArtistsResponse> getLibraryArtists(String username, String page) {
        return lastfmRestClient.post()
                .uri(uriBuilder -> {
                    uriBuilder = getLibraryArtistsUriBuilder(username, uriBuilder);
                    if (page != null) {
                        return uriBuilder.queryParam("page", page).build();
                    }
                    return uriBuilder.build();
                })
                .retrieve().toEntity(LastfmGetLibraryArtistsResponse.class);
    }

    private String getApiSignature(String base) {
        return DigestUtils.md5DigestAsHex(base.getBytes(StandardCharsets.UTF_8)).toUpperCase();
    }

    private UriBuilder getLibraryArtistsUriBuilder(String username, UriBuilder uriBuilder) {
        return uriBuilder
                .queryParam("method","library.getArtists")
                .queryParam("user", username)
                .queryParam("limit", 100)
                .queryParam("api_key", lastfmApiKey)
                .queryParam("format", "json");
    }

}
