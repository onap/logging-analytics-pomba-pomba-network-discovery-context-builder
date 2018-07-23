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
package org.onap.pomba.contextbuilder.sd.exception;

import javax.ws.rs.core.Response.Status;

public class DiscoveryException extends Exception {

	private static final long serialVersionUID = -4874149714911165454L;

	private final Status httpStatus;

	public DiscoveryException(String message) {
	    this(message, Status.INTERNAL_SERVER_ERROR);
	}

	public DiscoveryException(String message, Status httpStatus) {
		super(message);
		if (httpStatus == null) {
		    throw new NullPointerException("httpStatus");
		}
		this.httpStatus = httpStatus;
	}

	public DiscoveryException(String message, Exception cause) {
	    super(message, cause);
	    this.httpStatus = Status.INTERNAL_SERVER_ERROR;
    }

    public Status getHttpStatus() {
		return this.httpStatus;
	}

}
