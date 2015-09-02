package com.pay.framework.db;
 
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * 扩展了  {@link AbstractRoutingDataSource}
 * Override 其中的 determineCurrentLookupKey方法实现数据源的route
 * 用于数据源读写分离
 * 现在有问题，spring不能在每个查询方法及时调用determineCurrentLookupKey，所以暂时停用
 * @author houzhaowei
 *
 */
public class DynamicDataSource extends AbstractRoutingDataSource{
 
    @Override
    public Object determineCurrentLookupKey() {
        return CustomerContextHolder.getCustomerType();
    }
}