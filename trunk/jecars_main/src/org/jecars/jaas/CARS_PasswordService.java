/*
 * Copyright 2007 NLR - National Aerospace Laboratory
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

import java.io.UnsupportedEncodingException;
import java.security.*;
import org.jecars.support.BASE64Encoder;

/**
 * CARS_PasswordService
 *
 * @version $Id: CARS_PasswordService.java,v 1.1 2007/09/26 14:14:38 weertj Exp $
 */
public final class CARS_PasswordService {
  
  private static CARS_PasswordService gInstance;
  
  private CARS_PasswordService() {    
  }

  /** encrypt
   *
   * @param pPassword
   * @return
   * @throws NoSuchAlgorithmException
   * @throws UnsupportedEncodingException
   */
  public synchronized String encrypt( final String pPassword ) throws NoSuchAlgorithmException, UnsupportedEncodingException {
    final MessageDigest md = MessageDigest.getInstance("SHA");
    md.update(pPassword.getBytes("UTF-8"));
    final byte raw[] = md.digest();
    return BASE64Encoder.encodeBuffer(raw);
  }
  
  public static synchronized CARS_PasswordService getInstance() {
    if (gInstance == null) {
      return new CARS_PasswordService();
    } else {
      return gInstance;
    }
  }

}
