package com.miribar.store.mapper;

import com.miribar.common.entity.Store;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StoreMapper {
    Store selectById(Long id);
}
