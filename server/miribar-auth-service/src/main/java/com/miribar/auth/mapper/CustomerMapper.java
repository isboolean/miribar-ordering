package com.miribar.auth.mapper;

import com.miribar.common.entity.Customer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CustomerMapper {
    Customer selectById(Long id);
    Customer selectByPhone(@Param("phone") String phone);
    int insert(Customer customer);
    int update(Customer customer);
}
