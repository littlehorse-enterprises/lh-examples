# E2E Tests

This guide shows you how to implement E2E tests for LittleHorse.

## Running with Testcontainer

Run tests:

```shell
./gradlew clean test -Dbootstrapper.class=io.littlehorse.e2e.StandaloneBootstrapper
```

## Running with external LittleHorse

Run LittleHorse:

```shell
docker compose up -d
```

Run tests:

```shell
./gradlew clean test -Dbootstrapper.class=io.littlehorse.e2e.ExternalLittleHorseBootstrapper
```

## Test Structure

Add the LittleHorse dependencies to `app/build.gradle` file:

```groovy
def lhVersion = '0.12.2'

dependencies {
    ...
    implementation "io.littlehorse:littlehorse-client:${lhVersion}"
    testImplementation "io.littlehorse:littlehorse-test-utils:${lhVersion}"
    testImplementation "io.littlehorse:littlehorse-test-utils-container:${lhVersion}"
    ...
}
```

Create a test class:

```java
// Declare this is a E2E test
@LHTest
public class BasicTest {

    // Inject the workflow to test
    @LHWorkflow(EXAMPLE_BASIC_WF)
    private Workflow basicWf;
    private WorkflowVerifier verifier;

    // Declare the test
    @Test
    // Run the custom workers
    @WithWorkers("myWorker")
    public void shouldSayHello() {
        // In this test we run a workflow with the input variable: "Anakin Skywalker",
        // then it waits for the status COMPLETED, and validates the result output from the worker
        verifier.prepareRun(basicWf, Arg.of(INPUT_NAME_VARIABLE, "Anakin Skywalker"))
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyTaskRunResult(0, 1, variableValue -> assertEquals(variableValue.getStr(), "Hello there! Anakin Skywalker"))
                .start();
    }

    // Register a workflow
    @LHWorkflow(EXAMPLE_BASIC_WF)
    public Workflow registerWf() {
        return BasicExample.getWorkflow();
    }

    // Initialize worker
    public Object myWorker() {
        return new MyWorker();
    }
}
```

## Configurations

### Using Environment Variables

You can define which test bootstrapper to use passing env variables.
This is useful when running the test inside a docker container or
a pipeline. Example:

```shell
BOOTSTRAPPER_CLASS="io.littlehorse.e2e.ExternalLittleHorseBootstrapper" ./gradlew clean test
```

### Using System Properties

Add a `bootstrapper.class` system property to `build.gradle` file:

```groovy
def bootstrapperClassProperty = 'bootstrapper.class'

test {
    useJUnitPlatform()
    systemProperty bootstrapperClassProperty, System.getProperty(bootstrapperClassProperty) ?: 'io.littlehorse.e2e.StandaloneBootstrapper'
}
```

Then you can pass which bootstrapper to use. Example:

```shell
./gradlew clean test -Dbootstrapper.class=io.littlehorse.e2e.ExternalLittleHorseBootstrapper
```

> Notice the `-D`.

### Using a Properties File

LittleHorse test utils allows you set the **default** `bootstrapper.class`
using a property file. This is specially useful when running
the test from an IDE.

Create a `test/resources/test.properties` file with:

```properties
bootstrapper.class = io.littlehorse.e2e.StandaloneBootstrapper
```
