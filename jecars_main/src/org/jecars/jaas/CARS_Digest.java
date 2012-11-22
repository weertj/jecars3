/*
 * Copyright 2010 NLR - National Aerospace Laboratory
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

package org.jecars.jaas;

import java.util.HashMap;
import java.util.StringTokenizer;

/** CARS_Digest
 *
 */
public class CARS_Digest {

  /** parseAuthenticationString
   *
   * @param pAuth
   * @return
   */
  static public HashMap<String, String> parseAuthenticationString( final String pAuth ) {
    final StringTokenizer tk = new StringTokenizer(pAuth.substring( "Digest".length()), ",");
    final HashMap authMapping = new HashMap();
    while(tk.hasMoreTokens()) {
      final String token = tk.nextToken().trim();
      if (token.indexOf("realm") != -1) {
        final String realm = token.substring(token.indexOf('"')+1,token.length()-1);
        authMapping.put("realm", realm);
      } else if (token.indexOf("cnonce") != -1) {
        final String nonce = token.substring(token.indexOf('"')+1,token.length()-1);
        authMapping.put("cnonce", nonce);
      } else if (token.indexOf("nonce") != -1) {
        final String nonce = token.substring(token.indexOf('"')+1,token.length()-1);
        authMapping.put("nonce", nonce);
      } else if (token.indexOf("opaque") != -1) {
        final String opaque = token.substring(token.indexOf('"')+1,token.length()-1);
        authMapping.put("opaque", opaque);
      } else if (token.indexOf("nc") != -1) {
        final String nc = token.substring(token.indexOf('"')+4,token.length());
        authMapping.put("nc", nc);
      } else if (token.indexOf("response") != -1) {
        final String response = token.substring(token.indexOf('"')+1,token.length()-1);
        authMapping.put("response", response);
      } else if (token.indexOf("qop") != -1) {
        if (token.endsWith( "\"" )) {
          final String qop = token.substring(token.indexOf('"')+1,token.length()-1);
          authMapping.put("qop", qop);
        } else {
          if (token.indexOf('"')==-1) {
            final String qop = token.substring(token.indexOf('=')+1,token.length());
            authMapping.put("qop", qop);
          } else {
            final String qop = token.substring(token.indexOf('"')+1,token.length());
            authMapping.put("qop", qop);
          }
        }
      } else if (token.indexOf("uri") != -1) {
        final String uri = token.substring(token.indexOf('"')+1,token.length()-1);
        authMapping.put("uri", uri);
      } else if (token.indexOf("username") != -1) {
        final String opaque = token.substring(token.indexOf('"')+1,token.length()-1);
        authMapping.put("username", opaque);
      }
    }
    return authMapping;
  }



}
