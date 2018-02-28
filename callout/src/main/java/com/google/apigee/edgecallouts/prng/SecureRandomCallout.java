// SecureRandomCallout.java
//
// This is the source code for a Java callout for Apigee Edge.
// This callout is very simple - it invokes java.security.SecureRandom()
// inserts the result into a context variable.
//
// Copyright 2017-2018 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
// ------------------------------------------------------------------

package com.google.apigee.edgecallouts.prng;

import com.google.apigee.edgecallouts.UuidEx;
import com.apigee.flow.execution.ExecutionContext;
import com.apigee.flow.execution.ExecutionResult;
import com.apigee.flow.execution.spi.Execution;
import com.apigee.flow.message.MessageContext;
import com.google.apigee.edgecallouts.CalloutBase;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang.exception.ExceptionUtils;

public class SecureRandomCallout extends CalloutBase implements Execution {
    private final static String varprefix= "prng_";

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
        super(properties);
    }

    public String getVarnamePrefix() {
        return varprefix;
    }
    
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
                case "uuid":
                    UuidEx uuid = UuidEx.randomUUID(prng);
                    msgCtxt.setVariable(varName("random"), uuid.toString());
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
