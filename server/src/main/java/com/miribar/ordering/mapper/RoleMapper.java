package com.miribar.ordering.mapper;

import com.miribar.ordering.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RoleMapper {
    
    List<Role> selectAll();
    
    List<Role> selectByStatus(@Param("status") Integer status);
    
    Role selectById(@Param("id") Long id);
    
    Role selectByRoleCode(@Param("roleCode") String roleCode);
    
    int insert(Role role);
    
    int update(Role role);
    
    int deleteById(@Param("id") Long id);
    
    List<Role> selectRolesByStaffId(@Param("staffId") Long staffId);

    /**
     * 分页查询角色
     *
     * @param roleName  角色名称模糊匹配（可选）
     * @param roleCode  角色编码模糊匹配（可选）
     * @param dataScope 数据范围（可选）
     * @param status    状态（可选）
     * @param offset    偏移量
     * @param pageSize  每页条数
     */
    List<Role> selectPage(@Param("roleName") String roleName,
                          @Param("roleCode") String roleCode,
                          @Param("dataScope") Integer dataScope,
                          @Param("status") Integer status,
                          @Param("offset") int offset,
                          @Param("pageSize") int pageSize);

    /** 统计满足条件的角色总数（与 selectPage 条件一致） */
    long countByCondition(@Param("roleName") String roleName,
                          @Param("roleCode") String roleCode,
                          @Param("dataScope") Integer dataScope,
                          @Param("status") Integer status);
}
