// SecureRandomCallout.java
//
// This is the source code for a Java callout for Apigee Edge.
// This callout is very simple - it invokes java.security.SecureRandom()
// inserts the result into a context variable.
//
// ------------------------------------------------------------------

package com.dinochiesa.edgecallouts;

import java.util.Map;
import java.util.HashMap;
import java.security.SecureRandom;
import java.security.NoSuchAlgorithmException;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.text.StrSubstitutor;
import com.dinochiesa.util.TemplateString;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.CacheLoader;
import java.util.concurrent.TimeUnit;

import com.apigee.flow.execution.ExecutionResult;
import com.apigee.flow.execution.spi.Execution;
import com.apigee.flow.message.MessageContext;
import com.apigee.flow.execution.ExecutionContext;


public class SecureRandomCallout implements Execution {
    private final static String varprefix= "prng_";
    private static String varName(String s) { return varprefix + s; }
    private Map properties; // read-only

    // It is expensive to initialize a SecureRandom. Also, instances of
    // SecureRandom are thread safe, once initialized. Therefore we want
    // to use a cache of these things.
    
    private final static int PRNG_CACHE_MAX_EXTRIES = 1024;
    private final static int PRNG_CACHE_WRITE_CONCURRENCY = 6;
    private static LoadingCache<String,SecureRandom> prngCache;

    static {
        prngCache =
            CacheBuilder.newBuilder()
            .concurrencyLevel(PRNG_CACHE_WRITE_CONCURRENCY)
            .maximumSize(PRNG_CACHE_MAX_EXTRIES)
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build(new CacheLoader<String,SecureRandom>() {
                    public SecureRandom load(String algorithm) throws NoSuchAlgorithmException {
                        SecureRandom prng = SecureRandom.getInstance(algorithm);
                        return prng;
                    }
                });
    }

    public SecureRandomCallout (Map properties) {
        this.properties = properties;
    }

    public SecureRandomCallout() { }

    private String getAlgorithm(MessageContext msgCtxt) throws Exception {
        String alg = getSimpleOptionalProperty("algorithm", msgCtxt);
        if (alg == null) {
            return "SHA1PRNG";
        }
        return alg;
    }

    private String getOutputType(MessageContext msgCtxt) throws Exception {
        String type = getSimpleOptionalProperty("output-type", msgCtxt);
        if (type == null) {
            return "int";
        }
        return type;
    }

    private String getDecimalDigits(MessageContext msgCtxt) throws Exception {
        String digits = getSimpleOptionalProperty("decimal-digits", msgCtxt);
        if (digits == null) {
            return "12";
        }
        return digits;
    }

    private String getSimpleOptionalProperty(String propName, MessageContext msgCtxt) throws Exception {
        String value = (String) this.properties.get(propName);
        if (value == null) { return null; }
        value = value.trim();
        if (value.equals("")) { return null; }
        value = resolvePropertyValue(value, msgCtxt);
        if (value == null || value.equals("")) { return null; }
        return value;
    }

    // If the value of a property value begins and ends with curlies,
    // eg, {apiproxy.name}, then "resolve" the value by de-referencing
    // the context variable whose name appears between the curlies.
    private String resolvePropertyValue(String spec, MessageContext msgCtxt) {
        if (spec.indexOf('{') > -1 && spec.indexOf('}')>-1) {
            // Replace ALL curly-braced items in the spec string with
            // the value of the corresponding context variable.
            TemplateString ts = new TemplateString(spec);
            Map<String,String> valuesMap = new HashMap<String,String>();
            for (String s : ts.variableNames) {
                valuesMap.put(s, (String) msgCtxt.getVariable(s));
            }
            StrSubstitutor sub = new StrSubstitutor(valuesMap);
            String resolvedString = sub.replace(ts.template);
            return resolvedString;
        }
        return spec;
    }

    public ExecutionResult execute (final MessageContext msgCtxt,
                                    final ExecutionContext execContext) {
        try {
            msgCtxt.removeVariable(varName("random"));
            String algorithm = getAlgorithm(msgCtxt);
            msgCtxt.setVariable(varName("algorithm"), algorithm);
            SecureRandom prng = prngCache.get(algorithm); // throws on invalid algorithm

            String outputType = getOutputType(msgCtxt);
            msgCtxt.setVariable(varName("output_type"), outputType);
            switch (outputType) {
                case "int" :
                    msgCtxt.setVariable(varName("random"), new Integer(prng.nextInt()).toString());
                    break;
                case "gaussian" :
                    double v = prng.nextGaussian();
                    String decimalDigits = getDecimalDigits(msgCtxt);
                    String format = "%." + decimalDigits + "f";
                    msgCtxt.setVariable(varName("random"), String.format(format, v));
                    break;
                default:
                    throw new Exception(String.format("invalid output-type: %s", outputType));
            }
            
            return ExecutionResult.SUCCESS;
        }
        catch (java.lang.Exception exc1) {
            msgCtxt.setVariable(varName("error"), exc1.getMessage());
            msgCtxt.setVariable(varName("stacktrace"), ExceptionUtils.getStackTrace(exc1));
            return ExecutionResult.ABORT;
        }
    }
}
