package com.hcc.config.center.autoconfigure;

import com.hcc.config.center.client.ConfigService;
import com.hcc.config.center.client.ProcessFailedCallBack;
import com.hcc.config.center.client.context.ConfigContext;
import com.hcc.config.center.client.entity.AppMode;
import com.hcc.config.center.client.spring.ConfigCenterBeanPostProcessor;
import com.hcc.config.center.client.spring.ConfigCenterClientInitializer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

import javax.annotation.PreDestroy;

/**
 * 配置中心自动配置
 *
 * @author hushengjun
 * @date 2022/10/29
 */
@Configuration
@EnableConfigurationProperties(ConfigCenterProperties.class)
@ConditionalOnProperty(value = "config.center.enabled", havingValue = "true")
public class ConfigCenterAutoConfiguration {

    private final ConfigCenterProperties configCenterProperties;
    private final ConfigContext configContext;

    public ConfigCenterAutoConfiguration(ConfigCenterProperties configCenterProperties) {
        this.configCenterProperties = configCenterProperties;
        this.configContext = this.buildConfigContext();
    }

    /**
     * 构建配置上下文
     * @return
     */
    public ConfigContext buildConfigContext() {
        ConfigContext configContext = new ConfigContext();

        configContext.setAppCode(configCenterProperties.getAppCode());
        configContext.setSecretKey(configCenterProperties.getSecretKey());
        configContext.setServerUrl(configCenterProperties.getServerUrl());
        if (configCenterProperties.getEnableDynamicPush() != null) {
            configContext.setEnableDynamicPush(configCenterProperties.getEnableDynamicPush());
        }
        if (configCenterProperties.getCheckConfigExist() != null) {
            configContext.setCheckConfigExist(configCenterProperties.getCheckConfigExist());
        }
        if (configCenterProperties.getPullInterval() != null) {
            configContext.setPullInterval(configCenterProperties.getPullInterval());
        }
        if (configCenterProperties.getLongPollingTimeout() != null) {
            configContext.setLongPollingTimeout(configCenterProperties.getLongPollingTimeout());
        }
        configContext.initContext();

        if (AppMode.LONG_POLLING.name().equals(configContext.getAppMode())) {
            Assert.isTrue(configContext.getPullInterval() >= 300, "拉取时间间隔不得小于300s");
            Assert.isTrue(configContext.getLongPollingTimeout() >= 90, "长轮询超时时间不得小于90s");
        }

        return configContext;
    }

    /**
     * 暴露ConfigService bean
     * @return
     */
    @Bean("hccConfigCenterConfigService")
    @ConditionalOnMissingBean
    public ConfigService configService() {
        return new ConfigService(configContext);
    }

    /**
     * 初始化值注入以及动态字段、监听方法收集
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public ConfigCenterBeanPostProcessor configCenterBeanPostProcessor() {
        return new ConfigCenterBeanPostProcessor(configContext);
    }

    /**
     * 启动配置中心监听
     * @param callBackObjectProvider
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(value = "config.center.enableDynamicPush", havingValue = "true")
    public ConfigCenterInitializerListener configCenterInitializerListener(ObjectProvider<ProcessFailedCallBack> callBackObjectProvider) {
        ProcessFailedCallBack callBack = callBackObjectProvider.getIfUnique(() -> new ProcessFailedCallBack() {});
        return new ConfigCenterInitializerListener(callBack);
    }

    /**
     * 应用启动完成后启动客户端服务或长轮询
     */
    private class ConfigCenterInitializerListener implements ApplicationListener<ApplicationReadyEvent> {

        private final ProcessFailedCallBack callBack;
        private ConfigCenterClientInitializer initializer;

        public ConfigCenterInitializerListener(ProcessFailedCallBack callBack) {
            this.callBack = callBack;
        }

        @Override
        public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
            initializer = new ConfigCenterClientInitializer(configContext, callBack);
            initializer.startClient();
        }

        @PreDestroy
        private void stopClient() {
            if (initializer != null) {
                initializer.stopClient();
            }
        }

    }

}
