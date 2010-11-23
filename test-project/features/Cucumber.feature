Feature: Cucumber
  In order to implement BDD in my Scala project
  As a developer
  I want to be able to run Cucumber from with SBT

  Scenario: Execute feature with console output
    Given A SBT project
    When I run the cucumber goal
    Then Cucumber is executed against my features and step definitions
