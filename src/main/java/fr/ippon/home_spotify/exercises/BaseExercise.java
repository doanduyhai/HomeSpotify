package fr.ippon.home_spotify.exercises;

import com.datastax.driver.core.Session;
import com.datastax.spark.connector.cql.CassandraConnector;
import com.datastax.spark.connector.cql.CassandraConnectorConf;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Unit;
import scala.runtime.AbstractFunction1;
import scala.tools.nsc.typechecker.Implicits;

import java.io.Serializable;

import static fr.ippon.home_spotify.exercises.Constants.*;
import static fr.ippon.home_spotify.exercises.Schema.*;

public class BaseExercise {

    protected static SparkConf buildScalaSparkConf(String exerciseName) {
        return new SparkConf()
                .setMaster(LOCAL_MODE)
                .setAppName(exerciseName)
                .set(CASSANDRA_HOST_NAME_PARAM, CASSANDRA_IP);
    }

    protected static JavaSparkContext buildSparkContext(String exerciseName) {
        SparkConf conf = buildScalaSparkConf(exerciseName);

        new CassandraConnector(CassandraConnectorConf.apply(conf)).withSessionDo(buildTruncateLambda(exerciseName));

        return new JavaSparkContext(conf);
    }

    protected static TruncateTables buildTruncateLambda(final String exerciseName) {
        return new TruncateTables() {

            @Override
            public Unit apply(Session session) {
                if (TABLES.containsKey(exerciseName)) {
                    session.execute("TRUNCATE " + KEYSPACE + "." + TABLES.get(exerciseName));
                }
                return null;
            }
        };
    }

    public static abstract class TruncateTables extends AbstractFunction1<Session, Unit> implements Serializable {

    }
}
