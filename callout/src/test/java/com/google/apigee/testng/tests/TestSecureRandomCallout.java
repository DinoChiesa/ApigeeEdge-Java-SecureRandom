package com.google.apigee.testng.tests;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;

import mockit.Mock;
import mockit.MockUp;

import com.apigee.flow.execution.ExecutionContext;
import com.apigee.flow.execution.ExecutionResult;
import com.apigee.flow.message.MessageContext;
import com.apigee.flow.message.Message;

import com.google.apigee.edgecallouts.prng.SecureRandomCallout;

public class TestSecureRandomCallout {
    private final static String testDataDir = "src/test/resources/test-data";

    MessageContext msgCtxt;
    String messageContent;
    Message message;
    ExecutionContext exeCtxt;

    @BeforeMethod()
    public void testSetup1() {

        msgCtxt = new MockUp<MessageContext>() {
            private Map variables;
            public void $init() {
                variables = new HashMap();
            }

            @Mock()
            public <T> T getVariable(final String name){
                if (variables == null) {
                    variables = new HashMap();
                }
                return (T) variables.get(name);
            }

            @Mock()
            public boolean setVariable(final String name, final Object value) {
                if (variables == null) {
                    variables = new HashMap();
                }
                variables.put(name, value);
                return true;
            }

            @Mock()
            public boolean removeVariable(final String name) {
                if (variables == null) {
                    variables = new HashMap();
                }
                if (variables.containsKey(name)) {
                    variables.remove(name);
                }
                return true;
            }

            @Mock()
            public Message getMessage() {
                return message;
            }
        }.getMockInstance();

        exeCtxt = new MockUp<ExecutionContext>(){ }.getMockInstance();

        // message = new MockUp<Message>(){
        //     @Mock()
        //     public InputStream getContentAsStream() {
        //         return new ByteArrayInputStream(messageContent.getBytes(StandardCharsets.UTF_8));
        //     }
        // }.getMockInstance();
    }


    @Test
    public void testRandomInt_SHA1PRNG() {
        String alg = "SHA1PRNG";

        Map properties = new HashMap();
        properties.put("algorithm", alg);
        properties.put("output-type", "int");

        for (int i=0; i < 10; i++) {
            SecureRandomCallout callout = new SecureRandomCallout(properties);
            ExecutionResult result = callout.execute(msgCtxt, exeCtxt);

            // check result and output
            Assert.assertEquals(result, ExecutionResult.SUCCESS);

            // retrieve output
            String output = msgCtxt.getVariable("prng_random");
            Assert.assertNotEquals(output, null, "random");
            System.out.printf("output: %s\n", output);
        }
    }

    @Test
    public void testRandomDefault_SHA1PRNG() {
        String alg = "SHA1PRNG";

        Map properties = new HashMap();
        properties.put("algorithm", alg);
        //properties.put("output-type", "int");

        for (int i=0; i < 10; i++) {
            SecureRandomCallout callout = new SecureRandomCallout(properties);
            ExecutionResult result = callout.execute(msgCtxt, exeCtxt);

            // check result and output
            Assert.assertEquals(result, ExecutionResult.SUCCESS);

            // retrieve output
            String output = msgCtxt.getVariable("prng_random");
            Assert.assertNotEquals(output, null, "random");
            System.out.printf("output: %s\n", output);
        }
    }

    @Test
    public void testRandomInt_NativePRNG() {
        String alg = "NativePRNG";

        Map properties = new HashMap();
        properties.put("algorithm", alg);
        properties.put("output-type", "int");

        for (int i=0; i < 10; i++) {
            SecureRandomCallout callout = new SecureRandomCallout(properties);
            ExecutionResult result = callout.execute(msgCtxt, exeCtxt);

            // check result and output
            Assert.assertEquals(result, ExecutionResult.SUCCESS);

            // retrieve output
            String output = msgCtxt.getVariable("prng_random");
            Assert.assertNotEquals(output, null, "random");
            System.out.printf("output: %s\n", output);
        }
    }

    @Test
    public void testRandomGaussian_NativePRNG() {
        String alg = "NativePRNG";

        Map properties = new HashMap();
        properties.put("algorithm", alg);
        properties.put("output-type", "gaussian");

        for (int i=0; i < 10; i++) {
            SecureRandomCallout callout = new SecureRandomCallout(properties);
            ExecutionResult result = callout.execute(msgCtxt, exeCtxt);

            // check result and output
            Assert.assertEquals(result, ExecutionResult.SUCCESS);

            // retrieve output
            String output = msgCtxt.getVariable("prng_random");
            Assert.assertNotEquals(output, null, "random");
            System.out.printf("output: %s\n", output);
        }
    }


    @Test
    public void testRandomUuid_NativePRNG() {
        String alg = "NativePRNG";

        Map properties = new HashMap();
        properties.put("algorithm", alg);
        properties.put("output-type", "uuid");

        for (int i=0; i < 10; i++) {
            SecureRandomCallout callout = new SecureRandomCallout(properties);
            ExecutionResult result = callout.execute(msgCtxt, exeCtxt);

            // check result and output
            Assert.assertEquals(result, ExecutionResult.SUCCESS);

            // retrieve output
            String output = msgCtxt.getVariable("prng_random");
            System.out.printf("output: %s\n", output);
            Assert.assertNotEquals(output, null, "random");
            Assert.assertEquals(output.length(), 36, "uuid length");
            UUID uuid = UUID.fromString(output); // must not throw
            Assert.assertEquals(uuid.toString(),output, "uuid conversion");
        }
    }

    @Test
    public void testBogusOutputType() {
        String alg = "NativePRNG";

        Map properties = new HashMap();
        properties.put("algorithm", alg);
        properties.put("output-type", "tree");

        SecureRandomCallout callout = new SecureRandomCallout(properties);
        ExecutionResult result = callout.execute(msgCtxt, exeCtxt);

        // check result and output
        Assert.assertEquals(result, ExecutionResult.ABORT);

        // retrieve output
        String output = msgCtxt.getVariable("prng_random");
        Assert.assertEquals(output, null, "random");
    }

    @Test
    public void testBogusAlgorithm() {
        String alg = "IMadeThisUp";

        Map properties = new HashMap();
        properties.put("algorithm", alg);
        properties.put("output-type", "int");

        SecureRandomCallout callout = new SecureRandomCallout(properties);
        ExecutionResult result = callout.execute(msgCtxt, exeCtxt);

        // check result and output
        Assert.assertEquals(result, ExecutionResult.ABORT);

        // retrieve output
        String output = msgCtxt.getVariable("prng_random");
        Assert.assertEquals(output, null, "random");
    }

}
