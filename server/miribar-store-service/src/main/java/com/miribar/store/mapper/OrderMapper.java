package com.miribar.store.mapper;

import com.miribar.common.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface OrderMapper {
    List<Order> selectByTableId(@Param("tableId") Long tableId);
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
