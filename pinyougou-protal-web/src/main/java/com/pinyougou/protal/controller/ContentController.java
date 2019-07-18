package com.pinyougou.protal.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.content.service.ContentService;
import com.pinyougou.pojo.TbContent;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/content")
public class ContentController {
    /**
     * 根据Id查询
     */
    @Reference
    private ContentService contentService;

    @RequestMapping("/findByCategoryId")
    public List<TbContent> findByCategoryId(Long id){
        List<TbContent> byCategoryId = contentService.findByCategoryId(id);
        System.out.println("11111111111111111111111111111111111111");
        return byCategoryId;
    }
}
