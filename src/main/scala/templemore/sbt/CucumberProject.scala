package templemore.sbt

import _root_.sbt._

/**
 * @author Chris Turner
 */
trait CucumberProject extends BasicScalaProject {

  // Versions - override to support newer versions
  def cucumberVersion = "0.9.4"
  def cuke4DukeVersion = "0.4.2"
  def picoContainerVersion = "2.11.2"

  // Individual task options - override to customise behaviour
  def extraCucumberOptions: List[String] = Nil
  def standardCucumberOptions = "--format" :: "pretty" :: "--no-source" :: "--no-snippets" :: Nil
  def devCucumberOptions = "--format" :: "pretty" :: Nil
  def htmlCucumberOptions = "--format" :: "html" :: "--out" :: "target/cucumber.html" :: Nil

  // Other configurations - override to customise location
  def featuresDirectory = info.projectPath / "features"

  // Paths and directories
  private def scalaLibraryPath = Path.fromFile(buildScalaInstance.libraryJar)
  private def jRubyHome = info.projectPath / "lib_managed" / "cucumber_gems"
  private def gemPath = jRubyHome / "gems"

  // Cuke4Duke configuration
  private def cuke4DukeGems = List(Gem("cucumber", cucumberVersion, "http://rubygems.org/"),
                                   Gem("cuke4duke", cuke4DukeVersion, "http://rubygems.org/"))
  private def cuke4DukeArgs = List("-Dcuke4duke.objectFactory=cuke4duke.internal.jvmclass.PicoFactory")
  private val cuke4DukeBin = gemPath / "bin" / "cuke4duke"

  private val cuke4DukeRepo = "Cuke4Duke Maven Repository" at "http://cukes.info/maven"
  private val cuke4Duke = "cuke4duke" % "cuke4duke" % cuke4DukeVersion % "test"
  private val picoContainer = "org.picocontainer" % "picocontainer" % picoContainerVersion % "test"

  // jRuby
  private val jRuby = new JRuby(fullClasspath(Configurations.Test),
                                scalaLibraryPath, cuke4DukeArgs,
                                jRubyHome, gemPath, log)

  // Automated Cucumber and Cuke4Duke Gem Installation
  override def updateAction = updateGems dependsOn(updateNoGemInstall)

  private lazy val updateGems = task {
    installGems match {
      case 0 => None
      case _ => Some("Installation of required gems failed!")
    }
  }
  private lazy val updateNoGemInstall = super.updateAction

  private def installGems = {
    log.info("Installing required gems for Cucumber and Cuke4Duke...")
    def gemInstalled(gem: Gem) = {
      val gemDirectory = gemPath / "gems" / (gem.name + "-" + gem.version)
      gemDirectory.exists
    }
    cuke4DukeGems.filter(!gemInstalled(_)) match {
      case Nil => log.info("All gems already installed."); 0
      case x => x.map(gem => jRuby.installGem(gem.command)).reduceLeft(_ + _)
    }
  }

  // Execute cucumber
  private def runCucumber(taskOptions: List[String]) = {
    jRuby(List(cuke4DukeBin.absolutePath,
               featuresDirectory.absolutePath,
               "--require", testCompilePath.absolutePath,
               "--color") ++ taskOptions ++ extraCucumberOptions) match {
      case 0 => None
      case code => Some("Cucumber execution failed! - Exit code: " + code)
    }
  }

  lazy val cucumber = cucumberAction dependsOn(testCompile) describedAs "Runs cucumber features with clean report output on the console"
  private def cucumberAction = task {
    runCucumber(standardCucumberOptions)
  }

  lazy val cucumberDev = cucumberDevAction dependsOn(testCompile) describedAs "Runs cucumber features with developer report output on the console"
  private def cucumberDevAction = task {
    runCucumber(devCucumberOptions)
  }

  lazy val cucumberHtml = cucumberHtmlAction dependsOn(testCompile) describedAs "Runs cucumber features with html report in the target directory"
  private def cucumberHtmlAction = task {
    runCucumber(htmlCucumberOptions)
  }
}
