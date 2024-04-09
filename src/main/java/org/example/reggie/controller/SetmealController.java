package org.example.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.example.reggie.common.R;
import org.example.reggie.dto.SetmealDto;
import org.example.reggie.entity.Category;
import org.example.reggie.entity.Setmeal;
import org.example.reggie.service.CategoryService;
import org.example.reggie.service.SetmealDishService;
import org.example.reggie.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 套餐管理
 */
@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private CategoryService categoryService;


    /**
     * 获取分页数据
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page> getPage(int page,int pageSize,String name){
        //分页构造器
        Page<Setmeal> pageInfo =new Page<>(page,pageSize);
        Page<SetmealDto>dtopage=new Page<>();
        LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper<>();
        //添加查询条件，根据name模糊查询
        queryWrapper.like(name!=null,Setmeal::getName,name);
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        setmealService.page(pageInfo,queryWrapper);

        //但是缺少分类信息，所以需要补上

        //对象拷贝:因为只需要考虑records中存的内容
        BeanUtils.copyProperties(pageInfo,dtopage,"records");
        List<Setmeal> records=pageInfo.getRecords();

        List<SetmealDto>list=records.stream().map((item)->{
            SetmealDto setmealdto=new SetmealDto();
            BeanUtils.copyProperties(item,setmealdto);
            Long cid=item.getCategoryId();
            Category category=categoryService.getById(cid);
            if(category!=null){
                String categoryName=category.getName();
                setmealdto.setCategoryName(categoryName);
            }
            return setmealdto;
        }).collect(Collectors.toList());
        dtopage.setRecords(list);
        return R.success(dtopage);
    }

    /**
     * 新增套餐
     * @param setmealDto
     * @return
     */
    @PostMapping
    public R<String>save(@RequestBody SetmealDto setmealDto){
        log.info("套餐信息：{}",setmealDto);
        setmealService.saveWithDish(setmealDto);
        return R.success("新增套餐成功");
    }

    @DeleteMapping
    public R<String>delete(@RequestParam List<Long>ids){
//        log.info("ids:{}",ids);
        setmealService.removeWithDish(ids);
        return R.success("套餐数据删除成功");
    }

    /**
     * 根据条件查询套餐数据
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    public R<List<Setmeal>>list(Setmeal setmeal){
        LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId()!=null,Setmeal::getCategoryId,setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus()!=null,Setmeal::getStatus,setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal>list=setmealService.list(queryWrapper);

        return R.success(list);
    }

}
