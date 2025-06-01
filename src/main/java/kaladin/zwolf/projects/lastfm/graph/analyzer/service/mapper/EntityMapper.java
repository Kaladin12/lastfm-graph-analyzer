package kaladin.zwolf.projects.lastfm.graph.analyzer.service.mapper;

import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.entity.mongo.LastfmArtist;
import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.entity.mongo.LastfmTrack;
import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.entity.neo4j.Album;
import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.entity.neo4j.Artist;
import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.entity.neo4j.Track;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface EntityMapper {
    EntityMapper INSTANCE = Mappers.getMapper(EntityMapper.class);

    @Mapping(target = "title", source = "name")
    @Mapping(target = "playCount", source = "playcount")
    Track mongoToNeo4jTrack(LastfmTrack lastfmTrack);

    @Mapping(target = "id", source = "mbid")
    Album mongoToNeo4jAlbum(LastfmTrack.Album album);

    Artist mongoToNeo4jArtist(LastfmArtist lastfmArtist);
}
