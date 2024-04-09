package org.example.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.example.reggie.common.R;
import org.example.reggie.dto.DishDto;
import org.example.reggie.entity.Category;
import org.example.reggie.entity.Dish;
import org.example.reggie.entity.DishFlavor;
import org.example.reggie.service.CategoryService;
import org.example.reggie.service.DishFlavorService;
import org.example.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜品管理
 */
@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增菜品
     * @param dishDto
     * @return
     */

    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());
        dishService.saveWithFlavor(dishDto);
        return R.success("新增菜品以及口味 成功！");
    }


    /**
     * 菜品的分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page>page(int page,int pageSize ,String name){
        //创建分页构造器
        Page<Dish>pageInfo=new Page<>(page,pageSize);
        Page<DishDto>dishDtoPage=new Page<>();
        //条件构造器
        LambdaQueryWrapper<Dish>queryWrapper=new LambdaQueryWrapper<>();
        //添加过滤条件
        queryWrapper.like(name!=null,Dish::getName,name);
        //添加排序条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        //Service生效即可
        dishService.page(pageInfo,queryWrapper);
        //对象拷贝
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");

        List<Dish> records=pageInfo.getRecords();
        List<DishDto>list=records.stream().map((item)->{
            //拷贝普通属性
            DishDto dishDto=new DishDto();
            BeanUtils.copyProperties(item,dishDto);

            //查询并设置类别名称
            Long categoryId=item.getCategoryId();
            Category category=categoryService.getById(categoryId);
            if(category!=null){
                String categoryName=category.getName();
                dishDto.setCategoryName(categoryName);
            }
            return dishDto;
        }).collect(Collectors.toList());
        dishDtoPage.setRecords(list);

        return R.success(dishDtoPage);
    }

    /**
     * 获取需要修改的数据条：根据id查询菜品
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto>getEdit(@PathVariable Long id){
        DishDto dishDto=dishService.getByIdFlavor(id);
        return R.success(dishDto);
    }



    @PutMapping
    public R<String>update(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());
        dishService.updateWithFlavor(dishDto);
        return R.success("修改菜品以及口味 成功！");
    }


    /**
     * 获取所有当前种类的
     */

    @GetMapping("/list")
    public R<List<DishDto>>getlist(Dish dish){
        //og.info("TAKE A LOOK: "+String.valueOf(categoryId));
        //条件构造器
        LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(dish.getName()),Dish::getName,dish.getName());
        queryWrapper.eq(null!=dish.getCategoryId(),Dish::getCategoryId,dish.getCategoryId());
        //添加条件，查询状态为1（起售状态）的菜品
        queryWrapper.eq(Dish::getStatus,1);
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        List<Dish>dishes=dishService.list(queryWrapper);

        List<DishDto>dishDtos=dishes.stream().map(item->{
            DishDto dishDto=new DishDto();
            BeanUtils.copyProperties(item,dishDto);
            Category category=categoryService.getById(item.getCategoryId());
            if(category!=null){
                dishDto.setCategoryName(category.getName());
            }
            LambdaQueryWrapper<DishFlavor>wrapper=new LambdaQueryWrapper<>();
            wrapper.eq(DishFlavor::getDishId,item.getId());
            dishDto.setFlavors(dishFlavorService.list(wrapper));

            return dishDto;
        }).collect(Collectors.toList());

        return R.success(dishDtos);


    }

}
