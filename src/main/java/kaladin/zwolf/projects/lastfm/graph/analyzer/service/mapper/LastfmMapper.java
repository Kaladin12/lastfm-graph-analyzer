package kaladin.zwolf.projects.lastfm.graph.analyzer.service.mapper;

import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.entity.mongo.LastfmArtist;
import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.entity.mongo.LastfmTrack;
import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.response.LastfmArtistInfoResponse;
import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.response.LastfmTopTracksResponse;
import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.response.LastfmTrackInfoResponse;

import java.util.Optional;
import java.util.UUID;

public class LastfmMapper {

    private LastfmMapper() {}

    public static LastfmArtist fromArtistInfoToEntity(LastfmArtistInfoResponse artistInfo) {
        return LastfmArtist.builder()
                .mbid(artistInfo.getArtist().getMbid())
                .name(artistInfo.getArtist().getName())
                .stats(artistInfo.getArtist().getStats())
                .tags(artistInfo.getArtist().getTags().getTag())
                .build();
    }

    public static LastfmTrack fromTrackInfoToEntity(LastfmTrackInfoResponse trackInfo, LastfmTopTracksResponse.Track track){
        var tags = trackInfo.getTrack().getTags().getTag();
        var tagsSublist = tags.subList(0, Math.min(3, tags.size()));

        String mbid = Optional.ofNullable(trackInfo.getTrack().getMbid()).orElse(UUID.nameUUIDFromBytes(track.getName().getBytes()).toString());

        var builder = LastfmTrack.builder()
                .mbid(mbid)
                .name(track.getName())
                .rank(Integer.parseInt(track.getAttributes().getRank()))
                .duration(Integer.parseInt(track.getDuration()))
                .playcount(Integer.parseInt(track.getPlaycount()))
                .tags(tagsSublist);

        Optional.ofNullable(trackInfo.getTrack().getAlbum()).ifPresent(album ->
                builder.album(LastfmTrack.Album.builder()
                        .mbid(album.getMbid() )
                        .name(album.getTitle())
                        .build())
        );
        if (trackInfo.getTrack().getMbid() == null) {
            builder.generatedMbid(true);
        }
        return builder.build();
    }

}
