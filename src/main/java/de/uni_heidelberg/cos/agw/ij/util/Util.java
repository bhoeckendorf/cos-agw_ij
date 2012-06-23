/*
 * This file is part of the COS AGW ImageJ plugin bundle.
 * https://github.com/bhoeckendorf/cos-agw_ij
 * 
 * Copyright 2012 B. Hoeckendorf <b.hoeckendorf at web dot de>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.uni_heidelberg.cos.agw.ij.util;


public class Util {

	
	public static String addToFilename(String filename, String addition) {
		final int dotIdx = filename.lastIndexOf(".");
		if (dotIdx < 0 || dotIdx < filename.length() - 6)
			return filename + addition;
		return filename.substring(0, dotIdx) + addition + filename.substring(dotIdx);
	}
	
}