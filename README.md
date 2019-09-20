# Gradle React Native Plugin

## Setup

## Usage

## Contribute

### Run CircleCI locally

based on: https://circleci.com/docs/2.0/local-cli/

```bash
brew install circleci

# validate config
circleci config validate

# run specific job
circleci local execute --job [dependencies|debug|release|test|lint|deploy] -e VAR1=FOO

## run tests 1/4 - specifically second slice: 2
circleci local execute --job test -e TEST_START=2 -e TEST_TOTAL=4 

# cleanup
brew uninstall circleci
```

## References:

* https://www.klg71.de/post/kotlin_gradle_plugin/
* https://github.com/FRI-DAY/elasticmq-gradle-plugin