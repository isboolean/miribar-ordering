package com.miribar.ordering.service;

import com.miribar.ordering.entity.Org;

import java.util.List;
import java.util.Map;

public interface OrgService {
    
    List<Org> listAll();
    
    List<Org> listEnabled();
    
    List<Map<String, Object>> listOrgTree();
    
    Org getById(Long id);
    
    Org getByOrgCode(String orgCode);
    
    void add(Org org);
    
    void update(Org org);
    
    void delete(Long id);
    
    List<Org> getChildren(Long parentId);
    
    List<Long> getOrgTreeIds(Long orgId);
    
    List<Org> search(String keyword);
}
