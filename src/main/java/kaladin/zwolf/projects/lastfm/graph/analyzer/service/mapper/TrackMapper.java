package kaladin.zwolf.projects.lastfm.graph.analyzer.service.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.entity.mongo.LastfmTrack;
import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.entity.neo4j.Track;

public class TrackMapper {
  private final Logger log = LoggerFactory.getLogger(TrackMapper.class);

  private TrackMapper() {
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

}
