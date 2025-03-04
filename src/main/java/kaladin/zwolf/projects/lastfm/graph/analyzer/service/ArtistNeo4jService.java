package kaladin.zwolf.projects.lastfm.graph.analyzer.service;

import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.entity.neo4j.Artist;
import kaladin.zwolf.projects.lastfm.graph.analyzer.ports.out.ArtistNeo4jRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ArtistNeo4jService {
    private Logger log = LoggerFactory.getLogger(ArtistNeo4jService.class);

    private ArtistNeo4jRepository artistNeo4jRepository;

    public ArtistNeo4jService(ArtistNeo4jRepository artistNeo4jRepository) {
        this.artistNeo4jRepository = artistNeo4jRepository;
    }

    public void saveArtist(Artist artist) {
        artistNeo4jRepository.save(artist);
    }
}
