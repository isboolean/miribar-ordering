package com.miribar.ordering.mapper;

import com.miribar.ordering.entity.Staff;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface StaffMapper {

    Staff selectById(Long id);

    Staff selectByUsername(@Param("username") String username);

    Staff selectByPhone(@Param("phone") String phone);

    int insert(Staff staff);

    int update(Staff staff);
}
