package org.example.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.example.reggie.common.BaseContext;
import org.example.reggie.common.R;
import org.example.reggie.entity.ShoppingCart;
import org.example.reggie.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;


    @PostMapping("/add")
    public R<ShoppingCart>add(@RequestBody ShoppingCart shoppingCart){
//        log.info("购物车数据：{}",shoppingCart);
        //获取当前用户id
        Long uid= BaseContext.getCurrentId();
        shoppingCart.setUserId(uid);
        //查询当前菜品或者套餐是否在购物车中
        LambdaQueryWrapper<ShoppingCart>queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,uid);
        Long dishId=shoppingCart.getDishId();
        if(dishId!=null){
            //当前添加的是菜品
            queryWrapper.eq(ShoppingCart::getDishId,dishId);
        }else{
            //当前添加的是套餐
            Long setmealId=shoppingCart.getSetmealId();
            queryWrapper.eq(ShoppingCart::getSetmealId,setmealId);

        }
        ShoppingCart selectOne=shoppingCartService.getOne(queryWrapper);
        if(selectOne==null){
            //需要添加数据
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            selectOne=shoppingCart;
        }else{
            //在原来的基础上加一
            selectOne.setNumber(selectOne.getNumber()+1);
            shoppingCartService.updateById(selectOne);
        }


        return R.success(selectOne);
    }

    //todo:这边少一个post的sub方法
    @PostMapping("/sub")
    public R<ShoppingCart>sub(@RequestBody ShoppingCart shoppingCart){
        return null;
    }

    @GetMapping("/list")
    public R<List<ShoppingCart>>list(){
        log.info("查看购物车数据");
        Long uid= BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart>queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,uid);
        queryWrapper.orderByAsc(ShoppingCart::getCreateTime);
        List<ShoppingCart>list=shoppingCartService.list(queryWrapper);

        return R.success(list);
    }

    /**
     * 清空购物车
     * @return
     */
    @DeleteMapping("/clean")
    public R<String>clean(){
        Long uid= BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart>queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,uid);
        shoppingCartService.remove(queryWrapper);
        return R.success("清空购物车成功");
    }
}
