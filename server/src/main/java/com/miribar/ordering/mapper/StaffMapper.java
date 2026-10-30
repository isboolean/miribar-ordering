package com.miribar.ordering.mapper;

import com.miribar.ordering.entity.Staff;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StaffMapper {

    Staff selectById(Long id);

    Staff selectByUsername(@Param("username") String username);

    Staff selectByPhone(@Param("phone") String phone);
    
    List<Staff> selectAll();
    
    List<Staff> selectByStoreId(@Param("storeId") Long storeId);
    
    List<Staff> selectByKeyword(@Param("keyword") String keyword);

    int insert(Staff staff);

    int update(Staff staff);
    
    int deleteById(@Param("id") Long id);
    
    int updatePassword(@Param("id") Long id, @Param("password") String password);
}
