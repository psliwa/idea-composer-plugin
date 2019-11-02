package org.psliwa.idea.composerJson.benchmarks

import java.util.concurrent.TimeUnit

import org.openjdk.jmh.annotations.{Benchmark, BenchmarkMode, Fork, Measurement, Mode, OutputTimeUnit, Scope, State, Warmup}
import org.psliwa.idea.composerJson.composer.version.Version

@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode(Array(Mode.SingleShotTime))
@State(Scope.Thread)
@Fork(value = 1)
@Warmup(iterations = 100)
@Measurement(iterations = 100)
class VersionIsGreaterBenchmark {
  val versions: List[String] = scala.util.Random.shuffle(for {
    major <- 0 to 5
    minor <- 0 to 5
    patch <- 0 to 5
    version <- List(s"$major.$minor.$patch", s"$major.$minor.*", s"$major.*", s"v$major.$minor.$patch")
  } yield version).toList

  @Benchmark
  def baseline(): Unit = {
    versions.sortWith(_ > _)
  }

  @Benchmark
  def sortVersions(): Unit = {
    versions.sortWith((a, b) => Version.isGreater(a, b))
  }
}
