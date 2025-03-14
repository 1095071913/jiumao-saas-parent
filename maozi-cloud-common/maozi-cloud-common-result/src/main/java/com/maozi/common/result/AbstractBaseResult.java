/*
 * Copyright 2012-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.maozi.common.result;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.maozi.base.error.code.SystemErrorCode;
import com.maozi.common.BaseCommon;
import com.maozi.common.result.error.ErrorResult;
import com.maozi.common.result.error.exception.BusinessResultException;
import com.maozi.utils.context.ApplicationEnvironmentContext;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@Schema(description = "接口结果集")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class AbstractBaseResult<D> implements Serializable {

	@Schema(description = "服务名称")
	private String applicationName = ApplicationEnvironmentContext.APPLICATION_NAME;
	
	@Schema(description = "数据")
	public abstract D getData();
	
	@Schema(description = "业务内码")
	public abstract Integer getCode();
	
	@JsonIgnore
	public Boolean isSuccess() {
		return getCode() == 200;
	}
	
	@JsonIgnore
	public <Result> Result getResult() {
		return (Result) this;
	}
	
	@JsonIgnore
	public D getResultDataThrowError() {
		if(!isSuccess()) {
			throwError();
		}
		return getData();
	}
	
	@JsonIgnore
	public D getResultNotNullDataThrowError(String serviceName) {
		
		if(!isSuccess()) {
			throwError();
		}
		
		if(BaseCommon.isNull(getData())) {
			throw new BusinessResultException(serviceName, SystemErrorCode.DATA_NOT_EXIST_ERROR,404);
		}
		
		return getData();
		
	}
	
	private void throwError() {
		ErrorResult<D> result = getResult();
		throw new BusinessResultException(result);
	}
	
}