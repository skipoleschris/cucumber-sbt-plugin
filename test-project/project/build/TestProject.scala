import sbt._
import templemore.sbt.CucumberProject

class TestProject(info: ProjectInfo) extends ParentProject(info) {

  // Projects
  lazy val jarProject = project("jar-project", "jar-project", new JarProject(_))
  lazy val warProject = project("war-project", "war-project", new WarProject(_), jarProject)

  // Jar Project
  class JarProject(info: ProjectInfo) extends DefaultProject(info) with CucumberProject {
    // Test Dependencies
    val scalatest = "org.scalatest" % "scalatest" % "1.2" % "test"

    override protected def beforeCucumberSuite = println("Will be run before cucumber suite")
    override protected def afterCucumberSuite = println("Will be run after cucumber suite")
  }

  // War project
  class WarProject(info: ProjectInfo) extends DefaultWebProject(info) with CucumberProject
}
