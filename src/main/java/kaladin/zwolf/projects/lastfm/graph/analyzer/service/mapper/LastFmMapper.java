package kaladin.zwolf.projects.lastfm.graph.analyzer.service.mapper;

import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.entity.mongo.LastfmArtist;
import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.response.LastfmArtistInfoResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface LastFmMapper {
    LastFmMapper INSTANCE = Mappers.getMapper(LastFmMapper.class);

    @Mapping(source = "artistInfo.artist.mbid", target = "mbid")
    @Mapping(source = "artistInfo.artist.name", target = "name")
    @Mapping(source = "artistInfo.artist.stats", target = "stats")
    @Mapping(source = "artistInfo.artist.tags.tag", target = "tags")
    LastfmArtist fromArtistInfoToEntity(LastfmArtistInfoResponse artistInfo);
}
