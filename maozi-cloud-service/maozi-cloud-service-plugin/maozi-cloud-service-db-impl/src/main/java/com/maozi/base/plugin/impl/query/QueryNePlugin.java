package com.maozi.base.plugin.impl.query;

import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.maozi.base.plugin.QueryBasePlugin;

public class QueryNePlugin<T> extends QueryBasePlugin<T>{

	@Override
	public void apply(MPJLambdaWrapper<T> wrapper,String field,Object data) {
		wrapper.ne(field, data);
	}

}
