package com.ztman.camera.controller;

import com.sun.org.apache.bcel.internal.generic.ARETURN;
import com.ztman.camera.util.camera.R;
import com.ztman.camera.util.camera.cache.CacheUtil;
import com.ztman.camera.util.camera.pojo.VideoPojo;
import com.ztman.camera.util.camera.thread.VideoPlayThread;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: wsd
 * @date: 2021/3/11 9:31
 */
@Slf4j
@RestController
@RequestMapping("flv")
public class flvController {
    // 存放任务 线程
    public static Map<String, VideoPlayThread.MyRunnable> videoJobMap = new HashMap<String, VideoPlayThread.MyRunnable>();

    private final String MP4_URL = "/home/0301.mp4";
//    private final String MP4_URL = "C:\\Users\\Administrator\\Desktop\\下载\\0301.mp4";
    /**
     * 获取视频流
     * @param token 视频存放信息索引
     */
    @GetMapping("/getVideo")
    public void getVideo(HttpServletRequest request, HttpServletResponse response, @RequestParam("token") String token)
    {
        //视频资源存储信息
        response.reset();
        //获取从那个字节开始读取文件
        String rangeString = request.getHeader("Range");
        try {
            //获取响应的输出流
            OutputStream outputStream = response.getOutputStream();
            File file = new File(MP4_URL);
            if(file.exists()){
                RandomAccessFile targetFile = new RandomAccessFile(file, "r");
                long fileLength = targetFile.length();
                VideoPojo videoPojo = new VideoPojo();
                //播放
                if( videoJobMap.get(token) != null) {
                    if (rangeString != null) {

                        long range = Long.valueOf(rangeString.substring(rangeString.indexOf("=") + 1, rangeString.indexOf("-")));
                        long playRange = (fileLength - 1);
                        System.out.println(range);
                        String dd = String.valueOf(fileLength - range);
                        //设置内容类型
                        response.setHeader("Content-Type", "video/mp4");
                        //设置此次相应返回的数据长度
//                    response.setHeader("Content-Length", String.valueOf(fileLength - range));
                        response.setHeader("Content-Length", String.valueOf(1024 * 1024));
                        //设置此次相应返回的数据范围
                        if ((range + (1024 * 1024)) < fileLength) {
                            playRange = (range + (1024 * 1024));
                            response.setHeader("Content-Range", "bytes " + range + "-" + playRange + "/" + fileLength);
                        } else if ((range + (1024 * 1024)) >= fileLength) {
                            response.setHeader("Content-Range", "bytes " + range + "-" + playRange + "/" + fileLength);
                        }
                        //返回码需要为206，而不是200
                        response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                        //设定文件读取开始位置（以字节为单位）
                        targetFile.seek(range);
                        videoPojo.setRange(range);
                        videoPojo.setLength(fileLength);
                        videoPojo.setVideoName("官网视频");
                        videoPojo.setType("mp4");
                        videoPojo.setStatus(true);
                        videoPojo.setToken(token);
                        videoPojo.setOpenTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis()));
                        VideoPlayThread.MyRunnable job = new VideoPlayThread.MyRunnable(videoPojo);
                        VideoPlayThread.MyRunnable.es.execute(job);
                        videoJobMap.put(token, job);
                    } else {//下载

                        //设置响应头，把文件名字设置好
                        response.setHeader("Content-Disposition", "attachment; filename=" + "官网视频");
                        //设置文件长度
                        response.setHeader("Content-Length", String.valueOf(fileLength));
                        //解决编码问题
                        response.setHeader("Content-Type", "application/octet-stream");
                    }


                    byte[] cache = new byte[1024 * 1024];
                    int flag;
                    while ((flag = targetFile.read(cache)) != -1) {
                        outputStream.write(cache, 0, flag);
                    }
                }
            }else {
                String message = "file:"+"官网视频"+" not exists";
                //解决编码问题
                response.setHeader("Content-Type","application/json");
                outputStream.write(message.getBytes(StandardCharsets.UTF_8));
            }

