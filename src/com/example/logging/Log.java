package com.example.logging;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Log {
	private static int getClassNumber(String className) {
		int i;
		String patternStr;// = "^Account\\z|^Account\\$[1-9]";
		Pattern pattern;
		Matcher matcher;
		boolean matchFound;
		for (i = 0; i < LogCode.ClassInfo.length; i++) {
			patternStr = String.format("^%s\\z|^%s\\$[1-9]",
					LogCode.ClassInfo[i], LogCode.ClassInfo[i]);
			pattern = Pattern.compile(patternStr);
			matcher = pattern.matcher(className);
			matchFound = matcher.find();
			if (matchFound)
				return Integer.valueOf(LogCode.ClassInfo[i + 1]);
		}
		return e(Log.class, LogCode.WAR_LOGER_NUMBER_NOT_FOUND);
	}

	public static synchronized Integer e(Class cls, String logCode, String extMsg) {
		String[] split = logCode.split(",", 2);
		int code = Integer.valueOf(split[0]);
		String msg = split[1];
		String className = cls.getName();
		int classNumber = getClassNumber(className);
		int rtValue = 0;
		if (code < 0) {
			rtValue = classNumber - code;
			Log.e(className, String.valueOf(String.format(
					"ErrCode: -%08d, logCode: %s(%s)", rtValue, msg, extMsg)));
			return -rtValue;
		} else {
			rtValue = classNumber + code;
			Log.e(className, String.valueOf(String.format(
					"ErrCode: %08d, logCode: %s(%s)", rtValue, msg, extMsg)));
			return rtValue;
		}
	}

	public static Integer e(Object obj, String logCode, String extMsg) {
		return e(obj.getClass(), logCode, "");
	}

	public static Integer e(Object obj, String logCode) {
		return e(obj.getClass(), logCode, "");
	}

	public static Integer e(Class cls, String logCode) {
		return e(cls, logCode, "");
	}
}