cucumber-sbt-plugin
===================

An [sbt](http://simple-build-tool.googlecode.com/) plugin for running [Cucumber](http://cukes.info) features under [cuke4duke](http://github.com/aslakhellesoy/cuke4duke).

Based on the original [cuke4duke-sbt-plugin](https://github.com/rubbish/cuke4duke-sbt-plugin) by rubbish. This implementation upgrades to the latest sbt, cucumber and cuke4duke version and provides more default options. Specifically:

* Works with sbt 0.7.4
* Works with Cucumber 0.9.4
* Works with cuke4duke 0.4.2
* Allows projects comipled and running against Scala 2.8.0
* Provides three default actions: cucumber, cucumber-dev and cucumber-html

## Usage ##
Just run one of the cucumber actions to run all of the cucumber features. Features go in a 'features' directory at the root of the project. Step definitions go in 'src/test/scala'. The following actions are supported:

* cucumber - Runs the cucumber tool with pretty output to the console and source and snippets turned off
* cucumber-dev - Runs the cucumber tool with pretty output to the console and source and snippets turned on
* cucumber-html - Runs the cucumber tool and generates an output cucumber.html file in the target directory

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
      val cucumberPlugin = "templemore" % "cucumber-sbt-plugin" % "0.1"
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

* cucumberVersion - Allows overriding the version of Cucumber that will be used (default: 0.9.4)
* cuke4dukeVersion - Allows overriding the version of cuke4duke that will be used (default: 0.4.2)
* picoContainerVersion - Allows overriding the version of PicoContainer used by cuke4duke (default: 2.11.2)
* extraCucumberOptions - Allows specifying of additional options to Cucumber, such as tags or names (default: Empty List)
* standardCucumberOptions - Allows overriding the custom options for the 'cucumber' goal (default: --format pretty --no-source --no-snippets)
* devCucumberOptions - Allows overriding the custom options for the 'cucumber-dev' goal (default: --format pretty)
* htmlCucumberOptions - Allows overriding the custom options for the 'cucumber-html' goal (default: --format html --out target/cucumber.html)

## Roadmap ##
Current plans:

* Upgrade to support sbt 0.7.5 and Scala 2.8.1

