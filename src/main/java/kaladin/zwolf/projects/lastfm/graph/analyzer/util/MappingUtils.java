package kaladin.zwolf.projects.lastfm.graph.analyzer.util;

import kaladin.zwolf.projects.lastfm.graph.analyzer.domain.response.enums.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class MappingUtils {
    private static final Logger log = LoggerFactory.getLogger(MappingUtils.class);
    private MappingUtils () {}

    public static Period getValidPeriod(String lookup) {
        try {
            return Period.valueOf(lookup);
        } catch (Exception e) {
            // Let's default to overall for now
            return Period.OVERALL;
        }
    }

    public static String getMbid(String mbid, String name) {
        if (mbid == null) {
            mbid = UUID.nameUUIDFromBytes(name.getBytes()).toString();
            log.info("Artist {} had null Mbid, setting it to {}", name, mbid);
        }
        return mbid;
    }
}
