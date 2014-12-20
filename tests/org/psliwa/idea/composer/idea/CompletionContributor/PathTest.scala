package org.psliwa.idea.composer.idea.completionContributor

class PathTest extends TestCase {
  def testSuggestions_givenEmptyPrefix_suggestPathOnTopLevel() = {
    myFixture.getTempDirFixture.findOrCreateDir("dir1").createChildDirectory(this, "dir1.1").createChildData(this, "file1.txt")
    myFixture.getTempDirFixture.findOrCreateDir("dir2").createChildDirectory(this, "dir2.1").createChildData(this, "file2.txt")

    suggestions(
      """
        |{
        | "bin": [ "<caret>" ]
        |}
      """.stripMargin,
      Array("dir1", "dir2"),
      Array("dir1/dir1.1", ".")
    )
  }
}
