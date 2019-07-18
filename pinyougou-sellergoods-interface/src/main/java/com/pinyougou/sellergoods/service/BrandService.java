package com.pinyougou.sellergoods.service;

import com.pinyougou.pojo.TbBrand;
import entity.PageResult;

import java.util.List;
import java.util.Map;

/**
 * 品牌接口
 */
public interface BrandService {
    /**
     * 查询所有品牌
     * @return
     */
    List<TbBrand> findAll();

    /**
     * 品牌分页
     * @param pageNum
     * @param pageSize
     * @return
     */
    PageResult findPage(int pageNum, int pageSize);

    /**
     * 新增一个品牌
     * @param tbBrand
     */
    void add(TbBrand tbBrand);
    /**
     * 根据id查询
     */
    TbBrand findOne(Long id);
    /**
     * 修改品牌
     */
    void update(TbBrand tbBrand);
    /**
     * 删除品牌
     */
    void delete(Long[] ids);
    /**
     * 条件查询
     */
    PageResult findPage(TbBrand tbBrand, int pageNum, int pageSize);
    /**
     * 查询品牌下拉列表
     */
    List<Map> selectOptionList();
}
