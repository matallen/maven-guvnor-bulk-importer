/*
 * Copyright 2010 JBoss Inc
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

package org.jboss.drools.guvnor.importgenerator.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

/**
 * File IO helper class for reading/writing files and converting to/from base64
 */
public class FileIOHelper {
  public static final String FORMAT = "utf-8";

  public static String readAllAsBase64(File file) throws FileNotFoundException, IOException {
//    if (true) return "XXXXXXX";
    byte[] bytes = IOUtils.toByteArray(new FileInputStream(file));
    return toBase64(bytes);
  }

  public static String toBase64(byte[] b) throws UnsupportedEncodingException {
    byte[] b64 = Base64.encodeBase64(b);
    return new String(b64, "utf-8");
  }

  public static String fromBase64(byte[] b64) throws UnsupportedEncodingException {
    byte[] b = Base64.decodeBase64(b64);
    return new String(b, "utf-8");
  }

}
