name := "home_spotify_exo"

version := "1.0"

scalaVersion := "2.10.5"


libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "1.4.1" withSources() withJavadoc(),
  "org.apache.spark" %% "spark-sql" % "1.4.1" withSources() withJavadoc(),
  "org.apache.spark" %% "spark-streaming" % "1.4.1" withSources() withJavadoc(),
  "org.apache.spark" %% "spark-streaming-twitter" % "1.4.1" withSources() withJavadoc(),
  "com.datastax.spark" %% "spark-cassandra-connector" % "1.4.0" withSources() withJavadoc()
)

resolvers += Resolver.file("home-spotify-repo", file("project/.ivy/cache")) transactional()
