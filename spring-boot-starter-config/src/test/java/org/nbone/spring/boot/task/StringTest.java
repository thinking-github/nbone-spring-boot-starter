package org.nbone.spring.boot.task;

import com.alibaba.druid.pool.DruidDataSource;
import com.google.common.base.CaseFormat;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.Test;
import org.springframework.beans.BeanUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

/**
 * @author thinking
 * @version 1.0
 * @since 1/22/21
 */
public class StringTest {


    @Test
    public void dd() {

        String name = CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_CAMEL, "maxActive");

        System.out.println(name);
        System.out.println(CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_CAMEL, "max-active"));


    }


    @Test
    public void bean() {

        PropertyDescriptor propertyDescriptor = BeanUtils.getPropertyDescriptor(DruidDataSource.class, "maxActive");
        Method set = propertyDescriptor.getWriteMethod();
        System.out.println(set.getName());



        PropertyDescriptor property = BeanUtils.getPropertyDescriptor(HikariDataSource.class, "maximumPoolSize");
        Method set1 = property.getWriteMethod();
        System.out.println(set1.getName());

    }
}
