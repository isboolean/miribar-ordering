package com.miribar.ordering.service.impl;

import com.miribar.ordering.entity.Org;
import com.miribar.ordering.mapper.OrgMapper;
import com.miribar.ordering.service.OrgService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrgServiceImpl implements OrgService {
    
    private final OrgMapper orgMapper;
    
    public OrgServiceImpl(OrgMapper orgMapper) {
        this.orgMapper = orgMapper;
    }
    
    @Override
    public List<Org> listAll() {
        return orgMapper.selectAll();
    }
    
    @Override
    public List<Org> listEnabled() {
        return orgMapper.selectByStatus(1);
    }
    
    @Override
    public List<Map<String, Object>> listOrgTree() {
        List<Org> allOrgs = orgMapper.selectAll();
        return buildOrgTree(allOrgs, 0L);
    }
    
    @Override
    public Org getById(Long id) {
        return orgMapper.selectById(id);
    }
    
    @Override
    public Org getByOrgCode(String orgCode) {
        return orgMapper.selectByOrgCode(orgCode);
    }
    
    @Override
    @Transactional
    public void add(Org org) {
        // 检查组织编码是否已存在
        Org existing = orgMapper.selectByOrgCode(org.getOrgCode());
        if (existing != null) {
            throw new RuntimeException("组织编码已存在：" + org.getOrgCode());
        }
        
        if (org.getSortOrder() == null) {
            org.setSortOrder(0);
        }
        if (org.getStatus() == null) {
            org.setStatus(1);
        }
        if (org.getParentId() == null) {
            org.setParentId(0L);
        }
        
        orgMapper.insert(org);
    }
    
    @Override
    @Transactional
    public void update(Org org) {
        // 如果修改了组织编码，检查是否与其他组织冲突
        Org existing = orgMapper.selectById(org.getId());
        if (existing != null && !existing.getOrgCode().equals(org.getOrgCode())) {
            Org conflict = orgMapper.selectByOrgCode(org.getOrgCode());
            if (conflict != null && !conflict.getId().equals(org.getId())) {
                throw new RuntimeException("组织编码已存在：" + org.getOrgCode());
            }
        }
        
        orgMapper.update(org);
    }
    
    @Override
    @Transactional
    public void delete(Long id) {
        // 检查是否有子组织
        List<Org> children = orgMapper.selectByParentId(id);
        if (!children.isEmpty()) {
            throw new RuntimeException("存在子组织，无法删除");
        }
        
        orgMapper.deleteById(id);
    }
    
    @Override
    public List<Org> getChildren(Long parentId) {
        return orgMapper.selectByParentId(parentId);
    }
    
    @Override
    public List<Long> getOrgTreeIds(Long orgId) {
        List<Long> ids = new ArrayList<>();
        ids.add(orgId);
        
        // 递归获取所有子组织
        List<Org> children = orgMapper.selectByParentId(orgId);
        for (Org child : children) {
            ids.addAll(getOrgTreeIds(child.getId()));
        }
        
        return ids;
    }
    
    @Override
    public List<Org> search(String keyword) {
        return orgMapper.selectByKeyword(keyword);
    }
    
    /**
     * 构建组织树
     */
    private List<Map<String, Object>> buildOrgTree(List<Org> orgs, Long parentId) {
        List<Map<String, Object>> tree = new ArrayList<>();
        
        for (Org org : orgs) {
            if (parentId.equals(org.getParentId())) {
                Map<String, Object> node = new HashMap<>();
                node.put("id", org.getId());
                node.put("parentId", org.getParentId());
                node.put("orgName", org.getOrgName());
                node.put("orgCode", org.getOrgCode());
                node.put("orgType", org.getOrgType());
                node.put("leader", org.getLeader());
                node.put("phone", org.getPhone());
                node.put("address", org.getAddress());
                node.put("sortOrder", org.getSortOrder());
                node.put("status", org.getStatus());
                
                List<Map<String, Object>> children = buildOrgTree(orgs, org.getId());
                if (!children.isEmpty()) {
                    node.put("children", children);
                }
                
                tree.add(node);
            }
        }
        
        // 按排序字段排序
        tree.sort((a, b) -> {
            Integer sortA = (Integer) a.get("sortOrder");
            Integer sortB = (Integer) b.get("sortOrder");
            if (sortA == null) sortA = 0;
            if (sortB == null) sortB = 0;
            return sortA.compareTo(sortB);
        });
        
        return tree;
    }
}
