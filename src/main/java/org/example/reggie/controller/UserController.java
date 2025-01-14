package org.example.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.example.reggie.common.R;
import org.example.reggie.entity.User;
import org.example.reggie.service.UserService;
import org.example.reggie.utils.SMSUtils;
import org.example.reggie.utils.ValidateCodeUtils;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;


@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/sendMsg")
    public R<String>sendMsg(@RequestBody User user, HttpSession session){
        //获取手机号
        String phone=user.getPhone();
        if(!StringUtils.isNotEmpty(phone)){
            return R.error("短信发送失败");
        }

        //生成随机的验证码
        String code=ValidateCodeUtils.generateValidateCode(4).toString();
        //调用阿里云服务
        //SMSUtils.sendMessage("阿里云短信测试", "SMS_154950909",phone,code);
        log.info("验证码：{}",code);
        log.info("当前短信功能已经关闭，需要的时候再开启即可");
        //将生成的验证码保存至Session
        session.setAttribute(phone,code);
        return R.success("发送验证码成功");
    }

    /**
     * 移动端用户登录
     * @param map
     * @param session
     * @return
     */
    @PostMapping("/login")
    public R<User>login(@RequestBody Map map, HttpSession session){
        //通过键值对获取参数
        log.info(map.toString());
        String phone=map.get("phone").toString();
        String code=map.get("code").toString();
        Object codeInSession=session.getAttribute(phone);
        if(codeInSession!=null&&codeInSession.equals(code)){
            //验证成功

            //判断是否为新用户
            LambdaQueryWrapper<User> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone,phone);
            User user=userService.getOne(queryWrapper);
            if(user==null){
                //新用户，自动注册
                user=new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
            session.setAttribute("user",user.getId());
            return R.success(user);
        }
        return R.error("登录失败，验证码错误");
    }
}
