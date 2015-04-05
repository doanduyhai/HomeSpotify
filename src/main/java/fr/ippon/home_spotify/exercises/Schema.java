package fr.ippon.home_spotify.exercises;

public interface Schema {
    String KEYSPACE = "home_spotify";

    String PERFORMERS = "performers";
    String ALBUMS = "albums";
    String PERFORMERS_BY_STYLE = "performers_by_style";
    String PERFORMERS_DISTRIBUTION_BY_STYLE = "performers_distribution_by_style";
    String TOP_10_STYLES = "top10_styles";
    String ALBUMS_BY_DECADE_AND_COUNTRY = "albums_by_decade_and_country";
    String ALBUMS_BY_DECADE_AND_COUNTRY_SQL = "albums_by_decade_and_country_sql";
}
