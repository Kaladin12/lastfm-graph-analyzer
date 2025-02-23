package kaladin.zwolf.projects.lastfm.graph.analyzer.service;

import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.entity.LastfmArtist;
import kaladin.zwolf.projects.lastfm.graph.analyzer.ports.out.MusicRepository;
import kaladin.zwolf.projects.lastfm.graph.analyzer.util.MappingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class MusicRepositoryService {
    private final Logger log = LoggerFactory.getLogger(MusicRepositoryService.class);

    private MusicRepository musicRepository;

    public MusicRepositoryService(MusicRepository musicRepository) {
        this.musicRepository = musicRepository;
    }

    public Optional<LastfmArtist> findArtistByMbid(String mdid) {
        return musicRepository.findLastfmArtistByMbid(mdid);
    }

    public Optional<LastfmArtist> findArtistByName(String name) {
        return musicRepository.findLastfmArtistByName(name);
    }

    public void saveArtistInfoIfNotExist(LastfmArtist artistInfo) {
        String mbid = artistInfo.getMbid();
        String name = artistInfo.getName();
        artistInfo.setMbid(MappingUtils.getMbid(mbid, name));
        mbid = artistInfo.getMbid();

        var exists = findArtistByMbid(artistInfo.getMbid());
        if (exists.isEmpty()) {
            setTags(artistInfo);
            musicRepository.save(artistInfo);
            log.debug("ARTIST {} SAVED", mbid);
            return;
        }
        log.warn("ARTIST {} ALREADY EXISTED IN DB", mbid);
    }

    public void saveArtistInfo(LastfmArtist artistInfo) {
        musicRepository.save(artistInfo);
    }

    private void setTags(LastfmArtist artistInfo) {
        var tags = artistInfo.getTags();
        artistInfo.setTags(tags.subList(0, Math.min(3, tags.size())));
    }
}
