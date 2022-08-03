# Java Secure Pseudo Random Number Generator

This directory contains the Java source code and pom.xml file required
to compile a simple Java callout for Apigee. The callout uses
java.security.SecureRandom to generate random numbers (ints, UUIDs, or
Gaussian values) within a policy Apigee proxy, and sets a context
variable with that information.

## Disclaimer

This example is not an official Google product, nor is it part of an official Google product.

## Using this callout

You do not need to compile the callout to use it.
It is usable as is, in pre-built form. Just grab [the jar from this repository](bundle/apiproxy/resources/java/edge-java-callout-prng-1.0.2.jar) and use it.

Configure the policy like this:

```xml
<JavaCallout name='Java-PRNG-gaussian'>
  <ClassName>com.google.apigee.edgecallouts.prng.SecureRandomCallout</ClassName>
  <Properties>
    <Property name='algorithm'>SHA1PRNG</Property>
    <Property name='output-type'>int</Property>
  </Properties>
  <ResourceURL>java://edge-java-callout-prng-1.0.2.jar</ResourceURL>
</JavaCallout>
```

The callout sets the context variable `prng_random` to a randomly-generated value.

The properties:

| Property | Required? | description |
|:---------|:---------|:---------|
| algorithm | no |one of the algorithms returned by Java's [java.security.Security.getProviders()](https://docs.oracle.com/javase/8/docs/api/java/security/Security.html#getProviders()). Typically you will use one of these:<ul><li>SHA1PRNG</li><li>NativePRNG</li></ul> Defaults to SHA1PRNG |
| output-type | no | `uuid`, `gaussian`, or `int`. Defaults to int. A Gaussian output will return the next pseudorandom, Gaussian ("normally") distributed double value with mean 0.0 and standard deviation 1.0, as returned by [java.util.Random.nextGaussian()](https://docs.oracle.com/javase/7/docs/api/java/util/Random.html#nextGaussian()) |
| decimal-digits | no | The number of decimal digits with which to render the value generated for the Gaussian distribution. The default is 12.  Used only when `output-type` is `gaussian`. |
| range | no | The min,max range for integer values, inclusive. Used only when  `output-type` is `int`. |

The policy caches the java.security.SecureRandom and re-uses it for multiple threads. This means it should perform well at high load and concurrency.


## Example output

Example successive values of `prng_random` for int:
* -1604942246
* 1436054319
* 55498893
* -1631105169
* 1950692777
* -1103022666
* -79584472
* 1752902214
* 1820421062
* -22240590
* -13385112

Example successive values of `prng_random` for gaussian:
* -1.026991678598
* 1.579345979370
* -1.585246137998
* 1.567258571580
* -0.914702253954
* 0.363466074537
* -0.453967291420


Example successive values of `prng_random` for uuid:
* 778b4e42-065c-4535-8e1f-1f9d23e6516c
* eb0c4a87-7100-4ce9-b425-0d99c4f1597f
* 8a256fa0-ac54-4103-a43f-2b6dd21cf2a1
* 2386a528-06bd-4043-9ac1-1f9d2aa8361e
* fd0b04bc-734a-4e82-a262-78eae85f897a
* 0f0534bd-f623-4fca-bff2-ba58145852c7

Presumably you will use this value in a subsequent policy. See [the example apiproxy](bundle/apiproxy/) for suggestions.

## Building:

You do not need to compile the callout to use it.  It is usable as is, in pre-built
form. Just grab [the jar from this
repository](bundle/apiproxy/resources/java/edge-java-callout-prng-1.0.2.jar) and use it.  But
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
   target/apigee-java-callout-prng-20220802.jar to your apiproxy/resources/java directory.  If you
   don't edit proxy bundles offline, upload the jar file into the API Proxy via the Apigee
   API Proxy Editor .

5. include an XML file for the Java callout policy in your
   apiproxy/resources/policies directory. It should look as shown above.

6. use the APigee UI, or a command-line tool like [apigeecli](https://github.com/apigee/apigeecli) or [importAndDeploy](https://github.com/DinoChiesa/apigee-edge-js-examples/blob/main/importAndDeploy.js) or similar to
   import the proxy into an Apigee organization, and then deploy the proxy .
   Eg,
   ```
   node importAndDeploy.js -v -o ORGNAME -e test -n prng -d bundle
   ```

7. Use a client to generate and send http requests to the example proxy. Eg,
   ```
   endpoint=https://whatever.api.com
   curl -i $endpoint/prng/int
   ```

   or

   ```
   curl -i $endpoint/prng/gaussian
   ```

   or

   ```
   curl -i $endpoint/prng/uuid
   ```


## Dependencies

- Apigee Edge expressions v1.0
- Apigee Edge message-flow v1.0

The first two jars must be available on the classpath for the compile to succeed. The
buildsetup.sh script will download these files for you automatically, and will insert
them into your maven cache.

If you want to download them manually:

These jars are produced by Apigee; contact Apigee support to obtain these jars to allow
the compile, or get them here:
https://github.com/apigee/api-platform-samples/tree/master/doc-samples/java-cookbook/lib

## Notes

There is one callout class, com.google.apigee.callouts.prng.SecureRandomCallout ,
which uses [java.security.SecureRandom](https://docs.oracle.com/javase/8/docs/api/java/security/SecureRandom.html) to generate random values.

## Support

This callout is open-source software, and is not a supported part of Apigee Edge.
If you need assistance, you can try inquiring on
[the Apigee Community Site](https://www.googlecloudcommunity.com/gc/Apigee/bd-p/cloud-apigee).  There is no service-level
guarantee for responses to inquiries regarding this callout.


## LICENSE

This material is Copyright © 2015-2022 Google LLC.
and is licensed under the Apache 2.0 license. See the [LICENSE](LICENSE) file.
