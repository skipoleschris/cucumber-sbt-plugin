import sbt._
import java.io.File

class CucumberSbtPluginProject(info: ProjectInfo) extends PluginProject(info) {

  override def managedStyle = ManagedStyle.Maven
  lazy val publishTo = Resolver.file("Templemore Maven Repo", new java.io.File("target/publish"))
}