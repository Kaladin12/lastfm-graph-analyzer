package kaladin.zwolf.projects.lastfm.graph.analyzer.adapters.out;

import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.LastfmSessionTokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

@Component
public class LastFmApiAdapter {
    private final Logger log = LoggerFactory.getLogger(LastFmApiAdapter.class);

    private RestClient lastfmRestClient;

    @Value("${lastfm_api_key}")
    private String lastfmApiKey;

    @Value("${lastfm_api_secret}")
    private String lastfmApiSecret;

    public LastFmApiAdapter(RestClient lastfmRestClient) {
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

    public ResponseEntity<String> getArtistInfo(String name) {
        return lastfmRestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("method", "artist.getInfo")
                        .queryParam("artist", name)
                        .queryParam("api_key", lastfmApiKey)
                        .queryParam("format", "json")
                        .build())
                .retrieve().toEntity(String.class);
    }

    private String getApiSignature(String base) throws NoSuchAlgorithmException {
        return DigestUtils.md5DigestAsHex(base.getBytes(StandardCharsets.UTF_8)).toUpperCase();

    }

}
