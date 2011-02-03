package test

import cuke4duke.{EN, ScalaDsl}
import org.scalatest.matchers.ShouldMatchers

class CucumberWarSteps extends ScalaDsl with EN with ShouldMatchers {

  private var givenCalled = false
  private var whenCalled = false

  Given("""^an SBT project$""") {
    givenCalled = true
  }

  When("""^the cucumber task is called$""") {
    whenCalled = true
  }

  Then("""^Cucumber is executed against the features and step definitions$""") {
    givenCalled should be (true)
    whenCalled should be (true)
  }
}