package kaladin.zwolf.projects.lastfm.graph.analyzer.domain.response.enums;

public enum Period {
    OVERALL("overall"),
    WEEK("7day"),
    MONTH("1month"),
    QUARTER("3month"),
    HALF_YEAR("6month"),
    YEAR("12month");

    private final String value;

    Period(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
