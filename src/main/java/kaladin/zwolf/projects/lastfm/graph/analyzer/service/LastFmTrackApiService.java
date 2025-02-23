package kaladin.zwolf.projects.lastfm.graph.analyzer.service;

import kaladin.zwolf.projects.lastfm.graph.analyzer.adapters.out.LastFmArtistApiAdapter;
import kaladin.zwolf.projects.lastfm.graph.analyzer.adapters.out.LastFmTrackApiAdapter;
import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.entity.LastfmArtist;
import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.entity.LastfmTrack;
import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.response.LastfmTopTracksResponse;
import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.response.LastfmTrackInfoResponse;
import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.response.enums.Period;
import kaladin.zwolf.projects.lastfm.graph.analyzer.service.mapper.LastfmMapper;
import kaladin.zwolf.projects.lastfm.graph.analyzer.util.ChunkIterator;
import kaladin.zwolf.projects.lastfm.graph.analyzer.util.MappingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
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

    @Async
    public void getTopTracks(String user, String period) {
        Period validPeriod = MappingUtils.getValidPeriod(period);
        LastfmTopTracksResponse response = lastFmTrackApiAdapter.getTracks(user, validPeriod, "26").getBody();

        handleTracksPageResponse(response.getTopTracks());

        int totalPages = 26; //Integer.parseInt(response.getArtists().getAttributes().getTotalPages());
        int page = Integer.parseInt(response.getTopTracks().getPageAttributes().getPage()) + 1;

        while (page <= totalPages) {
            List<CompletableFuture<LastfmTopTracksResponse>> topTracksPageThread = new ArrayList<>();

            for (int thread = 0; thread < THREAD_COUNT && page <= totalPages; thread++, page++) {
                topTracksPageThread.add(getTopTracksAsync(user, validPeriod, String.valueOf(page)));
            }

            CompletableFuture.allOf(topTracksPageThread.toArray(new CompletableFuture[0])).join();

            topTracksPageThread.stream().map(fetchedPage -> {
                        try {
                            return fetchedPage.get().getTopTracks();
                        } catch (InterruptedException | ExecutionException e) {
                            log.error("Cant get top tracks: {}", e.getMessage());
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .forEach(this::handleTracksPageResponse);
            log.info("{}/{} PAGES COMPLETED", page - 1, totalPages);
        }
    }

    @Async
    protected CompletableFuture<LastfmTopTracksResponse> getTopTracksAsync(String user, Period period, String page) {
        return CompletableFuture.completedFuture(lastFmTrackApiAdapter.getTracks(user, period, page).getBody());
    }

    private void handleTracksPageResponse(LastfmTopTracksResponse.TopTracks tracks) {
        tracks.getTracks().stream()
                .collect(Collectors.groupingBy(e -> e.getArtist().getName())) // avoids redundant calls to db :)
                .forEach((artist, trackList) -> musicRepositoryService.findArtistByName(artist).ifPresent(originalArtist -> {
                    if (originalArtist.getTracks() == null) {
                        originalArtist.setTracks(new HashMap<>());
                    }
                    int originalSize = originalArtist.getTracks().size();
                    Stream<List<LastfmTopTracksResponse.Track>> chunked = new ChunkIterator<>(trackList.iterator(), THREAD_COUNT).stream();
                    chunked.forEach(chunkedTracks -> trackInfoStream(chunkedTracks, originalArtist));
                    if (originalSize < originalArtist.getTracks().size()) {
                        int newSize = originalArtist.getTracks().size();
                        log.info("{}-{}-{}", artist, newSize-originalSize, newSize);
                        musicRepositoryService.saveArtistInfo(originalArtist);
                    } else {
                        log.info("Not updating {} as no changes were recorded", artist);
                    }
                }));
    }

    private void trackInfoStream(
            List<LastfmTopTracksResponse.Track> tracks,
            LastfmArtist originalArtist
    ) {
        List<CompletableFuture<LastfmTrackInfoResponse>> threads = new ArrayList<>();
        for (int threadId = 0; threadId < THREAD_COUNT && threadId < tracks.size(); threadId++) {
            threads.add(getTrackInfoAsync(tracks.get(threadId).getName(),
                    tracks.get(threadId).getMbid(), tracks.get(threadId).getArtist().getName()));
        }
        CompletableFuture.allOf(threads.toArray(new CompletableFuture[0])).join();

        AtomicInteger index = new AtomicInteger(0);

        threads.forEach(fetchedTrackInfo -> {
            int id = index.getAndIncrement();
             mapTrackInfoWithRetry(fetchedTrackInfo, tracks.get(id), originalArtist);
        });
    }

    @Async
    protected CompletableFuture<LastfmTrackInfoResponse> getTrackInfoAsync(String track, String mbid, String artist) {
        return CompletableFuture.completedFuture(lastFmTrackApiAdapter.getTrackInfo(track, mbid, artist).getBody());
    }

    private void mapTrackInfoWithRetry(
            CompletableFuture<LastfmTrackInfoResponse> fetchedTrackInfo,
            LastfmTopTracksResponse.Track track,
            LastfmArtist originalArtist) {
        try {
            LastfmTrackInfoResponse infoResponse = fetchedTrackInfo.get();
            if (infoResponse.getTrack() == null && !track.getName().isBlank()) {
                infoResponse = lastFmTrackApiAdapter
                        .getTrackInfo(track.getName(), null, track.getArtist().getName()).getBody();
            }
            if (infoResponse.getTrack() == null) {
                log.error("DROPPING {}", track.getName());
                return;
            }
            LastfmTrack mappedTrack = LastfmMapper.fromTrackInfoToEntity(infoResponse, track);
            // Yay! O(1)
            if (originalArtist.getTracks().containsKey(mappedTrack.getMbid())) {
                log.error("DROPPING {}", track.getName());
                return;
            }
            mappedTrack.setMbid(MappingUtils.getMbid(mappedTrack.getMbid(), mappedTrack.getName()));
            originalArtist.getTracks().put(mappedTrack.getMbid(), mappedTrack);
        } catch (Exception ex) {
            log.error("An exception occurred while trying to add {}. Exception: {}",track.getMbid(), ex.getMessage());
        }
    }
}
