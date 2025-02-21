package kaladin.zwolf.projects.lastfm.graph.analyzer.service;

import kaladin.zwolf.projects.lastfm.graph.analyzer.util.ChunkIterator;
import kaladin.zwolf.projects.lastfm.graph.analyzer.adapters.out.LastFmArtistApiAdapter;
import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.entity.LastfmArtist;
import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.response.LastfmArtistInfoResponse;
import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.response.LastfmGetLibraryArtistsResponse;
import kaladin.zwolf.projects.lastfm.graph.analyzer.service.mapper.LastfmArtistMapper;
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
public class LastFmArtistApiService {
    private final Logger log = LoggerFactory.getLogger(LastFmArtistApiService.class);

    private final LastFmArtistApiAdapter lastFmArtistApiAdapter;
    private final MusicRepositoryService musicRepositoryService;

    @Value("${thread.count}")
    private int THREAD_COUNT;

    public LastFmArtistApiService(LastFmArtistApiAdapter lastFmArtistApiAdapter,
                                  MusicRepositoryService musicRepositoryService) {
        this.lastFmArtistApiAdapter = lastFmArtistApiAdapter;
        this.musicRepositoryService = musicRepositoryService;
    }

    public String getSessionKey(String token) {
        try {
            return Objects.requireNonNull(lastFmArtistApiAdapter.getWebServiceSession(token).getBody())
                    .getSession().getKey();
        } catch (NoSuchAlgorithmException e) {
            log.error("Unable to retrieve session key due to error: {}", e.getMessage());
        }
        return null;
    }

    public LastfmArtist getArtistInfo(String name) {
        LastfmArtistInfoResponse artistInfo = lastFmArtistApiAdapter.getArtistInfo(name, null).getBody();
        LastfmArtist artist = LastfmArtistMapper.fromArtistInfoToEntity(artistInfo);
        musicRepositoryService.saveArtistInfoIfNotExist(artist);
        return artist;
    }

    public void getLibraryArtists(String username) {
        LastfmGetLibraryArtistsResponse response = lastFmArtistApiAdapter.getLibraryArtists(username, null).getBody();
        handleFetchedPageData(response.getArtists().getArtist().stream());
        int totalPages = 20; //Integer.parseInt(response.getArtists().getAttributes().getTotalPages());
        int page = Integer.parseInt(response.getArtists().getPageAttributes().getPage()) + 1;

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
                .map(LastfmArtistMapper::fromArtistInfoToEntity)
                .forEach(artist -> {
                    musicRepositoryService.saveArtistInfoIfNotExist(artist);
                    log.debug("ARTIST: {}, PLAYCOUNT: {}",
                            artist.getName(),
                            artist.getStats().getPlaycount());
                });
    }

    @Async
    protected CompletableFuture<LastfmGetLibraryArtistsResponse> getLibraryArtistAsync(String username, String page) {
        return CompletableFuture.completedFuture(
                lastFmArtistApiAdapter.getLibraryArtists(username, page).getBody()
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
    protected CompletableFuture<LastfmArtistInfoResponse> getArtistInfoAsync(String name, String mbid) {
        var artistInfoRes = CompletableFuture.completedFuture(lastFmArtistApiAdapter.getArtistInfo(name, mbid).getBody());
        try {
            if (artistInfoRes.get().getArtist() == null) {
                return CompletableFuture.completedFuture(lastFmArtistApiAdapter.getArtistInfo(name, null).getBody());
            }
        } catch (Exception e) {
            return CompletableFuture.completedFuture(null);
        }
        return artistInfoRes;
    }


    private Stream<LastfmArtistInfoResponse> artistInfoStream(List<LastfmGetLibraryArtistsResponse. Artist> artists)  {
        List<CompletableFuture<LastfmArtistInfoResponse>> threads = new ArrayList<>();
        log.debug("SIZE: {}", artists.size());
        for (int thread = 0; thread < THREAD_COUNT && thread < artists.size(); thread++) {
            threads.add(getArtistInfoAsync(artists.get(thread).getName(),
                    artists.get(thread).getMbid().isBlank() ? null : artists.get(thread).getMbid()));
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
