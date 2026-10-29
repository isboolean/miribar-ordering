package com.miribar.auth.mapper;

import com.miribar.common.entity.Staff;
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
