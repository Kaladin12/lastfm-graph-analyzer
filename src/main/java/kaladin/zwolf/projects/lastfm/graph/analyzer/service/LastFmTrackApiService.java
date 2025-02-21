package kaladin.zwolf.projects.lastfm.graph.analyzer.service;

import kaladin.zwolf.projects.lastfm.graph.analyzer.adapters.out.LastFmArtistApiAdapter;
import kaladin.zwolf.projects.lastfm.graph.analyzer.adapters.out.LastFmTrackApiAdapter;
import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.entity.LastfmTrack;
import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.response.LastfmTopTracksResponse;
import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.response.LastfmTrackInfoResponse;
import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.response.enums.Period;
import kaladin.zwolf.projects.lastfm.graph.analyzer.service.mapper.LastfmMapper;
import kaladin.zwolf.projects.lastfm.graph.analyzer.util.ChunkIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Service
public class LastFmTrackApiService {
    private final Logger log = LoggerFactory.getLogger(LastFmTrackApiService.class);

    private final LastFmArtistApiAdapter lastFmArtistApiAdapter;
    private final LastFmTrackApiAdapter lastFmTrackApiAdapter;
    private final MusicRepositoryService musicRepositoryService;

    @Value("${thread.count}")
    private int THREAD_COUNT;

    public LastFmTrackApiService(LastFmArtistApiAdapter lastFmArtistApiAdapter,
                                 LastFmTrackApiAdapter lastFmTrackApiAdapter,
                                 MusicRepositoryService musicRepositoryService) {
        this.lastFmArtistApiAdapter = lastFmArtistApiAdapter;
        this.lastFmTrackApiAdapter = lastFmTrackApiAdapter;
        this.musicRepositoryService = musicRepositoryService;
    }

    public void getTopTracks(String user, String period) {
        LastfmTopTracksResponse response = lastFmTrackApiAdapter.getTracks(user,
                        getValidPeriod(period), null)
                .getBody();
        // for each track, get artist and album info
        handleTracksPageResponse(response.getTopTracks());

        // get remaining pages
    }

    private Period getValidPeriod(String period) {
        try {
            return Period.valueOf(period);
        } catch (Exception e) {
            return Period.OVERALL;
        }
    }

    private void handleTracksPageResponse(LastfmTopTracksResponse.TopTracks tracks) {
        Stream<List<LastfmTopTracksResponse.Track>> stream = new ChunkIterator<>(tracks.getTracks().iterator(), THREAD_COUNT).stream();

        stream.flatMap(this::trackInfoStream)
                .forEach(e -> log.info("Track: {}", e));

    }

    private Stream<LastfmTrack> trackInfoStream(List<LastfmTopTracksResponse.Track> tracks) {
        List<CompletableFuture<LastfmTrackInfoResponse>> threads = new ArrayList<>();
        for (int threadId = 0; threadId < THREAD_COUNT && threadId < tracks.size(); threadId++) {
            threads.add(getTrackInfoAsync(tracks.get(threadId).getName(),
                    tracks.get(threadId).getMbid(), tracks.get(threadId).getArtist().getName()));
        }
        CompletableFuture.allOf(threads.toArray(new CompletableFuture[0])).join();

        AtomicInteger index = new AtomicInteger(0);

        return threads.stream().flatMap(fetchedTrackInfo -> mapTrackInfoWithRetry(fetchedTrackInfo, tracks, index));
    }

    @Async
    protected CompletableFuture<LastfmTrackInfoResponse> getTrackInfoAsync(String track, String mbid, String artist) {
        return CompletableFuture.completedFuture(lastFmTrackApiAdapter.getTrackInfo(track, mbid, artist).getBody());
    }

    private Stream<LastfmTrack> mapTrackInfoWithRetry(
            CompletableFuture<LastfmTrackInfoResponse> fetchedTrackInfo,
            List<LastfmTopTracksResponse.Track> tracks,
            AtomicInteger index) {
        try {
            LastfmTrackInfoResponse info = fetchedTrackInfo.get();
            int id = index.getAndIncrement();
            if (info.getTrack() == null && !tracks.get(id).getMbid().isBlank()) {
                info = lastFmTrackApiAdapter.getTrackInfo(
                        tracks.get(id).getName(), null, tracks.get(id).getArtist().getName()
                ).getBody();
            }
            if (info.getTrack() != null) {
                return Stream.of(LastfmMapper.fromTrackInfoToEntity(info, tracks.get(id)));
            }
            return Stream.empty();
        } catch (InterruptedException | ExecutionException ex) {
            throw new RuntimeException(ex);
        }
    }

}
