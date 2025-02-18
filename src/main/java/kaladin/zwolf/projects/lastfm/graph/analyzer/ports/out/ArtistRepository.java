package kaladin.zwolf.projects.lastfm.graph.analyzer.ports.out;

import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.LastfmArtistInfo;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ArtistRepository extends MongoRepository<LastfmArtistInfo, String> {

    Optional<LastfmArtistInfo> findLastfmArtistInfoByArtist_Mbid(String mdid);
}