            outputStream.flush();
            outputStream.close();
        } catch (FileNotFoundException e) {

        } catch (IOException e) {

        }
    }

    /**
     * @Title: keepAlive
     * @Description:视频流保活
     * @param tokens
     * @return void
     **/
    @GetMapping("/video/{tokens}")
    public R keepAlive(@PathVariable("tokens") String tokens) {
        // 校验参数
        if (null != tokens && !"".equals(tokens)) {
            String[] tokenArr = tokens.split(",");
            for (String token : tokenArr) {
                VideoPojo videoPojo = new VideoPojo();
                // 直播流token
                if (null != CacheUtil.START_VIDEO_MAP.get(token)) {
                    videoPojo = CacheUtil.START_VIDEO_MAP.get(token);
                    // 更新当前系统时间
                    videoPojo.setOpenTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis()));
                    log.debug("视频流：" + videoPojo.getToken() + "保活！");

                    return new R("成功","成功");
                }
            }
        }
        return new R(false,"失败");
    }

    volatile int thread_num = 0;

    /**
     * 断点下载
     * @param request
     * @param response
     * @throws IOException
     */
    @RequestMapping("download")
    public void download(HttpServletRequest request, HttpServletResponse response) throws IOException {
        synchronized (this) {
            thread_num++;
            System.out.println("当前下载线程数：" + thread_num);
        }
        response.reset();
        //是否断点续传
        boolean fromBreakpoint = true;
        String rangeInHeader = request.getHeader("Range");

        String fileFullPath = MP4_URL;
        File file = new File(fileFullPath);
        long filelength = file.length();
        String fileName = "测试.mp4";
        String contentType = null;
        fileName = java.net.URLEncoder.encode(fileName, "utf-8");
        //开始字节位置
        long start = 0;
        //结束字节位置，默认为整个文件最后一个字节
        long end = filelength - 1;

        if ("".equals(rangeInHeader) || rangeInHeader == null) {
            fromBreakpoint = false;
            //----------获取ContentType---------
            Path path = Paths.get(fileFullPath);
            //JDK自带方法获取文件的contentType，这种方式前提是文件不能太大，否则获取不到，更多方式参照https://blog.csdn.net/qq_41911762/article/details/103639175
            contentType = Files.probeContentType(path);
        }
        //如果是部分传输
        if (fromBreakpoint) {
            System.out.println(rangeInHeader);
            String[] range = rangeInHeader.split("=")[1].split("-");
            String contentRange;
            start = Long.valueOf(range[0]);
            //bytes=n- 的情况，列出来，方便理解
            if (range.length == 1) {
            //bytes=n-m的情况
            } else {
                end = Long.valueOf(range[1]);
            }
            contentType = "multipart/byteranges";
            contentRange = "bytes " + start + "-" + end + "/" + filelength;
            response.setHeader("Content-Range", contentRange);
            //表部分传输
            response.setStatus(206);
        }

        response.setContentType(contentType);
        response.setContentLengthLong(filelength);

        try (BufferedInputStream br = new BufferedInputStream(new FileInputStream(file))) {

            //---------跳过已传输过的字节--------------
            br.skip(start);
            OutputStream out = response.getOutputStream();
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

            //-----------向输出流写入数据---------
            //已传输字节数
            //需要传输字节数
            long count = 0;
            long num = end - start + 1;
            //每次读到的字节数
            int len;
            //根据实际情况修改大小
            byte[] buf = new byte[4 * 1024];
            //超过了需要传输的字节数则不用传了
            while ((len = br.read(buf)) > 0 && count <= num) {
                out.write(buf, 0, len);
                count += len;
            }

        } catch (Exception e) {
            System.out.println("连接断开");
        }
        synchronized (this) {
            thread_num--;
            System.out.println("当前下载线程数：" + thread_num);
        }

    }

}
