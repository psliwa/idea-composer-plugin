package org.psliwa.idea.composerJson.completion.completionContributor

class FilePathTest extends TestCase {
  def testSuggestions_givenEmptyPrefix_suggestPathOnTopLevel() = {
    myFixture.getTempDirFixture.findOrCreateDir("dir1").createChildDirectory(this, "dir1.1").createChildData(this, "file1.txt")
    myFixture.getTempDirFixture.findOrCreateDir("dir2").createChildDirectory(this, "dir2.1").createChildData(this, "file2.txt")
    myFixture.getTempDirFixture.createFile("file3.txt")

    suggestions(
      """
        |{
        | "bin": [ "<caret>" ]
        |}
      """.stripMargin,
      Array("dir1", "dir2", "file3.txt"),
      Array("dir1/dir1.1", ".", "..")
    )
  }

  def testSuggestions_givenDirectoryPrefix_suggestPathsFromGivenDirectory() = {
    myFixture.getTempDirFixture.findOrCreateDir("dir1").createChildDirectory(this, "dir1.1")
    myFixture.getTempDirFixture.findOrCreateDir("dir1").createChildData(this, "file1.txt")
    myFixture.getTempDirFixture.findOrCreateDir("dir2").createChildDirectory(this, "dir2.1")

    suggestions(
      """
        |{
        | "bin": [ "dir1/<caret>" ]
        |}
      """.stripMargin,
      Array("dir1.1", "file1.txt"),
      Array("dir2", "dir2/dir2.1", "dir2.1", "dir1/dir1.1")
    )
  }

  def testSuggestions_givenDirectoryPrefix_givenFileInDirectory_suggestFileFromGivenDirectory() = {
    myFixture.getTempDirFixture.findOrCreateDir("dir1").createChildDirectory(this, "dir1.1").createChildData(this, "file.txt")

    suggestions(
      """
        |{
        | "bin": [ "dir1/dir1.1/<caret>" ]
        |}
      """.stripMargin,
      Array("file.txt")
    )
  }

  def testSuggestionsInAutoloadProperty() = {
    myFixture.getTempDirFixture.findOrCreateDir("dir1")

    suggestions(
      """
        |{
        | "autoload": {
        |   "psr-0": {
        |     "Some\\Namespace": "<caret>"
        |   }
        | }
        |}
      """.stripMargin,
      Array("dir1")
    )
  }

  def testSuggestionsInAutoloadProperty_givenArrayOfFilePaths_completionShouldWorkAsWell() = {
    myFixture.getTempDirFixture.findOrCreateDir("dir1")

    suggestions(
      """
        |{
        | "autoload": {
        |   "psr-0": {
        |     "Some\\Namespace": [ "<caret>" ]
        |   }
        | }
        |}
      """.stripMargin,
      Array("dir1")
    )
  }

  def testSuggestionsInAutoload_filePathsShouldNotBeSuggestedAsAutoloadProperties() = {
    myFixture.getTempDirFixture.findOrCreateDir("dir1")

    suggestions(
      """
        |{
        | "autoload": {
        |   "psr-0": {
        |     "<caret>"
        |   }
        | }
        |}
      """.stripMargin,
      Array(),
      Array("dir1")
    )
  }

  def testAutoloadCompletion_completeAutoloadPropertyValuesAsObjects() = {
    completion(
      """
        |{
        | "autoload": {
        |   "psr0<caret>"
        | }
        |}
      """.stripMargin,
      """
        |{
        | "autoload": {
        |   "psr-0": {<caret>}
        | }
        |}
      """.stripMargin
    )
  }
}
