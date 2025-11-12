package com.multiLongin.core.properties;

import com.multiLongin.core.exception.MultiLoginException;
import com.multiLongin.core.properties.config.GlobalConfig;
import com.multiLongin.core.properties.config.LoginMethodConfig;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wan
 * 主配置类
 */
@ConfigurationProperties(prefix = "multi-login")
@Data
public class MultiLoginProperties {
    private boolean enabled = false;
    private GlobalConfig global = new GlobalConfig();
    private List<LoginMethodConfig> methods = new ArrayList<>();

    // 如果配置了 processUrl 则直接使用；如果未配置但有 name，则用 "/login/" + name 生成
    @PostConstruct
    public void determineProcessUrl() {
        for (LoginMethodConfig method : methods) {
            String name = method.getName();
            String processUrl = method.getProcessUrl();
            // 若processUrl未配置（为空），且name存在，则动态生成
            if (processUrl == null || processUrl.trim().isEmpty()) {
                if (name != null && !name.trim().isEmpty()) {
                    method.setProcessUrl("/login/" + name);
                } else {
                    // 如果两者都未配置，报错
                    throw new MultiLoginException("The request path for login is not configured");
                }
            }
            // 若processUrl已配置，则不做处理，直接使用配置值
        }
    }

    // 优先使用显式配置的 paramName（需包含 principalParamName + credentialParamName 所有元素）；
    // 未配置则自动用后两者合并作为 paramName
    @PostConstruct
    public void initParamName() {
        for (LoginMethodConfig method : methods) {
            List<String> paramName = method.getParamName();
            List<String> principalParamName = method.getPrincipalParamName();
            List<String> credentialParamName = method.getCredentialParamName();
            // 未配置paramName：自动合并principal和credential的参数名（去重）
            if (paramName == null || paramName.isEmpty()) {
                List<String> mergedParams = new ArrayList<>(principalParamName);
                // 添加credential中不存在于principal的参数（避免重复）
                for (String credParam : credentialParamName) {
                    if (!mergedParams.contains(credParam)) {
                        mergedParams.add(credParam);
                    }
                }
                method.setParamName(mergedParams);
            } else {
                // 已配置paramName：校验是否包含principal和credential的所有参数
                List<String> missingParams = getParams(principalParamName, paramName, credentialParamName);
                // 缺失则抛异常提示
                if (!missingParams.isEmpty()) {
                    throw new MultiLoginException("paramName must contain all the parameters of principalParamName and credentialParamName");
                }
            }
        }
    }

    private static List<String> getParams(List<String> principalParamName, List<String> paramName, List<String> credentialParamName) {
        List<String> missingParams = new ArrayList<>();
        // 校验principal的参数是否都在paramName中
        for (String principalParam : principalParamName) {
            if (!paramName.contains(principalParam)) {
                missingParams.add(principalParam);
            }
        }
        // 校验credential的参数是否都在paramName中
        for (String credParam : credentialParamName) {
            if (!paramName.contains(credParam)) {
                missingParams.add(credParam);
            }
        }
        return missingParams;
    }
}
