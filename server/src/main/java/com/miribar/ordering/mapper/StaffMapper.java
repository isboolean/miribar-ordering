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

    /** 当前最大的纯数字工号（10 位），无则返回 null */
    Long selectMaxStaffNo();
    
    List<Staff> selectAll();
    
    List<Staff> selectByStoreId(@Param("storeId") Long storeId);
    
    List<Staff> selectByKeyword(@Param("keyword") String keyword);

    /**
     * 条件查询员工（条件全部下推到 SQL）
     *
     * @param storeId 门店ID（可选）
     * @param orgIds  机构ID集合，含下级机构（可选）
     * @param keyword 账号/姓名/手机号模糊匹配（可选）
     */
    List<Staff> selectByCondition(@Param("storeId") Long storeId,
                                  @Param("orgIds") List<Long> orgIds,
                                  @Param("keyword") String keyword);

    /**
     * 分页查询员工
     *
     * @param storeId  门店ID（可选）
     * @param orgIds   机构ID集合，含下级机构（可选）
     * @param keyword  工号/姓名/手机号模糊匹配（可选）
     * @param phone    手机号模糊匹配（可选）
     * @param role     角色（可选）
     * @param status   状态（可选）
     * @param offset   偏移量
     * @param pageSize 每页条数
     */
    List<Staff> selectPage(@Param("storeId") Long storeId,
                           @Param("orgIds") List<Long> orgIds,
                           @Param("keyword") String keyword,
                           @Param("phone") String phone,
                           @Param("role") Integer role,
                           @Param("status") Integer status,
                           @Param("offset") int offset,
                           @Param("pageSize") int pageSize);

    /** 统计满足条件的员工总数（与 selectPage 条件一致） */
    long countByCondition(@Param("storeId") Long storeId,
                          @Param("orgIds") List<Long> orgIds,
                          @Param("keyword") String keyword,
                          @Param("phone") String phone,
                          @Param("role") Integer role,
                          @Param("status") Integer status);

    int insert(Staff staff);

    int update(Staff staff);
    
    int deleteById(@Param("id") Long id);
    
    int updatePassword(@Param("id") Long id, @Param("password") String password);
}
