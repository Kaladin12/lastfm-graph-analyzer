package kaladin.zwolf.projects.lastfm.graph.analyzer.service;

import kaladin.zwolf.projects.lastfm.graph.analyzer.Util.ChunkIterator;
import kaladin.zwolf.projects.lastfm.graph.analyzer.adapters.out.LastFmApiAdapter;
import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.LastfmArtistInfo;
import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.LastfmGetLibraryArtistsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
public class LastFmApiService {
    private final Logger log = LoggerFactory.getLogger(LastFmApiService.class);

    private final LastFmApiAdapter lastFmApiAdapter;
    private final ArtistRepositoryService artistRepositoryService;

    @Value("${thread.count}")
    private int THREAD_COUNT;

    public LastFmApiService(LastFmApiAdapter lastFmApiAdapter,
                            ArtistRepositoryService artistRepositoryService) {
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
        LastfmArtistInfo artistInfo = lastFmApiAdapter.getArtistInfo(artistName).getBody();
        artistRepositoryService.saveArtistInfoIfNotExist(artistInfo);
        return artistInfo;
    }

    public void getLibraryArtists(String username) {
        LastfmGetLibraryArtistsResponse response = lastFmApiAdapter.getLibraryArtists(username, null).getBody();
        handleFetchedPageData(response.getArtists().getArtist().stream());
        int totalPages = 2; //Integer.parseInt(response.getArtists().getAttributes().getTotalPages());
        int page = Integer.parseInt(response.getArtists().getAttributes().getPage()) + 1;

        while (page <= totalPages) {
            List<CompletableFuture<LastfmGetLibraryArtistsResponse>> artistLibraryThreads = new ArrayList<>();

            for (int thread = 0; thread < THREAD_COUNT && page <= totalPages; thread++, page++) {
                artistLibraryThreads.add(getLibraryArtistAsync(username, String.valueOf(page)));
            }

            // Converts the threads list into an array, using CompletableFuture as allocation size
            CompletableFuture.allOf(artistLibraryThreads.toArray(new CompletableFuture[0])).join();

            Stream<LastfmGetLibraryArtistsResponse.Artist> fetchedPagesArtists = artistLibraryThreads
                    .stream().flatMap(this::artistStream);
            handleFetchedPageData(fetchedPagesArtists);
            log.info("{}/{} PAGES COMPLETED", page - 1, totalPages);
        }
    }

    private void handleFetchedPageData(Stream<LastfmGetLibraryArtistsResponse.Artist> fetchedPageArtists) {
        Stream<List<LastfmGetLibraryArtistsResponse.Artist>> stream = getStreamAsChunks(fetchedPageArtists, THREAD_COUNT);

        stream.flatMap(this::artistInfoStream)
                .filter(Objects::nonNull)
                .forEach(artistInfo -> {
                    artistRepositoryService.saveArtistInfoIfNotExist(artistInfo);
                    log.info("ARTIST: {}, PLAYCOUNT: {}",
                            artistInfo.getArtist().getName(),
                            artistInfo.getArtist().getStats().getPlaycount());
                });
    }

    @Async
    protected CompletableFuture<LastfmGetLibraryArtistsResponse> getLibraryArtistAsync(String username, String page) {
        return CompletableFuture.completedFuture(
                lastFmApiAdapter.getLibraryArtists(username, page).getBody()
        );
    }

    private Stream<LastfmGetLibraryArtistsResponse.Artist> artistStream(
            CompletableFuture<LastfmGetLibraryArtistsResponse> fetchedPageData) {
        try {
            return fetchedPageData.get().getArtists().getArtist().stream();
        } catch (InterruptedException | ExecutionException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Async
    protected CompletableFuture<LastfmArtistInfo> getArtistInfoAsync(String name) {
        return CompletableFuture.completedFuture(
                lastFmApiAdapter.getArtistInfo(name).getBody()
        );
    }


    private Stream<LastfmArtistInfo> artistInfoStream(List<LastfmGetLibraryArtistsResponse. Artist> artists) {
        List<CompletableFuture<LastfmArtistInfo>> threads = new ArrayList<>();
        log.info("SIZE: {}", artists.size());
        for (int thread = 0; thread < THREAD_COUNT && thread < artists.size(); thread++) {
            threads.add(getArtistInfoAsync(artists.get(thread).getName()));
        }
        CompletableFuture.allOf(threads.toArray(new CompletableFuture[0])).join();
        return threads.stream().flatMap(fetchedArtist -> {
            try {
                return Stream.ofNullable(fetchedArtist.get());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }


    private Stream<List<LastfmGetLibraryArtistsResponse.Artist>> getStreamAsChunks(
            Stream<LastfmGetLibraryArtistsResponse.Artist> artists, int size) {
        // https://stackoverflow.com/questions/27583623/is-there-an-elegant-way-to-process-a-stream-in-chunks
        var listIterator = new ChunkIterator<>(artists.iterator(), size);
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(listIterator, Spliterator.ORDERED),
                false
        );
    }
}
