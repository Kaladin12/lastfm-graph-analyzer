package kaladin.zwolf.projects.lastfm.graph.analyzer.service;

import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.entity.mongo.LastfmArtist;
import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.entity.mongo.LastfmTrack;
import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.entity.mongo.LastfmTrack.Album;
import kaladin.zwolf.projects.lastfm.graph.analyzer.ports.out.MusicRepository;
import kaladin.zwolf.projects.lastfm.graph.analyzer.util.MappingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

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

  public Stream<LastfmTrack> findAllTracksByArtist(String mbid) {
    Optional<LastfmArtist> result = musicRepository.findLastfmArtistByMbid(mbid);
    AtomicReference<Stream<LastfmTrack>> tracks = new AtomicReference<>(Stream.empty());
    result.ifPresent(artist -> {
      tracks.set(artist.getTracks().values().stream());
    });
    return tracks.get();
  }

  public List<Album> findAllAlbumsByArtist(String mbid) {
    Optional<LastfmArtist> result = musicRepository.findLastfmArtistByMbid(mbid);
    List<Album> albums = new ArrayList<>();
    result.ifPresent(artist -> {
      albums.addAll(artist.getTracks().values().stream()
              .filter(track -> track.getAlbum() != null)
              .map(this::setAlbumId)
              .map(LastfmTrack::getAlbum)
              .filter(Objects::nonNull).toList());
    });
    return albums;
  }

  public Stream<LastfmTrack> findAllTracksByAlbum(String artistMbid, String albumMbid) {
    Optional<LastfmArtist> result = musicRepository.findLastfmArtistByMbid(artistMbid);
    AtomicReference<Stream<LastfmTrack>> albumTracks = new AtomicReference<>(Stream.<LastfmTrack>builder().build());
    result.ifPresent(artist -> {
     albumTracks.set(artist.getTracks().values().stream()
             .filter(track ->  track.getAlbum()!=null)
             .map(this::setAlbumId)
             .filter(a -> a.getAlbum().getMbid().equals(albumMbid)));
    });
    return albumTracks.get();
  }

  public Page<LastfmArtist> findAllArtists(Pageable pageable) {
    return musicRepository.findAll(pageable);
  }

  private void setTags(LastfmArtist artistInfo) {
    var tags = artistInfo.getTags();
    artistInfo.setTags(tags.subList(0, Math.min(3, tags.size())));
  }

  private LastfmTrack setAlbumId(LastfmTrack track) {
    if (track.getAlbum().getMbid() == null) {
      track.getAlbum().setMbid(UUID.nameUUIDFromBytes(track.getAlbum().getName().getBytes()).toString());
    }
    return track;
  }
}
