package com.company.itoms.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.DataPermissionInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.DataPermissionHandler;
import com.company.itoms.common.DataScopeContextHolder;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        
        // Data Permission Interceptor
        DataPermissionInterceptor dataPermissionInterceptor = new DataPermissionInterceptor(new DataPermissionHandler() {
            @Override
            public Expression getSqlSegment(Expression where, String mappedStatementId) {
                String sqlFilter = DataScopeContextHolder.getSqlFilter();
                if (!StringUtils.hasText(sqlFilter)) {
                    return where;
                }
                
                try {
                    Expression dataScopeExpression = CCJSqlParserUtil.parseCondExpression(sqlFilter);
                    if (where == null) {
                        return dataScopeExpression;
                    } else {
                        return new net.sf.jsqlparser.expression.operators.conditional.AndExpression(where, dataScopeExpression);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return where;
                }
            }
        });
        
        interceptor.addInnerInterceptor(dataPermissionInterceptor);
        
        // Pagination Interceptor
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        
        return interceptor;
    }
}
