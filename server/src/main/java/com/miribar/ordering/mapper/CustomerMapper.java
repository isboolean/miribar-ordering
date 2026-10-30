package com.miribar.ordering.mapper;

import com.miribar.ordering.entity.Customer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CustomerMapper {

    List<Customer> selectAll();

    Customer selectById(Long id);

    Customer selectByPhone(@Param("phone") String phone);

    List<Customer> selectByKeyword(@Param("keyword") String keyword);

    int insert(Customer customer);

    int update(Customer customer);

    int deleteById(@Param("id") Long id);
}
