package com.project.travel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.project.travel.domain.SysProduct;
import com.project.travel.mapper.SysProductMapper;
import com.project.travel.service.SysProductService;
import org.springframework.stereotype.Service;

@Service
public class SysProductServiceImpl extends ServiceImpl<SysProductMapper, SysProduct> implements SysProductService {
}
