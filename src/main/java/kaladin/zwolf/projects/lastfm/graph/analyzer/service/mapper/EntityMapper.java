package kaladin.zwolf.projects.lastfm.graph.analyzer.service.mapper;

import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.entity.mongo.LastfmArtist;
import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.entity.neo4j.Album;
import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.entity.neo4j.Artist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.entity.mongo.LastfmTrack;
import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.entity.neo4j.Track;

public class EntityMapper {
  private final Logger log = LoggerFactory.getLogger(EntityMapper.class);

  private EntityMapper() {
  }

  public static Track fromMongoToNeo(LastfmTrack original) {
    Track neoTrack = new Track();
    neoTrack.setMbid(original.getMbid());
    neoTrack.setDuration(original.getDuration());
    neoTrack.setTitle(original.getName());
    neoTrack.setPlayCount(original.getPlaycount());
    neoTrack.setRank(original.getRank());

    return neoTrack;
  }

  public static Album fromMongoToNeo(LastfmTrack.Album original) {
    Album neoAlbum = new Album();
    neoAlbum.setId(original.getMbid());
    neoAlbum.setName(original.getName());
    return neoAlbum;
  }

  public static Artist fromMongoToNeo(LastfmArtist original) {
    Artist neoArtist = new Artist();
    neoArtist.setName(original.getName());
    neoArtist.setMbid(original.getMbid());
    return neoArtist;
  }

}
