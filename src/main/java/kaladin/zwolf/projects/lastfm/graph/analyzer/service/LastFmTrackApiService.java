package kaladin.zwolf.projects.lastfm.graph.analyzer.service;

import kaladin.zwolf.projects.lastfm.graph.analyzer.adapters.out.LastFmApiAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LastFmTrackApiService {
    private final Logger log = LoggerFactory.getLogger(LastFmTrackApiService.class);

    private final LastFmApiAdapter lastFmApiAdapter;
    private final MusicRepositoryService musicRepositoryService;

    public LastFmTrackApiService(LastFmApiAdapter lastFmApiAdapter, MusicRepositoryService musicRepositoryService) {
        this.lastFmApiAdapter = lastFmApiAdapter;
        this.musicRepositoryService = musicRepositoryService;
    }
}
