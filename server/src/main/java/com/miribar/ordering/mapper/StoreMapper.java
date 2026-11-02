package com.miribar.ordering.mapper;

import com.miribar.ordering.entity.Store;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StoreMapper {
    Store selectById(Long id);

    /** 按所属组织查门店ID（员工挂机构时用门店兜底） */
    Long selectIdByOrgId(@Param("orgId") Long orgId);

    /** 取第一个门店ID（系统只有一个门店时的兜底） */
    Long selectFirstId();

    /** 按组织ID集合查店铺（带组织名，数据权限过滤用） */
    List<Store> selectByOrgIds(@Param("orgIds") List<Long> orgIds);

    /** 按店铺ID集合查（带组织名） */
    List<Store> selectByIds(@Param("storeIds") List<Long> storeIds);

    /** 全部店铺（带组织名，无数据权限限制时使用） */
    List<Store> selectAllStores();
}
