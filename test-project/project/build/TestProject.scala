import sbt._
import templemore.sbt.CucumberProject

class TestProject(info: ProjectInfo) extends DefaultWebProject(info) with CucumberProject {

  // Test Dependencies
  val scalatest = "org.scalatest" % "scalatest" % "1.2" % "test"
}