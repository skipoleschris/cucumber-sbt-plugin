package templemore.sbt

import _root_.sbt._

/**
 * @author Chris Turner
 */
trait CucumberProject extends BasicScalaProject {

  // Versions - override to support newer versions
  def jrubyVersion = "1.6.1"
  def cucumberVersion = "1.0.0"
  def cuke4DukeVersion = "0.4.4"
  def picoContainerVersion = "2.11.2"
  def prawnVersion = "0.8.4"

  def gemUrl = "http://rubygems.org/"

  // Individual task options - override to customise behaviour
  def extraCucumberOptions: List[String] = Nil
  def standardCucumberOptions = "--format" :: "pretty" :: "--no-source" :: "--no-snippets" :: Nil
  def devCucumberOptions = "--format" :: "pretty" :: Nil
  def htmlCucumberOptions = "--format" :: "html" :: "--out" :: htmlReportPath.absolutePath :: Nil
  def pdfCucumberOptions = "--format" :: "pdf" :: "--out" :: pdfReportPath.absolutePath :: Nil

  // Override report output locations
  def reportPath = outputPath / "cucumber-report"
  def htmlReportPath = reportPath / "cucumber.html"
  def pdfReportPath = reportPath / "cucumber.pdf"

  // Other configurations - override to customise location
  def featuresDirectory = info.projectPath / "features"

  // JVM Memory options for JRuby
  def jRubyMaxMemory = "256M"
  def jRubyPermGen = "64M"

  // Paths and directories
  private def scalaLibraryPath = Path.fromFile(buildScalaInstance.libraryJar)
  private def jRubyHome = info.projectPath / "lib_managed" / "cucumber_gems"
  private def gemPath = jRubyHome / "gems"

  // Cuke4Duke configuration
  private def cuke4DukeGems = List(Gem("cucumber", cucumberVersion, gemUrl),
                                   Gem("cuke4duke", cuke4DukeVersion, gemUrl),
                                   Gem("prawn", prawnVersion, gemUrl))
  private def cuke4DukeArgs = List("-Dcuke4duke.objectFactory=cuke4duke.internal.jvmclass.PicoFactory")
  private val cuke4DukeBin = gemPath / "bin" / "cuke4duke"

  private val jrubyComplete = "org.jruby" % "jruby-complete" % jrubyVersion % "test"
  private val cuke4DukeRepo = "Cuke4Duke Maven Repository" at "http://cukes.info/maven"
  private val cuke4Duke = "cuke4duke" % "cuke4duke" % cuke4DukeVersion % "test"
  private val picoContainer = "org.picocontainer" % "picocontainer" % picoContainerVersion % "test"

