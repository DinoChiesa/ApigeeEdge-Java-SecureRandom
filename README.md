# Java Secure Pseudo Random Number Generator

This directory contains the Java source code and pom.xml file required
to compile a simple Java callout for Apigee Edge. The callout uses
java.security.SecureRandom to generate random numbers (ints, UUIDs, or
Gaussian values) within a policy Apigee Edge proxy, and sets a context
variable with that information.

## Disclaimer

This example is not an official Google product, nor is it part of an official Google product.

## Using this callout

You do not need to compile the callout to use it.
It is usable as is, in pre-built form. Just grab [the jar from this repository](bundle/apiproxy/resources/java/edge-java-callout-prng-1.0.1.jar) and use it.

Configure the policy like this:

```xml
<JavaCallout name='Java-PRNG-gaussian'>
  <ClassName>com.google.apigee.edgecallouts.prng.SecureRandomCallout</ClassName>
  <Properties>
    <Property name='algorithm'>SHA1PRNG</Property>
    <Property name='output-type'>gaussian</Property>
  </Properties>
  <ResourceURL>java://edge-java-callout-prng-1.0.1.jar</ResourceURL>
</JavaCallout>
```

The properties:

| Property | Required? | description |
|:---------|:---------|:---------|
| algorithm | no |one of the algorithms returned by Java's [java.security.Security.getProviders()](https://docs.oracle.com/javase/7/docs/api/java/security/Security.html#getProviders()). Typically you will use one of these:<ul><li>SHA1PRNG</li><li>NativePRNG</li></ul> Defaults to SHA1PRNG |
| output-type | no | uuid, gaussian, or int. Defaults to int. A Gaussian output will return the next pseudorandom, Gaussian ("normally") distributed double value with mean 0.0 and standard deviation 1.0, as returned by [java.util.Random.nextGaussian()](https://docs.oracle.com/javase/7/docs/api/java/util/Random.html#nextGaussian()) |
| decimal-digits | no | The number of decimal digits with which to render the value generated for the Gaussian distribution. The default is 12.  This is ignored when the output-type is int. |

The policy caches the java.security.SecureRandom and re-uses it for multiple threads. This means it should perform well at high load and concurrency.


## Building:

You do not need to compile the callout to use it.  It is usable as is, in pre-built
form. Just grab [the jar from this
repository](bundle/apiproxy/resources/java/edge-java-callout-prng-1.0.1.jar) and use it.  But
if YOU DO wish to build it, here's how you can do so:

1. clone this repo
  ```
  git clone
  ```

2. configure the build on your machine by loading the Apigee jars into
   your local cache.  You need to do this once, ever, on the machine doing
   the compilation.

   ```
   ./buildsetup.sh
   ```

3. Build with maven.
   ```
   mvn clean package
   ```

4. if you edit proxy bundles offline, copy the resulting jar file, available in
   target/edge-java-callout-prng-1.0.1.jar to your apiproxy/resources/java directory.  If you
   don't edit proxy bundles offline, upload the jar file into the API Proxy via the Edge
   API Proxy Editor . Also upload the Guava jar from the target/lib directory.

5. include an XML file for the Java callout policy in your
   apiproxy/resources/policies directory. It should look as shown above.

6. use the Edge UI, or a command-line tool like [pushapi](https://github.com/carloseberhardt/apiploy) or [importAndDeploy](https://github.com/DinoChiesa/apigee-edge-js/blob/master/examples/importAndDeploy.js) or similar to
   import the proxy into an Edge organization, and then deploy the proxy .
   Eg,
   ```
   node importAndDeploy.js -v -o ORGNAME -e test -n prng -d bundle
   ```

7. Use a client to generate and send http requests to the example proxy. Eg,
   ```
   curl -i http://ORGNAME-test.apigee.net/prng/int
   ```

   or

   ```
   curl -i http://ORGNAME-test.apigee.net/prng/gaussian
   ```

   or

   ```
   curl -i http://ORGNAME-test.apigee.net/prng/uuid
   ```


## Dependencies

- Apigee Edge expressions v1.0
- Apigee Edge message-flow v1.0
- Google Guava 18.0

The first two jars must be available on the classpath for the compile to succeed. The
buildsetup.sh script will download these files for you automatically, and will insert
them into your maven cache.

If you want to download them manually:

These jars are produced by Apigee; contact Apigee support to obtain these jars to allow
the compile, or get them here:
https://github.com/apigee/api-platform-samples/tree/master/doc-samples/java-cookbook/lib

The Guava Jar must be uploaded with your proxy bundle.

## Notes

There is one callout class, com.google.apigee.edgecallouts.prng.SecureRandomCallout ,
which uses java.security.SecureRandom to generate random values.


## Support

This callout is open-source software, and is not a supported part of Apigee Edge.
If you need assistance, you can try inquiring on
[The Apigee Community Site](https://community.apigee.com).  There is no service-level
guarantee for responses to inquiries regarding this callout.


## LICENSE

This material is copyright 2015-2018 Google Inc.
and is licensed under the Apache 2.0 license. See the [LICENSE](LICENSE) file.
