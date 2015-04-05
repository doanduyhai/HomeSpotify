package fr.ippon.home_spotify.exercises;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

import static fr.ippon.home_spotify.exercises.Schema.*;

public class Constants {

    public static final String CASSANDRA_HOST_NAME_PARAM = "spark.cassandra.connection.host";
    public static final String CASSANDRA_IP = "192.168.51.10";
    public static final String LOCAL_MODE = "local";

    public static final String EXERCISE_1 = "exercise1";
    public static final String EXERCISE_2 = "exercise2";
    public static final String EXERCISE_3 = "exercise3";
    public static final String EXERCISE_4 = "exercise4";
    public static final String EXERCISE_5 = "exercise5";
    public static final String EXERCISE_6 = "exercise6";

    public static final Map<String,String> TABLES = ImmutableMap
            .of(EXERCISE_1,PERFORMERS_BY_STYLE,
                EXERCISE_2,PERFORMERS_DISTRIBUTION_BY_STYLE,
                EXERCISE_3,TOP_10_STYLES,
                EXERCISE_4,ALBUMS_BY_DECADE_AND_COUNTRY,
                EXERCISE_5,ALBUMS_BY_DECADE_AND_COUNTRY_SQL);


}
