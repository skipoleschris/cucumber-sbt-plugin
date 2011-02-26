cucumber-sbt-plugin
===================

An [sbt](http://simple-build-tool.googlecode.com/) plugin for running [Cucumber](http://cukes.info) features under [cuke4duke](http://github.com/aslakhellesoy/cuke4duke).

Based on the original [cuke4duke-sbt-plugin](https://github.com/rubbish/cuke4duke-sbt-plugin) by rubbish. This implementation upgrades to the latest sbt, cucumber and cuke4duke version and provides more default options. Specifically:

* Works with sbt 0.7.4 & 0.7.5.RC0
* Works with Cucumber 0.10.0
* Works with cuke4duke 0.4.3
* Allows projects compiled and running against Scala 2.8.0 and 2.8.1
* Provides three default actions: cucumber, cucumber-dev and cucumber-html

## Usage ##
Just run one of the cucumber actions to run all of the cucumber features. Features go in a 'features' directory at the root of the project. Step definitions go in 'src/test/scala'. The following actions are supported:

* cucumber - Runs the cucumber tool with pretty output to the console and source and snippets turned off
* cucumber-dev - Runs the cucumber tool with pretty output to the console and source and snippets turned on
* cucumber-html - Runs the cucumber tool and generates an output cucumber.html file in the target directory
* cucumber-pdf - Runs the cucumber tool and generates an output cucumber.pdf file in the target directory

There are also parameterised versions of each of these tasks (see IMPORTANT NOTE below):

* cuke
* cuke-dev
* cuke-html
* cuke-pdf

Each of these tasks accepts parameter arguments. E.g.:
    cuke @demo,~@in-progress
would run features tagged as @demo and not those tagged as @in-progress. Also:
    cuke "User admin"
would run features with a name matched to "User admin". Multiple arguments can be supplied and honour the following rules:

* arguments starting with @ or ~ will be passed to cucumber using the --tags flag
* arguments starting with anything else will be passed to cucumber using the --name flag

IMPORTANT NOTE: The current design of sbt prevents tasks with parameters (method tasks) being run against the parent project in a multi-module sbt project. This is why there are separate tasks with parameters. To use a parameter task you mush first select a child project.
The non-parameter tasks can be run against the parent project or a selected child.

## Writing Features ##
Features are written in text format and are placed in .feature files inside the 'features' directory. For more info on writing features please see the [Cucumber](http://cukes.info) website.
For example:
    Feature: Cucumber
      In order to implement BDD in my Scala project
      As a developer
      I want to be able to run Cucumber from with SBT

      Scenario: Execute feature with console output
        Given A SBT project
        When I run the cucumber goal
        Then Cucumber is executed against my features and step definitions


## Writing Step Defitions ##
Step definitions can be written in Scala, using the cuke4duke Scala DSL. More information on this api can be obtained from the the [cuke4duke wiki page for scala](http://wiki.github.com/aslakhellesoy/cuke4duke/scala).
For example:
    import cuke4duke.{EN, ScalaDsl}
    import org.scalatest.matchers.ShouldMatchers

    class CucumberSteps extends ScalaDsl with EN with ShouldMatchers {

      private var givenCalled = false
      private var whenCalled = false

      Given("""^A SBT project$""") {
        givenCalled = true
      }

      When("""^I run the cucumber goal$""") {
        whenCalled = true
      }

      Then("""^Cucumber is executed against my features and step definitions$""") {
        givenCalled should be (true)
        whenCalled should be (true)
      }
    }

## Project Setup ##
In the plugin definition file (project/plugins/Plugin.scala), add the cucumber-sbt-plugin dependency:
    import sbt._

    class Plugins(info: ProjectInfo) extends PluginDefinition(info) {
      val templemoreRepo = "templemore sbt repo" at "http://templemore.co.uk/repo"
      val cucumberPlugin = "templemore" % "cucumber-sbt-plugin" % "0.4.1"
    }

In your project file (i.e. project/build/TestProject.scala), mixin the CucumberProject trait:
    import sbt._
    import templemore.sbt.CucumberProject

    class TestProject(info: ProjectInfo) extends DefaultWebProject(info) with CucumberProject {

      // Test Dependencies
      val scalatest = "org.scalatest" % "scalatest" % "1.2" % "test"

      //...
    }

This trait will all the cuke4duke dependency into your project for use in writing the step definitions.

## Customisation ##
The plugin supports a number of customisations. The following overrides can be added to your project file to change the behaviour of the plugin:

* cucumberVersion - Allows overriding the version of Cucumber that will be used (default: 0.10.0)
* cuke4dukeVersion - Allows overriding the version of cuke4duke that will be used (default: 0.4.3)
* picoContainerVersion - Allows overriding the version of PicoContainer used by cuke4duke (default: 2.11.2)
* prawnVersion - Allows overriding the version of the prawn gem that will be used (default 0.8.4)
* featuresDirectory - The location cucumber looks in for feature files (default: info.projectPath / "features")
* reportPath - The directory that will be used for report generation (default: outputPath / "cucumber-report")
* htmlReportPath - The name of the file that the html report will be generated into (default: reportPath / "cucumber.html")
* pdfReportPath - The name of the file that the pdf report will be generated into (default: reportPath / "cucumber.pdf")
* extraCucumberOptions - Allows specifying of additional options to Cucumber, such as tags or names (default: Empty List)
* standardCucumberOptions - Allows overriding the custom options for the 'cucumber' goal (default: --format pretty --no-source --no-snippets)
* devCucumberOptions - Allows overriding the custom options for the 'cucumber-dev' goal (default: --format pretty)
* htmlCucumberOptions - Allows overriding the custom options for the 'cucumber-html' goal (default: --format html --out target/cucumber.html)
* pdfCucumberOptions - Allows overriding the custom options for the 'cucumber-pdf' goal (default: --format pdf --out target/cucumber.pdf)

## Before/After Hooks ##
The plugin supports a number of before and after hooks. These are provided to allow services to be started before cucumber test runs and to shut them down once the test run is complete. The following hooks are provided:

* beforeCucumberSuite and afterCucumberSuite - The default methods that are run. Override these to run custom hooks before/after both the cucumber and cuke tasks
* beforeCucumber and afterCucumber - Override these custom hooks that run before/after only the cucumber tasks (won't be run for cuke tasks)
* beforeCuke and afterCuke - Override these custom hooke that run before/after only the cuke tasks (won't be run for cucumber tasks). These have access to the tag and name parameters passed to the task.

## Roadmap ##


## Release History ##

### 0.5.0 ###
* Added lifecycle methods that are run before and after cucumber feature executions


### 0.4.1 ###
* Renamed all the parameterised cucumberp tasks to be cuke instead as this is more in keeping with the Cucumber project naming style

### 0.4.0 ###
* Tested with Scala 2.8.1 and SBT 0.7.5
* Fixed Issue #3 - It is now possible to configure location and names of output reports
* Fixed Issue #1 - Output reports now generate into target directories of each sub project rather than the parent project in a multi module project
* Fixed Issue #4 - Cucumber gems are now deleted when running the clean-lib task
* Fixed Issue #5 - Updated to Cucumber 0.10.0 and cuke4duke 0.4.3
* Fixed Issue #2 - Allowed cucumber tasks to be called from the parent project with separate parameterised tasks that can only be run on child projects

### 0.3.1 ###
* Bug fixes

### 0.3.0 ###
* First public release