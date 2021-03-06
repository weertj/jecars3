/*
 * Copyright 2011 NLR - National Aerospace Laboratory
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

package org.jecars.tools.workflow;

import java.util.EnumSet;
import java.util.List;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 *
 */
public interface IWF_Task extends IWF_Default {
 
  EWF_TaskType              getType() throws RepositoryException;
  Node                      getToolTemplateNode() throws RepositoryException;
  EnumSet<EWF_TaskModifier> getModifiers() throws RepositoryException;
  List<IWF_TaskPort>        getInputs();
  List<IWF_TaskPort>        getOutputs();
  
  //  void         startTask() throws RepositoryException;

}
