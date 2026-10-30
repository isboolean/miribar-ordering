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

    Customer selectByOpenid(@Param("openid") String openid);

    List<Customer> selectByKeyword(@Param("keyword") String keyword);

    /**
     * 分页查询客户
     *
     * @param nickname 昵称模糊匹配（可选）
     * @param phone    手机号模糊匹配（可选）
     * @param openid   OpenID 模糊匹配（可选）
     * @param keyword  昵称/手机号/OpenID 统一模糊匹配（可选）
     * @param offset   偏移量
     * @param pageSize 每页条数
     */
    List<Customer> selectPage(@Param("nickname") String nickname,
                              @Param("phone") String phone,
                              @Param("openid") String openid,
                              @Param("keyword") String keyword,
                              @Param("offset") int offset,
                              @Param("pageSize") int pageSize);

    /** 统计满足条件的客户总数（与 selectPage 条件一致） */
    long countByCondition(@Param("nickname") String nickname,
                          @Param("phone") String phone,
                          @Param("openid") String openid,
                          @Param("keyword") String keyword);

    int insert(Customer customer);

    int update(Customer customer);

    int deleteById(@Param("id") Long id);
}
