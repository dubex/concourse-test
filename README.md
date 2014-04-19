concourse-test
=================

The concourse-test framework allows developers to write end-to-end integration tests that utilize both the server and client. Each test case written against the framework runs against a tempory server for any release version or locally built installer and is dynaically setup and torn down. The framework manages all the boilerplate so that developers can test code against any version of Concourse with confidence.

## General Information

### Versioning

This is version 1.0.0 of the concourse-config-framework.

This project will be maintained under the [Semantic Versioning](http://semver.org)
guidelines such that release versions will be formatted as `<major>.<minor>.<patch>`
where

* breaking backward compatibility bumps the major,
* new additions while maintaining backward compatibility bumps the minor, and
* bug fixes or miscellaneous changes bumps the patch.

## Usage
Each test class must extend the `ClientServerTest` base test to ensure that it is equipped with a temporary server installation against which it can issue test actions using the `client` API. The temporary server is completely managed by the test framework but lives in a separate JVM process, which means that test cases run in a realistic environment.

### Specify the server version
Each test class must implement the `getServerVersion` method and return a string that describes the release version number against which the tests should run. The returned version string should be formatted as a `<major>.<minor>.<patch>` version number.

### Specify a custom installer build
Each test class has the option of specifying a custom installer build to use in each test by supplying a `File` that points to the installer in the `installerPath` method.

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

