package org.example.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.example.reggie.common.BaseContext;
import org.example.reggie.common.R;
import org.example.reggie.entity.Orders;
import org.example.reggie.service.OrdersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrdersController {
    @Autowired
    private OrdersService ordersService;

    /**
     * 提交订单
     * @param orders 因为是json格式所以需要RequestBody
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        ordersService.submit(orders);
        return R.success("用户下单成功");
    }
    //todo:这里差一个历史订单的查看
    @GetMapping("/userPage")
    public R<Page>page(int page,int pageSize){
        Long uid= BaseContext.getCurrentId();
        Page pageInfo=new Page(page,pageSize);
        LambdaQueryWrapper<Orders>queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getUserId,uid);

        ordersService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }
}
