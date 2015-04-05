package com.datastax.homespotify.conf

import com.datastax.driver.core.Cluster

object CassandraConfig {
  lazy val cluster = Cluster.builder()
                      .addContactPoint("192.168.51.10")
                      .withClusterName("Spark-Cassandra")
                      .build()

  implicit lazy val session = cluster.connect("home_spotify")
}
