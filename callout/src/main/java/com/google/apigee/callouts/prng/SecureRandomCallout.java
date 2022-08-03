// SecureRandomCallout.java
//
// This is the source code for a Java callout for Apigee Edge.
// This callout is very simple - it invokes java.security.SecureRandom()
// inserts the result into a context variable.
//
// Copyright 2017-2022 Google LLC.
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

package com.google.apigee.callouts.prng;

import com.apigee.flow.execution.ExecutionContext;
import com.apigee.flow.execution.ExecutionResult;
import com.apigee.flow.execution.spi.Execution;
import com.apigee.flow.message.MessageContext;
import com.google.apigee.callouts.CalloutBase;
import com.google.apigee.callouts.UuidEx;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.LinkedHashMap;
import java.util.Map;

public class SecureRandomCallout extends CalloutBase implements Execution {
  private static final String varprefix = "prng_";

  // It is expensive to initialize a SecureRandom. Though instances of
  // SecureRandom are thread safe, once initialized, there may be contention
  // at high concurrency. Therefore this implementation uses a ThreadLocal
  // SecureRandom. Each thread can have a map of 10 of these.

  private static ThreadLocal<Map<String, SecureRandom>> prngMap =
      ThreadLocal.withInitial(
          () ->
              (new LinkedHashMap<String, SecureRandom>() {
                protected boolean removeEldestEntry(Map.Entry<String, SecureRandom> eldest) {
                  return size() > 10;
                }
              }));

  public SecureRandomCallout(Map properties) {
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

    static class Range {
            public Range(String specifier){
                String[] parts = specifier.trim().split("\\s*,\\s*");
                if (parts.length!=2)
                    throw new RuntimeException("range specifier is invalid");

                int min = Integer.parseInt(parts[0]);
                int max = Integer.parseInt(parts[1]);
            if (min >= max) throw new RuntimeException("max is not greater than min");
            this.min = min;
            this.max = max;
        }
        public int min, max;
    }

  private Range getRange(MessageContext msgCtxt) throws Exception {
    String rangeSpecifier = getSimpleOptionalProperty("range", msgCtxt);
    if (rangeSpecifier == null) {
      return null;
    }
    return new Range(rangeSpecifier);
  }

  public ExecutionResult execute(final MessageContext msgCtxt, final ExecutionContext execContext) {
    boolean debug = false;
    try {
      msgCtxt.removeVariable(varName("random"));
      debug = getDebug();
      String algorithm = getAlgorithm(msgCtxt);
      msgCtxt.setVariable(varName("algorithm"), algorithm);
      Map<String, SecureRandom> map = prngMap.get();

      SecureRandom prng =
          map.computeIfAbsent(
              algorithm,
              k -> {
                try {
                  return SecureRandom.getInstance(k);
                } catch (NoSuchAlgorithmException e1) {
                  throw new RuntimeException(e1);
                }
              });

      String outputType = getOutputType(msgCtxt);
      msgCtxt.setVariable(varName("output_type"), outputType);
      switch (outputType) {
        case "int":
          Range range = getRange(msgCtxt);
          int value = (range!=null) ? prng.nextInt(range.max + 1 - range.min) + range.min : prng.nextInt();
          msgCtxt.setVariable(varName("random"), new Integer(value).toString());
          break;
        case "gaussian":
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
    } catch (Exception e) {
      if (debug) {
        e.printStackTrace();
        msgCtxt.setVariable(varName("stacktrace"), exceptionStackTrace(e));
      }
      setExceptionVariables(e, msgCtxt);
      return ExecutionResult.ABORT;
    }

    return ExecutionResult.SUCCESS;
  }
}
