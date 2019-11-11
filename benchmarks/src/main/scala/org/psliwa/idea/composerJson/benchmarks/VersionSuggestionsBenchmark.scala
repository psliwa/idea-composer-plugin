package org.psliwa.idea.composerJson.benchmarks

import java.util.concurrent.TimeUnit

import org.openjdk.jmh.annotations._
import org.psliwa.idea.composerJson.composer.version.VersionSuggestions

@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode(Array(Mode.SingleShotTime))
@State(Scope.Thread)
@Fork(value = 1)
@Warmup(iterations = 500)
@Measurement(iterations = 300)
class VersionSuggestionsBenchmark {
  val versions: List[String] = scala.util.Random.shuffle(for {
    major <- 0 to 6
    minor <- 0 to 6
    patch <- 0 to 6
    version <- List(s"$major.$minor.$patch", s"v$major.$minor.$patch", s"$major.$minor.${patch}_2")
  } yield version).toList

  @Benchmark
  def versionSuggestions(): Unit = {
    VersionSuggestions.suggestionsForPrefix(versions, "")
  }
}
