package com.miribar.ordering.mapper;

import com.miribar.ordering.entity.DiningTable;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TableMapper {

    List<DiningTable> selectByStoreId(@Param("storeId") Long storeId);

    DiningTable selectById(Long id);

    DiningTable selectByQrToken(@Param("qrToken") String qrToken);

    int insert(DiningTable table);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    int update(DiningTable table);

    int deleteById(Long id);
}
