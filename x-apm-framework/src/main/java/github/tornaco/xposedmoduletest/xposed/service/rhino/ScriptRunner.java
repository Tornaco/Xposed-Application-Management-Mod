package github.tornaco.xposedmoduletest.xposed.service.rhino;/* -*- Mode: java; tab-width: 8; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

import com.google.common.io.Files;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;

import github.tornaco.xposedmoduletest.xposed.service.rhino.android.RhinoAndroidHelper;
import github.tornaco.xposedmoduletest.xposed.service.rhino.objects.Counter;
import github.tornaco.xposedmoduletest.xposed.service.rhino.objects.ServerSide;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;


/**
 * ScriptRunner: simplest example of controlling execution of Rhino.
 * <p>
 * Collects its arguments from the command line, executes the
 * script, and prints the result.
 *
 * @author Norris Boyd
 */
public class ScriptRunner {

    public static void run(android.content.Context androidContext, File file)
            throws IOException, IllegalAccessException, InstantiationException, InvocationTargetException {
        String c = Files.asCharSource(file, Charset.defaultCharset()).read();
        run(androidContext, new String[]{c});
    }

    public static void run(android.content.Context androidContext,
                           String args[])
            throws IllegalAccessException,
            InvocationTargetException,
            InstantiationException {
        // Creates and enters a Context. The Context stores information
        // about the execution environment of a script.
        Context cx = new RhinoAndroidHelper().enterContext();

        // By default, rhino will try do optimization by generating JVM bytecode on the fly.
        // Android doesn't run JVM bytecode byt Dalvik bytecode.
        // That is why you have to disable optimization:
        cx.setOptimizationLevel(-1);

        try {
            // Initialize the standard objects (Object, Function, etc.)
            // This must be done before scripts can be executed. Returns
            // a scope object that we use in later calls.
            ServerSide serverSide = new ServerSide(cx, androidContext);

            // Add serverSide variables.
            ScriptableObject.defineClass(serverSide, Counter.class);

            // Collect the arguments into a single string.
            StringBuilder s = new StringBuilder();
            for (String arg : args) {
                s.append(arg);
            }

            // Now evaluate the string we've colected.
            Object result = cx.evaluateString(serverSide, s.toString(), "script.js", 1, null);

            // Convert the result to a string and print it.
            XposedLog.debug((Context.toString(result)));

        } finally {
            // Exit from the context.
            Context.exit();
        }
    }
}

