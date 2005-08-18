/*
 * Copyright 2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.components.flow.javascript;

import java.util.ArrayList;
import java.util.List;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.util.ExceptionUtils;
import org.apache.cocoon.util.location.Location;
import org.apache.cocoon.util.location.LocationImpl;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.debug.DebugFrame;
import org.mozilla.javascript.debug.DebuggableScript;
import org.mozilla.javascript.debug.Debugger;

/**
 * A Rhino debugger that tracks location information when an exception is raised in some JavaScript code.
 * It's purpose is to build a {@link org.apache.cocoon.ProcessingException} that holds the stacktrace
 * in the JavaScript code.
 * <p>
 * This debugger implementation is designed to be as lightweight and fast as possible, in order to have a
 * negligible impact on the performances of the Rhino interpreter.
 * 
 * @since 2.1.8
 * @version $Id$
 */
public class LocationTrackingDebugger implements Debugger {
    
    private List locations;
    private Throwable throwable;
    
    public void handleCompilationDone(Context cx, DebuggableScript fnOrScript, String source) {
        // nothing
    }

    public DebugFrame getFrame(Context cx, DebuggableScript fnOrScript) {
        return new StackTrackingFrame(fnOrScript);
    }

    /**
     * Get an exception that reflects the known location stack
     *
     * @param description a description for the exception
     * @param originalException the original exception
     * 
     * @return a suitable exception to throw
     * @see ProcessingException#throwLocated(String, Throwable, Location)
     */
    public Exception getException(String description, Exception originalException) throws ProcessingException {
        if (throwable == null || locations == null) {
            // Cannot to better for now
            return originalException;
        }

        // Unwrap original exception, if any, wrapped by Rhino (the wrapping
        // class is different in Rhino+cont and Rhino 1.6)
        Throwable cause = ExceptionUtils.getCause(throwable);
        if (cause != null)
            throwable = cause;

        return ProcessingException.throwLocated(description, throwable, locations);
    }

    private class StackTrackingFrame implements DebugFrame {
        
        DebuggableScript script;
        int line;

        public StackTrackingFrame(DebuggableScript script) {
            this.script = script;
        }
        
        public void onEnter(Context cx, Scriptable activation, Scriptable thisObj, Object[] args) {
            // nothing
        }
        
        public void onLineChange(Context cx, int lineNumber) {
            line = lineNumber;
        }

        public void onExceptionThrown(Context cx, Throwable ex) {
            throwable = ex;
        }

        public void onExit(Context cx, boolean byThrow, Object resultOrException) {
            if (byThrow) {
                String name = null;
                if (script.isFunction()) {
                    name = script.getFunctionName();
                } else {
                    name = "[script]";
                }

                if (locations == null) {
                    locations = new ArrayList(1); // start small
                }

                locations.add(new LocationImpl(name, script.getSourceName(), line, -1));

            } else if (locations != null) {
                // The exception was handled by the script: clear any recorded locations
                locations = null;
            }
        }
    }
}

