package com.tkclock.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class StringUtils {
	private static String default_delimiter = ";";
	public static List<String> SplitUsingToken(String Subject, String delimiters) 
    {
		if(delimiters == null || delimiters == "")
			delimiters = default_delimiter;
		StringTokenizer StrTkn = new StringTokenizer(Subject, delimiters);
		List<String> ArrLis = new ArrayList<String>();
		while(StrTkn.hasMoreTokens()) {
			ArrLis.add(StrTkn.nextToken());
		}
		return ArrLis;
	}
	
	public static String list2String(List<String> list, String delimiter) {
		String result = "";
		if(list == null)
			return result;
		
		if(delimiter == null)
			// Default delimiter: ";"
			delimiter = default_delimiter;
		for(int i = 0; i < list.size(); i++) {
			result = result + list.get(i) + delimiter;
		}
		
		return result;
	}
}
