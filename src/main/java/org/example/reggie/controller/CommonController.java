package org.example.reggie.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.reggie.common.R;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

/**
 * 文件的上传和下载
 */
@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {
    @Value("${reggie.path}")
    private String basePath;

    @PostMapping("/upload")

    public R<String> upload(MultipartFile file){
        //"file"和前端一致
        //file是临时文件，需要及时转存
        log.info(file.toString());
        String originFilename=file.getOriginalFilename();
        String suffix=originFilename.substring(originFilename.lastIndexOf("."));
        String s= UUID.randomUUID().toString();
        String fileName=s+suffix;
        //创建一个目录对象
        File dir = new File(basePath);
        //判断当前目录是否存在，不存在的话需要另外创建
        if(!dir.exists()){
            dir.mkdirs();
        }
        try{
            file.transferTo(new File(basePath+fileName));
        }catch(IOException e){
            e.printStackTrace();
        }
        return R.success(fileName);
    }

    /**
     * 文件下载
     * @param name
     * @param response
     */
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response){

        try {
            //输入流，通过输入流读取文件内容
            FileInputStream fileInputStream = new FileInputStream(new File(basePath + name));
            //输出流，通过输出流写回浏览器，在浏览器展示图片
            ServletOutputStream outputStream= response.getOutputStream();

            response.setContentType("image/jpeg");

            int len=0;
            byte[] bytes=new byte[1024];
            while((len=fileInputStream.read(bytes))!=-1){
                outputStream.write(bytes,0,len);
                outputStream.flush();
            }
            //关闭资源
            fileInputStream.close();
            outputStream.close();

        }catch(Exception e){
            e.printStackTrace();
        }


    }
}
