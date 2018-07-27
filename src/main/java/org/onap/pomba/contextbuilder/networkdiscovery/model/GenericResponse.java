/*
 * ============LICENSE_START===================================================
 * Copyright (c) 2018 Amdocs
 * ============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=====================================================
 */
package org.onap.pomba.contextbuilder.networkdiscovery.model;

import java.io.Serializable;

public class GenericResponse implements Serializable {

 private static final long serialVersionUID = 1L;

 private Object responseObj;

 private String status;

 private String failureReason;

 public String getFailureReason() {
  return failureReason;
 }

 public void setFailureReason(String failureReason) {
  this.failureReason = failureReason;
 }

 public Object getResponseObj() {
  return responseObj;
 }

 public void setResponseObj(Object responseObj) {
  this.responseObj = responseObj;
 }

 public String getStatus() {
  return status;
 }

 public void setStatus(String status) {
  this.status = status;
 }

}
