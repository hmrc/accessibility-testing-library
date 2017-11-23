
# accessibility-testing-library

[ ![Download](https://api.bintray.com/packages/hmrc/releases/accessibility-testing-library/images/download.svg) ](https://bintray.com/hmrc/releases/accessibility-testing-library/_latestVersion)

This library can be used to integrate a number of automated tools into Selenium UI test repositories to help identify a number of potential accessibility issues in web front-ends.

The tools currently included are:
1. HTML Codesniffer [(Github link)](https://github.com/squizlabs/HTML_CodeSniffer), a JavaScript application that audits pages for standards violations, implemented in the library API as `AuditTester`.
2. The Nu Html Checker [(Github link)](https://validator.github.io/validator), a Java library for validating HTML documents, implemented in the library API as `HtmlValidationTester`.

The output of the library is integrated into the Cucumber report as HTML so is best viewed in a browser. A summary of the number of potential issues found can also given at the end of running a suite.

## Prerequisites

To use this project your project should include the following:
- Selenium _(currently 2.53.0)_
- Cucumber _(currently 1.2.2)_
- ~~A JS-enabled browser~~ Chrome (GUI or headless) _(currently only Chrome is known to be working with the Html Codesniffer integration, as Firefox is awaiting standardisation of the WebDriver logging API (see [WebDriver issue #406](https://github.com/w3c/webdriver/issues/406),[GeckoDriver issue #284](https://github.com/mozilla/geckodriver/issues/284)))_

## Using the library

1. Add the dependency in the project SBT file, replacing the version with the latest available:

```sbtshell
"uk.gov.hmrc" %% "accessibility-testing-library" % "x.y.z"
```

2. Create a companion object to initialise the parts of the library:
```scala
object AccessibilityHooks {
  var auditor: Option[AuditTester] = None
  var validator: Option[HtmlValidationTester] = None

  def propInit(key: String, fn: () => Unit) = {
    sys.props.get(key).foreach(x => {if (x == "true") fn()})
  }

  propInit("accessibility.audit", () => auditor = AuditTester.initialise(Driver.webDriver))
  propInit("accessibility.htmlvalidator", () => validator = HtmlValidationTester.initialise(Driver.webDriver))
}
```
> The `propInit` helper is included to allow you to enable and disable the library with command line arguments, which in the above would be `-Daccessibility.audit=true|false` and `-Daccessibility.htmlvalidator=true|false`

3. Create a class to provide integration with the Cucumber runner in your test suite:
```scala
class AccessibilityHooks {
  @Before
  def startScenarioAccessibility(scenario: Scenario) = {
    AccessibilityHooks.auditor.foreach(_.startScenario(scenario))
    AccessibilityHooks.validator.foreach(t => t.startScenario(scenario))
  }

  @After
  def endScenarioAccessibility() = {
    AccessibilityHooks.auditor.foreach(t => t.endScenario())
    AccessibilityHooks.validator.foreach(t => t.endScenario())
  }
}
```
> The `AccessibilityHooks` class **must** be located in a package specified in the `glue` option of your Cucumber runner class.

4. Trigger the testers from your step definition (this should ideally be in a step that is called once per page visited, such as a page heading check):
```scala
def someStepDef: Unit = {  
  ...
  AccessibilityHooks.auditor.foreach(t => t.checkContent(webDriver.getPageSource))
  AccessibilityHooks.validator.foreach(t => t.checkContent(webDriver.getPageSource))
  ...
}
```

5. In your Cucumber runner class, add a companion object to call each accessibility tester to print a summary at the end of the test suite execution:
```scala
object RunSuite {
  @org.junit.AfterClass
  def printAccessibilityResults(): Unit = {
    AccessibilityHooks.auditor.foreach(t => t.printTotalResults())
    AccessibilityHooks.validator.foreach(t => t.printTotalResults())
  }
}
```

6. Run your tests as usual with Chrome, ensuring you have provided the required command line arguments to enabled the accessibility testers, and review the detailed results in the Cucumber report as needed.

### Filtering results
By default, all possible issues found will be included in the Cucumber report. However, these may include problems outside of the control of teams (e.g., header or footer content from templates that must be used) or known false positives (e.g., intentional non-compliance to accommodate bugs in other software).

The library includes filters for a number of these, which teams are recommended to use as appropriate for their services. These can easily be extended as needed for a specific project.

#### AuditFilters
- `headerFooterFilter`: removes issues relating to known problems in the Gov.Uk template.
- `webChatFilter`: removes issues from integration with WebChat.
- `knownErrorsFilter`: removes issues known generally to be false positives for HMRC front end services.

#### HtmlValidationFilters
- `headerFooterFilter`: removes issues relating to known problems in the Gov.Uk template.
- `knownErrorsFilter`: removes issues known generally to be false positives for HMRC front end services.

#### Using filters
When calling the `checkContent` method of a tester, filters can be provided as a chain of `PartialFunction`'s that are combined with `orElse`, which match known results to ignore, that ends with a `emptyFilter` function that matches all other issues to retain them.

Unless otherwise needed, the configuration for most services is likely to be:
```scala
AccessibilityHooks.auditor.foreach(t => t.checkContent(driver.getPageSource, AuditFilters.headerFooterFilter orElse AuditFilters.knownErrorsFilter orElse AuditFilters.emptyFilter))
AccessibilityHooks.validator.foreach(t => t.checkContent(driver.getPageSource, HtmlValidationFilters.headerFooterFilter orElse HtmlValidationFilters.knownErrorsFilter orElse HtmlValidationFilters.emptyFilter))
```

#### Implementing a filter
A filter should be of the type `PartialFunction[AuditResult, Boolean]` (for audit issues) or `PartialFunction[HtmlValidationError, Boolean]` (for HTML validation issues).

Typically, the implementation will be a `case` statement that matches against the relevant part of the result class, for example:
```scala
def myFilter : PartialFunction[AuditResult, Boolean] = {
  case AuditResult(_,"WCAG2AA.Principle1.Guideline1_4.1_4_3.G18.BgImage",_,_,_,_) => false
}
```
This will match any `AuditResult` that relates to specific WCAG 2.0 violation (in this case technique G18 relating to contrast of text against background images) and return `false` which means it will not be retained and added to the Cucumber report.

The `emptyFilter` included at the end of the chain matches any remaining issues and returns `true`, retaining any results not removed by another filter.

## License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
