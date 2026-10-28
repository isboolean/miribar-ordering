package com.miribar.ordering.controller;

import com.miribar.ordering.common.Result;
import com.miribar.ordering.entity.Store;
import com.miribar.ordering.mapper.StoreMapper;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/store")
public class StoreController {

    private final StoreMapper storeMapper;

    public StoreController(StoreMapper storeMapper) {
        this.storeMapper = storeMapper;
    }

    @GetMapping("/{id}")
    public Result<Store> getStore(@PathVariable Long id) {
        Store store = storeMapper.selectById(id);
        if (store == null) {
            return Result.error(404, "门店不存在");
        }
        return Result.success(store);
    }
}
