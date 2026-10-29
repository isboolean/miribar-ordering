package com.miribar.store.controller;

import com.miribar.common.entity.Store;
import com.miribar.common.result.Result;
import com.miribar.store.mapper.StoreMapper;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/store")
public class StoreController {
    private final StoreMapper storeMapper;
    public StoreController(StoreMapper storeMapper) { this.storeMapper = storeMapper; }

    @GetMapping("/{id}")
    public Result<Store> getStore(@PathVariable Long id) {
        Store store = storeMapper.selectById(id);
        if (store == null) return Result.error(404, "门店不存在");
        return Result.success(store);
    }
}
