package com.kenvix.utils.android;

import com.kenvix.utils.tools.StringTools;

import static com.kenvix.utils.tools.StringTools.convertPackageNameToUppercaseLetter;

public class PreprocessorName {
    public static String getViewAutoLoaderMethodName(String tag) {
        return "autoLoad" + StringTools.makeFirstLetterUppercase(convertPackageNameToUppercaseLetter(tag));
    }

    public static String getFormEmptyCheckerMethodName(String tag) {
        return StringTools.makeFirstLetterLowercase(convertPackageNameToUppercaseLetter(tag));
    }
}
