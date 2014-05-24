concourse-test
=================

The concourse-test framework is designed to allow developers to write end-to-end integration tests that utilize both the server and client. For each test case, you just specify one or more versions to test against and the framework dynamically launches a temporary server and corresponding client connection that is automatically torn down at the end of the test. The framework manages all the boilerplate so that developers can test code against any version of Concourse with confidence.

## General Information

### Versioning

This is version 1.1.1 of the concourse-config-framework.

This project will be maintained under the [Semantic Versioning](http://semver.org)
guidelines such that release versions will be formatted as `<major>.<minor>.<patch>`
where

* breaking backward compatibility bumps the major,
* new additions while maintaining backward compatibility bumps the minor, and
* bug fixes or miscellaneous changes bumps the patch.

## Usage
### Single version tests
To run a test case against a single version of the Concourse server and client, extend the `ClientServerTest` base class. Doing so ensures that the test is equipped with a temporary server installation against which it can issue test actions using the `client` API. The temporary server is completely managed by the test framework but lives in a separate JVM process, which means that test cases run in a realistic environment.

#### Specify the version
Each test class must implement the `getServerVersion` method and return a string that describes the release version number against which the tests should run (formatted as a `<major>.<minor>.<patch>` version number) OR the path to a local server installer (i.e. a `.bin` file).

### Cross versions tests
Sometimes you'll want to run a single test case against multiple versions at the same time in order to compare performance and functionality. To do so, extend the `CrossVersionTest` base class. Doing so ensures that your tests are controlled by a custom runner that executes each test against all the specified versions. Just like when running a sngle version test, you have access to the `client` API which iteracts with the temporary servers for each version in separate JVMs.

#### Speicfy the versions
For a `CrossVersionTest` you will need to specify all of the release versions (formatted as a `<major>.<minor>.<patch>` version number) and/or paths to local installers using the `Versions` annotation.

### Setup and teardown
The framework automatically ensures that each test is equipped with a fully functional temporary server that is torn down at the end. But, each test class can also define additional setup and tear down methods in the `beforeEachTest` and `afterEachTest` methods.

### Debugging
The framework provides each test class with a global `Variables` register which will dump the state of the test case whenever it fails. To take advantage of this feature, simply register all of the interesting varaiables in your test implementation:
	
	@Test
	public void testAdd(){
		String key = Variables.register("key", "count");
		Object value = Variables.register("value", 1);
		long record = Variables.register("record", 1);
		...
	}

