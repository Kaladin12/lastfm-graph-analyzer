package kaladin.zwolf.projects.lastfm.graph.analyzer.service;

import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.entity.mongo.LastfmTrack;
import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.entity.neo4j.Album;
import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.entity.neo4j.Artist;
import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.entity.neo4j.Track;
import kaladin.zwolf.projects.lastfm.graph.analyzer.service.mapper.EntityMapper;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LoaderService {
    private final Logger log = org.slf4j.LoggerFactory.getLogger(LoaderService.class);

    private final MusicRepositoryService musicRepositoryService;
    private final TrackNeo4jService trackNeo4jService;
    private final AlbumNeo4jService albumNeo4jService;
    private final ArtistNeo4jService artistNeo4jService;

    public LoaderService(MusicRepositoryService musicRepositoryService, TrackNeo4jService trackNeo4jService,
                         AlbumNeo4jService albumNeo4jService, ArtistNeo4jService artistNeo4jService) {
        this.musicRepositoryService = musicRepositoryService;
        this.trackNeo4jService = trackNeo4jService;
        this.albumNeo4jService = albumNeo4jService;
        this.artistNeo4jService = artistNeo4jService;
    }

    public void findAndLoadTrack(String id) {
        musicRepositoryService.findAllTracksByArtist(id)
            .forEach(this::loadTrack);
    }

    public void findAndLoadAlbum(String id) {
        musicRepositoryService.findAllAlbumsByArtist(id)
            .forEach(album -> {
                loadAlbum(album, id);
            });
    }

    public void findAndLoadArtist(String id) {
        var artist = musicRepositoryService.findArtistByMbid(id);
        if (artist.isPresent()) {
            Artist mappedArtist = EntityMapper.fromMongoToNeo(artist.get());
            musicRepositoryService.findAllAlbumsByArtist(id)
                .forEach(album -> mappedArtist.hasReleased(loadAlbum(album, id)));
            artistNeo4jService.saveArtist(mappedArtist);
        }
    }

    public Track loadTrack(LastfmTrack track) {
        log.info("TRACK: {}", track.getName());
        Track mappedTrack = EntityMapper.fromMongoToNeo(track);
        log.info("Mapped track: {}", mappedTrack);
        trackNeo4jService.saveTrack(mappedTrack);
        return mappedTrack;
    }

    public Album loadAlbum(LastfmTrack.Album album, String artistId) {
        log.info("ALBUM: {}", album.getName());
        Album mappedAlbum = EntityMapper.fromMongoToNeo(album);
        log.info("Mapped album: {}", mappedAlbum);
        musicRepositoryService.findAllTracksByAlbum(artistId, album.getMbid())
                .forEach(track -> mappedAlbum.hasTrack(loadTrack(track)));
        albumNeo4jService.saveAlbum(mappedAlbum);
        return mappedAlbum;
    }
}
