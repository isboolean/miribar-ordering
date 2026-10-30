package com.miribar.ordering.mapper;

import com.miribar.ordering.entity.StaffRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StaffRoleMapper {
    
    int insert(StaffRole staffRole);
    
    int batchInsert(@Param("list") List<StaffRole> list);
    
    int deleteByStaffId(@Param("staffId") Long staffId);
    
    List<Long> selectRoleIdsByStaffId(@Param("staffId") Long staffId);
    
    List<Long> selectStaffIdsByRoleId(@Param("roleId") Long roleId);
}
