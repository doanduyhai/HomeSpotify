name := "home_spotify_exo"

version := "1.0"

scalaVersion := "2.10.4"


libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "1.2.1" withSources() withJavadoc(),
  "org.apache.spark" %% "spark-sql" % "1.2.1" withSources() withJavadoc(),
  "org.apache.spark" %% "spark-streaming" % "1.2.1" withSources() withJavadoc(),
  "org.apache.spark" %% "spark-streaming-twitter" % "1.2.1" withSources() withJavadoc(),
  "com.datastax.spark" %% "spark-cassandra-connector" % "1.2.0-rc2" withSources() withJavadoc()
)

resolvers += Resolver.file("home-spotify-repo", file("project/.ivy/cache")) transactional()
