package kaladin.zwolf.projects.lastfm.graph.analyzer.service;

import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.LastfmArtistInfo;
import kaladin.zwolf.projects.lastfm.graph.analyzer.ports.out.ArtistRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ArtistRepositoryService {
    private ArtistRepository artistRepository;

    public ArtistRepositoryService(ArtistRepository artistRepository) {
        this.artistRepository = artistRepository;
    }

    public Optional<LastfmArtistInfo> findArtistByMbid(String mdid) {
        return artistRepository.findLastfmArtistInfoByArtist_Mbid(mdid);
    }

    public boolean saveArtistInfoIfNotExist(LastfmArtistInfo artistInfo) {
        var exists = findArtistByMbid(artistInfo.getArtist().getMbid());
        if (exists.isEmpty()) {
            artistRepository.save(artistInfo);
            return true;
        }
        return false;
    }
}
