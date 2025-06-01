package kaladin.zwolf.projects.lastfm.graph.analyzer.service;

import kaladin.zwolf.projects.lastfm.graph.analyzer.util.ChunkIterator;
import kaladin.zwolf.projects.lastfm.graph.analyzer.adapters.out.LastFmArtistApiAdapter;
import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.entity.mongo.LastfmArtist;
import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.response.LastfmArtistInfoResponse;
import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.response.LastfmGetLibraryArtistsResponse;
import kaladin.zwolf.projects.lastfm.graph.analyzer.service.mapper.LastfmMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
public class LastFmArtistApiService {
    private final Logger log = LoggerFactory.getLogger(LastFmArtistApiService.class);

    private final LastFmArtistApiAdapter lastFmArtistApiAdapter;
    private final MusicRepositoryService musicRepositoryService;

    @Value("${thread.count}")
    private int THREAD_COUNT;

    @Value("${mongo.connection-pool.max}")
    private int MAX_CONNECTION_POOL_SIZE;

    @Autowired
    @Qualifier("applicationTaskExecutor")
    private AsyncTaskExecutor asyncTaskExecutor;

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

    public Optional<LastfmArtist> getArtistInfo(String name) {
        return lastFmArtistApiAdapter.getArtistInfo(name, null)
                .filter(responseEntity -> Objects.nonNull(responseEntity.getBody()))
                .map(responseEntity -> LastfmMapper.fromArtistInfoToEntity(responseEntity.getBody()))
                .map(lastfmArtist -> {
                    musicRepositoryService.saveArtistInfoIfNotExist(lastfmArtist);
                    return lastfmArtist;
                });
    }

    @Async
    public void getLibraryArtists(String username) {
        LastfmGetLibraryArtistsResponse response = lastFmArtistApiAdapter.getLibraryArtists(username, null).getBody();
        // the APIs should always return Optional ... TODO
        if (response == null || response.getArtists() == null) {
            return;
        }
        handleFetchedPageData(response.getArtists().getArtist().stream());

        int totalPages = Math.min(20, Integer.parseInt(response.getArtists().getPageAttributes().getTotalPages()));
        AtomicInteger page = new AtomicInteger(Integer.parseInt(response.getArtists().getPageAttributes().getPage()) + 1);

        while (page.get() <= totalPages) {
            List<CompletableFuture<LastfmGetLibraryArtistsResponse>> artistLibraryThreads = new ArrayList<>();

            IntStream.range(1, 20).forEach(thread -> {
                artistLibraryThreads.add(asyncTaskExecutor.submitCompletable(() ->
                        lastFmArtistApiAdapter.getLibraryArtists(username, String.valueOf(thread)).getBody()));
                log.info("Fetched artist library thread {}", thread);
                page.addAndGet(1);
            });

            CompletableFuture.allOf(artistLibraryThreads.toArray(new CompletableFuture[0])).join();
            // TODO: Need to handle first page.
            Stream<LastfmGetLibraryArtistsResponse.Artist> fetchedPagesArtists = artistLibraryThreads
                   .stream().flatMap(this::artistStream);
            handleFetchedPageData(fetchedPagesArtists);
            log.info("{}/{} PAGES COMPLETED", page.get() - 1, totalPages);
        }
    }

    protected void handleFetchedPageData(Stream<LastfmGetLibraryArtistsResponse.Artist> fetchedPageArtists) {
        Stream<List<LastfmGetLibraryArtistsResponse.Artist>> stream = new ChunkIterator<>(fetchedPageArtists.iterator(), THREAD_COUNT).stream();

        //AtomicInteger counter = new AtomicInteger(0);
        Spliterator<LastfmArtist> spliterator = stream.flatMap(this::artistInfoStream)
                .filter(Objects::nonNull)
                .map(LastfmMapper::fromArtistInfoToEntity)
                //.collect(Collectors.groupingBy(artist -> counter.getAndIncrement()/MAX_CONNECTION_POOL_SIZE))
                .spliterator();

        while (true) {
            List<LastfmArtist> chunk = new ArrayList<>(MAX_CONNECTION_POOL_SIZE);
            for (int i = 0; i < MAX_CONNECTION_POOL_SIZE ; i++) {
                if (!spliterator.tryAdvance(chunk::add)) {
                    break;
                }
            }
            if (chunk.isEmpty()) {
                break;
            }
            List<CompletableFuture<Boolean>> threads = new ArrayList<>();
            // Mongo is ACID compliant, so there should not be any issue with this
            chunk.forEach(lastfmArtist -> threads.add(asyncTaskExecutor.submitCompletable(() -> musicRepositoryService.saveArtistInfoIfNotExist(lastfmArtist))));
            CompletableFuture.allOf(threads.toArray(new CompletableFuture[0])).join();
        }
    }

    protected LastfmArtistInfoResponse getArtistInfo(String name, String mbid) {
        var artistInfoRes = lastFmArtistApiAdapter.getArtistInfo(name, mbid).get().getBody();
        if (artistInfoRes == null || artistInfoRes.getArtist() == null) {
            return lastFmArtistApiAdapter.getArtistInfo(name, null).get().getBody();
        }
        return artistInfoRes;
    }


    private Stream<LastfmArtistInfoResponse> artistInfoStream(List<LastfmGetLibraryArtistsResponse. Artist> artists)  {
        List<CompletableFuture<LastfmArtistInfoResponse>> threads = new ArrayList<>();
        log.debug("SIZE: {}", artists.size());
        int limit = Math.min(artists.size(), THREAD_COUNT);

        IntStream.range(0, limit).forEach(thread -> threads.add(asyncTaskExecutor.submitCompletable(() ->
                getArtistInfo(artists.get(thread).getName(), artists.get(thread).getMbid().isBlank()
                        ? null : artists.get(thread).getMbid()))));

        CompletableFuture.allOf(threads.toArray(new CompletableFuture[0])).join();

        return threads.stream().flatMap(fetchedArtist -> {
            try {
                return Stream.ofNullable(fetchedArtist.get());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private Stream<LastfmGetLibraryArtistsResponse.Artist> artistStream(CompletableFuture<LastfmGetLibraryArtistsResponse> fetchedPageData) {
        try {
            return fetchedPageData.get().getArtists().getArtist().stream();
        } catch (InterruptedException | ExecutionException ex) {
            throw new RuntimeException(ex);
        }
    }
}
