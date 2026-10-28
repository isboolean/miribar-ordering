package com.miribar.ordering.mapper;

import com.miribar.ordering.entity.Store;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StoreMapper {
    Store selectById(Long id);
}
