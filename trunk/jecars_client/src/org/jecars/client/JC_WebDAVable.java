/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jecars.client;

/**
 *
 * @author weert
 */
public interface JC_WebDAVable {

  public void Dav_enable()  throws JC_Exception;
  public void Dav_disable() throws JC_Exception;

  public void   Dav_setDefaultFileType( String pFileType ) throws JC_Exception;
  public String Dav_getDefaultFileType();

  public void   Dav_setDefaultFolderType( String pFileType ) throws JC_Exception;
  public String Dav_getDefaultFolderType();

}
