package kaladin.zwolf.projects.lastfm.graph.analyzer.service;

import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.entity.neo4j.Album;
import kaladin.zwolf.projects.lastfm.graph.analyzer.ports.out.AlbumNeo4jRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AlbumNeo4jService {
    private final Logger log = LoggerFactory.getLogger(AlbumNeo4jService.class);

    private AlbumNeo4jRepository albumNeo4jRepository;

    public AlbumNeo4jService(AlbumNeo4jRepository albumNeo4jRepository) {
        this.albumNeo4jRepository = albumNeo4jRepository;
    }

    public void saveAlbum(Album album) {
        albumNeo4jRepository.save(album);
    }

    public Optional<Album> findAlbumById(String id) {
        return albumNeo4jRepository.findAlbumById(id);
    }

}
