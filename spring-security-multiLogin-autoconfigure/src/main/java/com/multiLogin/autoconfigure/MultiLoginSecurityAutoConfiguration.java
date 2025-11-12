package com.multiLogin.autoconfigure;

import com.multiLogin.autoconfigure.config.MultiLoginSecurity;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * @author wan
 */
@AutoConfiguration
@Import(MultiLoginSecurity.class)
public class MultiLoginSecurityAutoConfiguration {
}
