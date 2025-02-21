package kaladin.zwolf.projects.lastfm.graph.analyzer.service.mapper;

import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.entity.LastfmArtist;
import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.response.LastfmArtistInfoResponse;

public class LastfmArtistMapper {

    private LastfmArtistMapper() {}

    public static LastfmArtist fromArtistInfoToEntity(LastfmArtistInfoResponse artistInfo) {
        return LastfmArtist.builder()
                .mbid(artistInfo.getArtist().getMbid())
                .name(artistInfo.getArtist().getName())
                .stats(artistInfo.getArtist().getStats())
                .tags(artistInfo.getArtist().getTags().getTag())
                .build();
    }

}