  // jRuby
  private val jRuby = new JRuby(fullClasspath(Configurations.Test),
                                scalaLibraryPath, cuke4DukeArgs,
                                jRubyHome, gemPath, jRubyMaxMemory, jRubyPermGen, log)

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
    gemPath.asFile.mkdirs
    def gemInstalled(gem: Gem) = {
      val gemDirectory = gemPath / "gems" / (gem.name + "-" + gem.version)
      gemDirectory.exists
    }
    cuke4DukeGems.filter(!gemInstalled(_)) match {
      case Nil => log.info("All gems already installed."); 0
      case x => x.map(gem => jRuby.installGem(gem.command)).reduceLeft(_ + _)
    }
  }

  // Override clean lib to take down the gems and .gem directories
  override def cleanLibAction = cleanGems dependsOn(super.cleanLibAction)

  private def cleanGems = task {
    log.info("Deleteing managed gems...")
    FileUtilities.clean(jRubyHome.get, log)
  }

  // Execute cucumber
  private def runCucumber(taskOptions: List[String],
                          tags: List[String],
                          names: List[String]) = {
    // Create report directory
    reportPath.asFile.mkdirs

    // Launch JRuby
    jRuby(List(cuke4DukeBin.absolutePath,
               featuresDirectory.absolutePath,
               "--require", testCompilePath.absolutePath,
               "--color") ++ taskOptions ++ extraCucumberOptions ++
               tags.flatMap(List("--tags", _)) ++
               names.flatMap(List("--name", _))) match {
      case 0 => None
      case code => Some("Cucumber execution failed! - Exit code: " + code)
    }
  }

  protected def cucumberAction(tags: List[String],
                               names: List[String],
                               before: (List[String], List[String]) => Unit,
                               after: (List[String], List[String]) => Unit) = task {
    before(tags, names)
    try { runCucumber(standardCucumberOptions, tags, names) }
    finally { after(tags, names) }
  } dependsOn(testCompile) describedAs ("Runs cucumber features with output on the console")

  protected def cucumberDevAction(tags: List[String],
                                  names: List[String],
                                  before: (List[String], List[String]) => Unit,
                                  after: (List[String], List[String]) => Unit) = task {
    before(tags, names)
    try { runCucumber(devCucumberOptions, tags, names) }
    finally { after(tags, names) }
  } dependsOn(testCompile) describedAs ("Runs cucumber features with developer report output on the console")

  protected def cucumberHtmlAction(tags: List[String],
                                   names: List[String],
                                   before: (List[String], List[String]) => Unit,
                                   after: (List[String], List[String]) => Unit) = task {
    before(tags, names)
    try { runCucumber(htmlCucumberOptions, tags, names) }
    finally { after(tags, names) }
  } dependsOn(testCompile) describedAs ("Runs cucumber features with html report in the target directory")

  protected def cucumberPdfAction(tags: List[String],
                                  names: List[String],
                                  before: (List[String], List[String]) => Unit,
                                  after: (List[String], List[String]) => Unit) = task {
    before(tags, names)
    try { runCucumber(pdfCucumberOptions, tags, names) }
    finally { after(tags, names) }
  } dependsOn(testCompile) describedAs ("Runs cucumber features with pdf report in the target directory")

  // Main tasks (no arguments supported)
  lazy val cucumber = cucumberAction(List(), List(), internalBeforeCucumber, internalAfterCucumber)
  lazy val cucumberDev = cucumberDevAction(List(), List(), internalBeforeCucumber, internalAfterCucumber)
  lazy val cucumberHtml = cucumberHtmlAction(List(), List(), internalBeforeCucumber, internalAfterCucumber)
  lazy val cucumberPdf = cucumberPdfAction(List(), List(), internalBeforeCucumber, internalAfterCucumber)

  // NOTE: The design of SBT prevents method tasks (tasks with arguments)
  //       from be called on the parent project in a multi-project
  //       configuration. To allow the running of all the cucumber goals from
  //       within the parent we therefore have to have a separate set of tasks
  //       that support parameters. These can only be called when a child project
  //       is selected, whereas the non-parameter versions can be called on the
  //       parent and child projects. The tasks supporting parameters are appended
  //       with a 'p'
  lazy val cuke = task { args => cucumberAction(tagsFromArgs(args), namesFromArgs(args), beforeCuke, afterCuke) }
  lazy val cukeDev = task { args => cucumberDevAction(tagsFromArgs(args), namesFromArgs(args), beforeCuke, afterCuke) }
  lazy val cukeHtml = task { args => cucumberHtmlAction(tagsFromArgs(args), namesFromArgs(args), beforeCuke, afterCuke) }
  lazy val cukePdf = task { args => cucumberPdfAction(tagsFromArgs(args), namesFromArgs(args), beforeCuke, afterCuke) }

  private def tagsFromArgs(args: Array[String]) =
    args.filter(arg => arg.startsWith("@") || arg.startsWith("~")).toList

  private def namesFromArgs(args: Array[String]) =
    args.filter(arg => !arg.startsWith("@") && !arg.startsWith("~")).toList

  // Functions that provide lifecycle hooks to allow before and after actions to be
  // tied to the running of cucumber features
  private def internalBeforeCucumber(tags: List[String], names: List[String]) = beforeCucumber
  private def internalAfterCucumber(tags: List[String], names: List[String]) = afterCucumber

  protected def beforeCucumber = beforeCucumberSuite
  protected def afterCucumber = afterCucumberSuite
  protected def beforeCuke(tags: List[String], names: List[String]) = beforeCucumberSuite
  protected def afterCuke(tags: List[String], names: List[String]) = afterCucumberSuite
  protected def beforeCucumberSuite = { }
  protected def afterCucumberSuite = { }
}
