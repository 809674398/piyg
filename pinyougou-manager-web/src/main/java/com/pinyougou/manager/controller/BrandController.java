package com.pinyougou.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;
import entity.PageResult;
import entity.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/brand")
public class BrandController {
    @Reference
    private BrandService brandService;
    //查询总记录数
    @RequestMapping("/findAll")
    public List<TbBrand> findAll(){
    return brandService.findAll();
    }
    /**
     * 分页查询商品
     */
    @RequestMapping("/findPage")
    public PageResult findPage(int page, int size){
         return brandService.findPage(page,size);
    }
    /**
     * 增加商品
    */
    @RequestMapping("/add")
    public Result add(@RequestBody TbBrand tbBrand){
        try {
            brandService.add(tbBrand);
            return new Result(true,"添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"添加失败");
        }
    }

    /**
     * 根据id查询一个品牌
     * @param id
     * @return
     */
    @RequestMapping("/findOne")
    public TbBrand findOne(Long id){
        return brandService.findOne(id);
    }
    /**
     * 修改品牌
     */
    @RequestMapping("/update")
    public Result update(@RequestBody TbBrand tbBrand){
        try {
            brandService.update(tbBrand);
            return new Result(true,"修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"修改失败");
        }
    }
    /**
     * 删除品牌
     */
    @RequestMapping("/delete")
    public Result delete(Long[] ids){
        try {
             brandService.delete(ids);
            return new Result(true,"删除成功");
        } catch  (Exception e) {
            e.printStackTrace();
            return new Result(false,"删除失败");
        }
    }
    /**
     * 根据条件查询
     */
    @RequestMapping("/search")
    public PageResult search(@RequestBody TbBrand brand, int page, int size){
        return brandService.findPage(brand,page,size);
    }
    /**
     * 查询品牌下拉列表
     */
    @RequestMapping("/selectOptionList")
    public List<Map> selectOptionList(){
        return brandService.selectOptionList();
    }
}
