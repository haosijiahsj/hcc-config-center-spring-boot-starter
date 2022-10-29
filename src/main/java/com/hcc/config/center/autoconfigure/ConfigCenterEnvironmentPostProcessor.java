package com.hcc.config.center.autoconfigure;

import com.hcc.config.center.client.context.ConfigContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * 配置中心值加入环境中
 *
 * @author hushengjun
 * @date 2022/10/29
 */
public class ConfigCenterEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String appCode = environment.getRequiredProperty("config.center.appCode");
        String secretKey = environment.getRequiredProperty("config.center.secretKey");
        String serverUrl = environment.getRequiredProperty("config.center.serverUrl");

        ConfigContext configContext = new ConfigContext();
        configContext.setAppCode(appCode);
        configContext.setSecretKey(secretKey);
        configContext.setServerUrl(serverUrl);

        configContext.initContext();

        Map<String, String> keyValueMap = configContext.getConfigKeyValueMap();
        if (CollectionUtils.isEmpty(keyValueMap)) {
            return;
        }

        environment.getPropertySources().addFirst(
                new MapPropertySource("hccConfigCenterPropertySource",
                        keyValueMap.entrySet()
                                .stream()
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
                )
        );
    }

}
