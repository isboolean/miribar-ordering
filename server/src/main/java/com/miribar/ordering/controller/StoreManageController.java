package com.miribar.ordering.controller;

import com.miribar.ordering.common.Result;
import com.miribar.ordering.entity.Store;
import com.miribar.ordering.mapper.StoreMapper;
import com.miribar.ordering.service.StoreScopeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 店铺管理查询（业务管理模块用）
 * 路径不在公开白名单内，需员工登录；返回结果按当前员工数据范围（角色 dataScope → 组织 → 店铺）过滤。
 */
@RestController
@RequestMapping("/api/store/manage")
public class StoreManageController {

    private final StoreMapper storeMapper;
    private final StoreScopeService storeScopeService;

    public StoreManageController(StoreMapper storeMapper, StoreScopeService storeScopeService) {
        this.storeMapper = storeMapper;
        this.storeScopeService = storeScopeService;
    }

    /**
     * 当前员工可见的店铺列表（用于业务管理页店铺下拉与数据隔离）
     */
    @GetMapping("/list")
    public Result<List<Store>> list() {
        List<Long> allowed = storeScopeService.getAllowedStoreIds();
        List<Store> stores = (allowed == null)
                ? storeMapper.selectAllStores()
                : storeMapper.selectByIds(allowed);
        return Result.success(stores);
    }

    /**
     * 店铺详情（含所属组织），超出数据范围时拒绝
     */
    @GetMapping("/{storeId}")
    public Result<Store> detail(@PathVariable Long storeId) {
        storeScopeService.checkStoreAllowed(storeId);
        Store store = storeMapper.selectById(storeId);
        if (store == null) {
            return Result.error(404, "店铺不存在");
        }
        return Result.success(store);
    }
}
