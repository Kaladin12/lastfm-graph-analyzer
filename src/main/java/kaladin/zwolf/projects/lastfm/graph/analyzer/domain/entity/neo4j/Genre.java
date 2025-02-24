package kaladin.zwolf.projects.lastfm.graph.analyzer.domain.entity.neo4j;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Data
@Node
public class Genre {
    @Id
    private String name;
}
