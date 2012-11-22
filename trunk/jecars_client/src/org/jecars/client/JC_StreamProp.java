/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jecars.client;

import java.util.EnumSet;

/**
 *
 * @author weert
 */
public enum JC_StreamProp {

  NORMAL, FRAGMENT;

  static final public EnumSet<JC_StreamProp> DEFAULT    = EnumSet.of( NORMAL );
  static final public EnumSet<JC_StreamProp> FRAGMENTED = EnumSet.of( FRAGMENT );
    
    
}
