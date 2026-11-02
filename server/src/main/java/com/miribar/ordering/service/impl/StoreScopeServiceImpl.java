package com.miribar.ordering.service.impl;

import com.miribar.ordering.common.BusinessException;
import com.miribar.ordering.common.JwtUtil;
import com.miribar.ordering.controller.AuthController;
import com.miribar.ordering.entity.Role;
import com.miribar.ordering.entity.Staff;
import com.miribar.ordering.entity.Store;
import com.miribar.ordering.mapper.StaffMapper;
import com.miribar.ordering.mapper.StoreMapper;
import com.miribar.ordering.service.OrgService;
import com.miribar.ordering.service.RoleService;
import com.miribar.ordering.service.StoreScopeService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class StoreScopeServiceImpl implements StoreScopeService {

    private final JwtUtil jwtUtil;
    private final StaffMapper staffMapper;
    private final RoleService roleService;
    private final OrgService orgService;
    private final StoreMapper storeMapper;

    public StoreScopeServiceImpl(JwtUtil jwtUtil, StaffMapper staffMapper,
                                 RoleService roleService, OrgService orgService,
                                 StoreMapper storeMapper) {
        this.jwtUtil = jwtUtil;
        this.staffMapper = staffMapper;
        this.roleService = roleService;
        this.orgService = orgService;
        this.storeMapper = storeMapper;
    }

    @Override
    public boolean hasStaffContext() {
        return currentStaff() != null;
    }

    @Override
    public List<Long> getAllowedStoreIds() {
        Staff staff = currentStaff();
        if (staff == null) {
            // 无员工上下文（顾客端/公开接口），不做店铺限制
            return null;
        }

        List<Role> roles = roleService.getRolesByStaffId(staff.getId());
        List<Integer> scopes = roles.stream()
                .map(Role::getDataScope)
                .filter(Objects::nonNull)
                .toList();
        if (scopes.isEmpty()) {
            return null;
        }
        int minScope = scopes.stream().min(Integer::compareTo).orElse(1);

        if (minScope == 1) {
            // 全部数据，不限制
            return null;
        }

        // 2=本组织及下级 3=仅本组织：组织 → 店铺（t_store.org_id）
        List<Long> orgIds;
        if (minScope == 2 && staff.getOrgId() != null) {
            orgIds = orgService.getOrgTreeIds(staff.getOrgId());
        } else if (staff.getOrgId() != null) {
            orgIds = List.of(staff.getOrgId());
        } else {
            orgIds = List.of();
        }

        List<Long> storeIds = new ArrayList<>();
        if (!orgIds.isEmpty()) {
            for (Store s : storeMapper.selectByOrgIds(orgIds)) {
                storeIds.add(s.getId());
            }
        }
        // 员工直接挂店铺时兜底（店铺未映射组织等情况）
        if (staff.getStoreId() != null && !storeIds.contains(staff.getStoreId())) {
            storeIds.add(staff.getStoreId());
        }
        return storeIds;
    }

    @Override
    public void checkStoreAllowed(Long storeId) {
        List<Long> allowed = getAllowedStoreIds();
        if (allowed == null) {
            return;
        }
        if (storeId == null || !allowed.contains(storeId)) {
            throw new BusinessException("无权访问该店铺数据，店铺不在您的数据范围内");
        }
    }

    private Staff currentStaff() {
        ServletRequestAttributes attrs = (ServletRequestAttributes)
                RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return null;
        }
        HttpServletRequest request = attrs.getRequest();
        String token = AuthController.extractToken(request.getHeader("Authorization"));
        String payload = token == null ? null : jwtUtil.verify(token);
        if (payload == null) {
            return null;
        }
        Long staffId = jwtUtil.getStaffId(payload);
        return staffId == null ? null : staffMapper.selectById(staffId);
    }
}
