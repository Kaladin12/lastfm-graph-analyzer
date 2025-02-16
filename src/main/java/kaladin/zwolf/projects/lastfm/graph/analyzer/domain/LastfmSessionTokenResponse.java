package kaladin.zwolf.projects.lastfm.graph.analyzer.domain;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

@Data
@JacksonXmlRootElement(localName = "lfm")
public class LastfmSessionTokenResponse {
    @JacksonXmlProperty(localName = "session")
    private Session session;

    @Data
    @JacksonXmlRootElement(localName = "session")
    public static class Session {
        @JacksonXmlProperty(localName = "name")
        private String name;

        @JacksonXmlProperty(localName = "key")
        private String key;

        @JacksonXmlProperty(localName = "subscriber")
        private int subscriber;

    }
}
