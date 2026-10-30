package com.miribar.ordering.controller;

import com.miribar.ordering.common.PageQuery;
import com.miribar.ordering.common.PageResult;
import com.miribar.ordering.common.Result;
import com.miribar.ordering.entity.Customer;
import com.miribar.ordering.mapper.CustomerMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/customer")
public class CustomerManageController {
    
    private final CustomerMapper customerMapper;
    
    public CustomerManageController(CustomerMapper customerMapper) {
        this.customerMapper = customerMapper;
    }
    
    /**
     * 分页获取客户列表
     *
     * @param keyword  昵称/手机号/OpenID（可选）
     * @param nickname 昵称（模糊，可选）
     * @param phone    手机号（模糊，可选）
     * @param openid   OpenID（模糊，可选）
     * @param page     页码，默认 1
     * @param pageSize 每页条数，默认 10，可选 15 / 30
     */
    @GetMapping("/list")
    public Result<PageResult<Customer>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String nickname,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String openid,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize) {
        int current = PageQuery.page(page);
        int size = PageQuery.pageSize(pageSize);
        String kw = trimToNull(keyword);
        String nicknameKw = trimToNull(nickname);
        String phoneKw = trimToNull(phone);
        String openidKw = trimToNull(openid);

        long total = customerMapper.countByCondition(nicknameKw, phoneKw, openidKw, kw);
        if (total == 0) {
            return Result.success(PageResult.empty(0, current, size));
        }
        List<Customer> customers = customerMapper.selectPage(nicknameKw, phoneKw, openidKw, kw,
                PageQuery.offset(current, size), size);
        // 清除密码字段
        customers.forEach(c -> c.setPassword(null));
        return Result.success(PageResult.of(customers, total, current, size));
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
    
    /**
     * 获取客户详情
     */
    @GetMapping("/{id}")
    public Result<Customer> getById(@PathVariable Long id) {
        Customer customer = customerMapper.selectById(id);
        if (customer == null) {
            return Result.error(404, "客户不存在");
        }
        customer.setPassword(null);
        return Result.success(customer);
    }
    
    /**
     * 新增客户
     */
    @PostMapping
    public Result<Void> add(@RequestBody Customer customer) {
        // 检查手机号是否已存在
        if (customer.getPhone() != null && !customer.getPhone().trim().isEmpty()) {
            Customer existing = customerMapper.selectByPhone(customer.getPhone().trim());
            if (existing != null) {
                return Result.error(400, "该手机号已被注册");
            }
        }
        
        customerMapper.insert(customer);
        return Result.success(null);
    }
    
    /**
     * 修改客户
     */
    @PutMapping
    public Result<Void> update(@RequestBody Customer customer) {
        Customer existing = customerMapper.selectById(customer.getId());
        if (existing == null) {
            return Result.error(404, "客户不存在");
        }
        
        // 如果修改了手机号，检查是否与其他客户冲突
        if (customer.getPhone() != null && !customer.getPhone().equals(existing.getPhone())) {
            Customer conflict = customerMapper.selectByPhone(customer.getPhone());
            if (conflict != null && !conflict.getId().equals(customer.getId())) {
                return Result.error(400, "该手机号已被其他客户使用");
            }
        }
        
        // 不允许通过此接口修改密码
        customer.setPassword(null);
        
        customerMapper.update(customer);
        return Result.success(null);
    }
    
    /**
     * 删除客户
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Customer existing = customerMapper.selectById(id);
        if (existing == null) {
            return Result.error(404, "客户不存在");
        }
        
        customerMapper.deleteById(id);
        return Result.success(null);
    }
}
