package kaladin.zwolf.projects.lastfm.graph.analyzer.service;

import kaladin.zwolf.projects.lastfm.graph.analyzer.adapters.out.LastFmArtistApiAdapter;
import kaladin.zwolf.projects.lastfm.graph.analyzer.adapters.out.LastFmTrackApiAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LastFmTrackApiService {
    private final Logger log = LoggerFactory.getLogger(LastFmTrackApiService.class);

    private final LastFmArtistApiAdapter lastFmArtistApiAdapter;
    private final LastFmTrackApiAdapter lastFmTrackApiAdapter;
    private final MusicRepositoryService musicRepositoryService;

    public LastFmTrackApiService(LastFmArtistApiAdapter lastFmArtistApiAdapter, LastFmTrackApiAdapter lastFmTrackApiAdapter, MusicRepositoryService musicRepositoryService) {
        this.lastFmArtistApiAdapter = lastFmArtistApiAdapter;
        this.lastFmTrackApiAdapter = lastFmTrackApiAdapter;
        this.musicRepositoryService = musicRepositoryService;
    }




}
