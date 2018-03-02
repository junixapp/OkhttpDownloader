package com.lxj.okhttpdownloaderdemo;

/**
 * 定义url常量
 *
 * @author lxj
 */
public interface Url {
    String ServerHost = "http://127.0.0.1:8090/";//内网ip
    String ImagePrefix = ServerHost + "image?name=";//图片前缀
    //下载的接口
    String Download = ServerHost + "download?name=%s";//
    //断点下载的地址
    String BreakDownload = ServerHost + "download?name=%s&range=%d";//
}
