package com.hcc.config.center.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ConfigCenterProperties
 *
 * @author hushengjun
 * @date 2022/10/29
 */
@Data
@ConfigurationProperties(prefix = "config.center")
public class ConfigCenterProperties {

    /**
     * 应用编码
     */
    private String appCode;
    /**
     * 密钥
     */
    private String secretKey;
    /**
     * 服务地址
     */
    private String serverUrl;
    /**
     * 是否开启动态推送
     */
    private Boolean enableDynamicPush = false;
    /**
     * 是否检查配置在配置中心存在，为true则检查不存在抛出异常
     */
    private Boolean checkConfigExist = true;
    /**
     * 拉取时间间隔，默认5分钟
     */
    private Integer pullInterval = 300;
    /**
     * 长轮询hold时间
     */
    private Integer longPollingTimeout = 90000;

}
