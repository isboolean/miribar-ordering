package com.miribar.ordering.mapper;

import com.miribar.ordering.entity.Customer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CustomerMapper {

    Customer selectById(Long id);

    Customer selectByPhone(@Param("phone") String phone);

    int insert(Customer customer);

    int update(Customer customer);
}
