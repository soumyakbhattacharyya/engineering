h1 gherkin summary 

given : a precondition, puts the system in known state 
when : describes the key action that user performs 
then : observable outcome for end user, that adds business value (for example, changes in database, is not a valid "then" as it is internal and does not add any value to end user he he comnes to know about that)
and / but : when given (or when or then) is required to be repeated multiple time, that can be chained using "and", while "but" is used to depict a negative condition in the flow (fro exmaple "Then I see list of products But I do not see number of available stock for them")

feature : defines a behavior of the product. feature can have a description, one or more tags, one or more scenarios and a background for the scenarios. background which is an optional element, should be short and vivid. it should not be used to set up complicated state, unless the state is something that client is required to know. as a thumb rule, ensure that a feature file does not contain, so many features that one has to scroll down.

example

```
Feature: Multiple site support
  As a Mephisto site owner
  I want to host blogs for different people
  In order to make gigantic piles of money

  Background:
    Given a global administrator named "Greg"
    And a blog named "Greg's anti-tax rants"
    And a customer named "Dr. Bill"
    And a blog named "Expensive Therapy" owned by "Dr. Bill"

  Scenario: Dr. Bill posts to his own blog
    Given I am logged in as Dr. Bill
    When I try to post to "Expensive Therapy"
    Then I should see "Your article was published."

  Scenario: Dr. Bill tries to post to somebody else's blog, and fails
    Given I am logged in as Dr. Bill
    When I try to post to "Greg's anti-tax rants"
    Then I should see "Hey! That's not your blog!"

  Scenario: Greg posts to a client's blog
    Given I am logged in as Greg
    When I try to post to "Expensive Therapy"
    Then I should see "Your article was published."

```

scenario  outline :  instead of repeating a scenario multiple times with different sets of data it is wise to define scenario outline. a scenartio outline is described using placeholders and folloeing that a table documents all varieties the scenario can have. the scenario is executed with all placeholders being replaced with actual values at runtime 

```
Scenario Outline: eating
  Given there are <start> cucumbers
  When I eat <eat> cucumbers
  Then I should have <left> cucumbers

  Examples:
    | start | eat | left |
    |  12   |  5  |  7   |
    |  20   |  5  |  15  |
```

implementation of a scenario is called as step definition. step definition documents in form of given / when / then / and / but
note that while scenario outlines a concept, step definition should focus on one domain concept (database entity may be). if step definitions are too coupled to scenarios they are unusable.
another important point is, conjunction and / but are permitted to be used while defining scenarios but is a strict no no while creating step definition 
so refrain from creating step definition like ``` Given I do this And I do that ```






