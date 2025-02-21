package kaladin.zwolf.projects.lastfm.graph.analyzer.ports.out;

import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.entity.LastfmArtist;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface MusicRepository extends MongoRepository<LastfmArtist, String> {

    Optional<LastfmArtist> findLastfmArtistByMbid(String mdid);

    Optional<LastfmArtist> findLastfmArtistByName(String artistName);
}
