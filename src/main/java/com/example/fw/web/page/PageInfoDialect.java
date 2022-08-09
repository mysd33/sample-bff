package com.example.fw.web.page;

import java.util.Collections;
import java.util.Set;

import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.dialect.IExpressionObjectDialect;
import org.thymeleaf.expression.IExpressionObjectFactory;

public class PageInfoDialect implements IExpressionObjectDialect {
    private static final String PAGE_INFO_DIALECT_NAME = "pageInfo";
    private static final Set<String> EXPRESSION_OBJECT_NAMES = 
    		Collections.singleton(PAGE_INFO_DIALECT_NAME);
    
	@Override
	public String getName() {
		return "PageInfo";
	}

    @Override
    public IExpressionObjectFactory getExpressionObjectFactory() {
        return new IExpressionObjectFactory() {
            @Override
            public Set<String> getAllExpressionObjectNames() {
                return EXPRESSION_OBJECT_NAMES;
            }

            @Override
            public Object buildObject(IExpressionContext context,
                    String expressionObjectName) {
                if (PAGE_INFO_DIALECT_NAME.equals(expressionObjectName)) {
                    return new PageInfo();
                }
                return null;
            }

            @Override
            public boolean isCacheable(String expressionObjectName) {
                return true;
            }

        };
    }


}
