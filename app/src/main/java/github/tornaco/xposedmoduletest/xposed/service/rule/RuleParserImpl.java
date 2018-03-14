/*
 * Copyright (C) 2015 The Android Open Source Project
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

package github.tornaco.xposedmoduletest.xposed.service.rule;

import android.util.Log;

import java.util.Arrays;

import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

class RuleParserImpl implements RuleParser {

    private String[] mArgs;

    private String mCmd;
    private int mArgPos;
    private String mCurArgData;

    public void init(String[] args, int firstArgPos) {
        mArgs = args;
        mArgPos = firstArgPos;
        mCurArgData = null;
    }

    // ALLOW tornaco.github.abc *
    @Override
    public Rule parse(String args) {
        Log.d(XposedLog.TAG, "parse args: " + args);
        if (args == null) return null;
        String[] arr = args.split("\\s+");
        Log.d(XposedLog.TAG, "parse args: " + Arrays.toString(arr));
        int N = arr.length;
        if (N != 3) return null;
        RuleAction action = RuleAction.from(arr[0]);
        if (action == null) return null;
        RuleTargetType targetType1 = RuleTargetType.from(arr[1]);
        if (targetType1 == null) return null;
        RuleTargetType targetType2 = RuleTargetType.from(arr[2]);
        if (targetType2 == null) return null;
        Rule rule = new Rule();
        rule.setRuleAction(action);
        rule.setRuleTargetTypes(new RuleTargetType[]{targetType1, targetType2});
        rule.setRuleTargets(new String[]{arr[1], arr[2]});
        return rule;
    }

    /**
     * Return the next option on the command line -- that is an argument that
     * starts with '-'.  If the next argument is not an option, null is returned.
     */
    @Override
    public String getNextOption() {
        if (mCurArgData != null) {
            String prev = mArgs[mArgPos - 1];
            throw new IllegalArgumentException("No argument expected after \"" + prev + "\"");
        }
        if (mArgPos >= mArgs.length) {
            return null;
        }
        String arg = mArgs[mArgPos];
        if (!arg.startsWith("-")) {
            return null;
        }
        mArgPos++;
        if (arg.equals("--")) {
            return null;
        }
        if (arg.length() > 1 && arg.charAt(1) != '-') {
            if (arg.length() > 2) {
                mCurArgData = arg.substring(2);
                return arg.substring(0, 2);
            } else {
                mCurArgData = null;
                return arg;
            }
        }
        mCurArgData = null;
        return arg;
    }

    /**
     * Return the next argument on the command line, whatever it is; if there are
     * no arguments left, return null.
     */
    @Override
    public String getNextArg() {
        if (mCurArgData != null) {
            String arg = mCurArgData;
            mCurArgData = null;
            return arg;
        } else if (mArgPos < mArgs.length) {
            return mArgs[mArgPos++];
        } else {
            return null;
        }
    }

    @Override
    public String peekNextArg() {
        if (mCurArgData != null) {
            return mCurArgData;
        } else if (mArgPos < mArgs.length) {
            return mArgs[mArgPos];
        } else {
            return null;
        }
    }

    /**
     * Return the next argument on the command line, whatever it is; if there are
     * no arguments left, throws an IllegalArgumentException to report this to the user.
     */
    @Override
    public String getNextArgRequired() {
        String arg = getNextArg();
        if (arg == null) {
            String prev = mArgs[mArgPos - 1];
            throw new IllegalArgumentException("Argument expected after \"" + prev + "\"");
        }
        return arg;
    }

}
