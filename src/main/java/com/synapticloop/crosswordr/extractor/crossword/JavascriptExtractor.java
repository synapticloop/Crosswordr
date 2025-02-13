package com.synapticloop.crosswordr.extractor.crossword;

/*
 * Copyright (c) 2019 - 2021 Synapticloop.
 * 
 * All rights reserved.
 * 
 * This code may contain contributions from other parties which, where 
 * applicable, will be listed in the default build file for the project 
 * ~and/or~ in a file named CONTRIBUTORS.txt in the root of the project.
 * 
 * This source code and any derived binaries are covered by the terms and 
 * conditions of the Licence agreement ("the Licence").  You may not use this 
 * source code or any derived binaries except in compliance with the Licence.  
 * A copy of the Licence is available in the file named LICENSE.txt shipped with 
 * this source code or binaries.
 */

import com.synapticloop.crosswordr.extractor.BaseExtractor;

public class JavascriptExtractor extends BaseExtractor {

	@Override
	public String extract(String data) {
		int startQuote = data.indexOf("\"");
		int endQuote = data.lastIndexOf("\"");
		return(data.substring(startQuote + 1, endQuote).replaceAll("\\\\", ""));
	}

}
