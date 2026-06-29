package com.project.travel.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.project.travel.domain.Result;
import com.project.travel.domain.SysExchangeRecord;
import com.project.travel.domain.SysProduct;
import com.project.travel.enums.ResultCode;
import com.project.travel.service.SysExchangeRecordService;
import com.project.travel.service.SysProductService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("admin/product")
public class ProductAdminController {

    @Autowired
    private SysProductService productService;

    @Autowired
    private SysExchangeRecordService exchangeRecordService;

    @PostMapping("getSysProductPage")
    public Result getSysProductPage(@RequestBody SysProduct sysProduct) {
        Page<SysProduct> page = new Page<>(sysProduct.getPageNumber(), sysProduct.getPageSize());
        QueryWrapper<SysProduct> wrapper = new QueryWrapper<>();
        wrapper.lambda()
                .like(StringUtils.isNotBlank(sysProduct.getName()), SysProduct::getName, sysProduct.getName())
                .eq(sysProduct.getStatus() != null, SysProduct::getStatus, sysProduct.getStatus())
                .eq(sysProduct.getType() != null, SysProduct::getType, sysProduct.getType())
                .orderByAsc(SysProduct::getSort)
                .orderByDesc(SysProduct::getCreateTime);
        return Result.success(productService.page(page, wrapper));
    }

    @GetMapping("getSysProductById")
    public Result getSysProductById(@RequestParam("id") String id) {
        return Result.success(productService.getById(id));
    }

    @PostMapping("saveSysProduct")
    public Result saveSysProduct(@RequestBody SysProduct sysProduct) {
        if (sysProduct.getStock() == null || sysProduct.getStock() < 0) {
            sysProduct.setStock(0);
        }
        if (sysProduct.getStatus() == null) {
            sysProduct.setStatus(1);
        }
        if (sysProduct.getSort() == null) {
            sysProduct.setSort(0);
        }
        if (sysProduct.getType() == null) {
            sysProduct.setType(0);
        }
        boolean saved = productService.save(sysProduct);
        return saved ? Result.success() : Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
    }

    @PostMapping("editSysProduct")
    public Result editSysProduct(@RequestBody SysProduct sysProduct) {
        boolean updated = productService.updateById(sysProduct);
        return updated ? Result.success() : Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
    }

    @GetMapping("removeSysProduct")
    public Result removeSysProduct(@RequestParam("ids") String ids) {
        if (StringUtils.isBlank(ids)) {
            return Result.fail("请选择要删除的数据");
        }
        for (String id : ids.split(",")) {
            long exchangeCount = exchangeRecordService.count(new QueryWrapper<SysExchangeRecord>()
                    .lambda()
                    .eq(SysExchangeRecord::getRelatedId, id)
                    .in(SysExchangeRecord::getType, 0, 1, 2));
            if (exchangeCount > 0) {
                return Result.fail("存在兑换记录的商品，无法删除");
            }
            productService.removeById(id);
        }
        return Result.success();
    }

    @PostMapping("toggleStatus")
    public Result toggleStatus(@RequestBody SysProduct sysProduct) {
        SysProduct existing = productService.getById(sysProduct.getId());
        if (existing == null) {
            return Result.fail("商品不存在");
        }
        existing.setStatus(sysProduct.getStatus());
        productService.updateById(existing);
        return Result.success();
    }
}
