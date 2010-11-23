package test

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