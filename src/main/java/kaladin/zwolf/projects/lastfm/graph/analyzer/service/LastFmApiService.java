package kaladin.zwolf.projects.lastfm.graph.analyzer.service;

import kaladin.zwolf.projects.lastfm.graph.analyzer.adapters.out.LastFmApiAdapter;
import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.LastfmArtistInfo;
import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.LastfmGetLibraryArtistsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

@Service
public class LastFmApiService {
    private final Logger log = LoggerFactory.getLogger(LastFmApiService.class);

    private LastFmApiAdapter lastFmApiAdapter;
    private ArtistRepositoryService artistRepositoryService;

    public LastFmApiService(LastFmApiAdapter lastFmApiAdapter, ArtistRepositoryService artistRepositoryService) {
        this.lastFmApiAdapter = lastFmApiAdapter;
        this.artistRepositoryService = artistRepositoryService;
    }

    public String getSessionKey(String token) {
        try {
            return Objects.requireNonNull(lastFmApiAdapter.getWebServiceSession(token).getBody())
                    .getSession().getKey();
        } catch (NoSuchAlgorithmException e) {
            log.error("Unable to retrieve session key due to error: {}", e.getMessage());
        }
        return null;
    }

    public LastfmArtistInfo getArtistInfo(String artistName) {
        LastfmArtistInfo artistInfo =  lastFmApiAdapter.getArtistInfo(artistName).getBody();
        setTags(artistInfo);

        if (artistInfo.getArtist().getMbid() == null) {
            artistInfo.getArtist().setMbid(UUID.nameUUIDFromBytes(artistInfo.getArtist().getName().getBytes()).toString());
            log.info("Artist {} had null Mbid, setting it to {}", artistName, artistInfo.getArtist().getMbid());
        }
        boolean exists = artistRepositoryService.saveArtistInfoIfNotExist(artistInfo);
        if (!exists) {
            log.warn("ARTIST {} ALREADY EXISTED IN DB", artistInfo.getArtist().getMbid());
            return null;
        }
        log.info("ARTIST {} SAVED", artistInfo.getArtist().getMbid());
        return artistInfo;
    }

    public void getLibraryArtists(String username) {
        LastfmGetLibraryArtistsResponse response = lastFmApiAdapter.getLibraryArtists(username, null).getBody();
        handleFetchedPageData(response.getArtists().getArtist().stream());
        int totalPages = 2; //Integer.parseInt(response.getArtists().getAttributes().getTotalPages());
        int page = Integer.parseInt(response.getArtists().getAttributes().getPage()) + 1;

        while (page <= totalPages) {
            List<CompletableFuture<LastfmGetLibraryArtistsResponse>> artistLibraryThreads = new ArrayList<>();

            for (int thread = 0; thread < 3 && page <= totalPages; thread++, page++) {
                artistLibraryThreads.add(getLibraryArtistAsync(username, String.valueOf(page)));
            }

            // Converts the threads list into an array, using CompletableFuture as allocation size
            CompletableFuture.allOf(artistLibraryThreads.toArray(new CompletableFuture[0])).join();

            Stream<LastfmGetLibraryArtistsResponse. Artist> fetchedPagesArtists = artistLibraryThreads.stream()
                    .flatMap(fetchedPageData -> {
                        try {
                            return fetchedPageData.get().getArtists().getArtist().stream();
                        } catch (InterruptedException | ExecutionException ex) {
                            throw new RuntimeException(ex);
                        }});
            handleFetchedPageData(fetchedPagesArtists);
            log.info("{}/{} PAGES COMPLETED", page-1, totalPages);
        }
    }

    private void handleFetchedPageData(Stream<LastfmGetLibraryArtistsResponse.Artist> fetchedPageArtists) {
        fetchedPageArtists
                .map(artist -> getArtistInfo(artist.getName()))
                .filter(Objects::nonNull)
                .forEach(artistInfo -> log.info("ARTIST: {}, PLAYCOUNT: {}",
                        artistInfo.getArtist().getName(), artistInfo.getArtist().getStats().getPlaycount()));
    }

    @Async
    protected CompletableFuture<LastfmGetLibraryArtistsResponse> getLibraryArtistAsync(String username, String page) {
        return CompletableFuture.completedFuture(
                lastFmApiAdapter.getLibraryArtists(username, page).getBody()
        );
    }

    @Async
    protected CompletableFuture<LastfmArtistInfo> getArtistInfoAsync(String name) {
        return CompletableFuture.completedFuture(
                lastFmApiAdapter.getArtistInfo(name).getBody()
        );
    }

    private void setTags(LastfmArtistInfo artistInfo) {
        var tags = artistInfo.getArtist().getTags().getTag();
        artistInfo.getArtist().getTags().setTag(tags.subList(0, Math.min(3, tags.size())));
    }
}
