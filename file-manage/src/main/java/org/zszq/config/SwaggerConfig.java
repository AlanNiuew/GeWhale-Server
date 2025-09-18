package org.zszq.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

/**
 * Swagger API文档配置类
 * 配置OpenAPI 3.0规范的API文档展示
 * 
 * @author GeWhale Team
 * @since 1.0.0
 */
@Configuration
public class SwaggerConfig {

    @Value("${server.port:8088}")
    private String serverPort;

    /**
     * 配置OpenAPI信息
     * 
     * @return OpenAPI配置实例
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(getApiInfo())
                .servers(getServers())
                .components(getComponents());
    }

    /**
     * API基本信息配置
     * 
     * @return API信息
     */
    private Info getApiInfo() {
        return new Info()
                .title("GeWhale文件管理服务API")
                .description("GeWhale音乐平台文件管理服务接口文档。提供文件上传、下载、预览等功能，基于MinIO对象存储。")
                .version("1.0.0")
                .contact(new Contact()
                        .name("GeWhale Team")
                        .email("support@gewhale.com")
                        .url("https://github.com/AlanNiew/GeWhale-Server"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }

    /**
     * 服务器配置
     * 
     * @return 服务器列表
     */
    private List<Server> getServers() {
        Server localServer = new Server()
                .url("http://localhost:" + serverPort)
                .description("本地开发环境");
        
        Server devServer = new Server()
                .url("https://dev-file-api.gewhale.com")
                .description("开发环境");
        
        return Arrays.asList(localServer, devServer);
    }

    /**
     * 安全组件配置
     * 
     * @return 组件配置
     */
    private Components getComponents() {
        return new Components()
                .addSecuritySchemes("apiKey", new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .name("X-API-Key")
                        .description("API密钥认证")
                );
    }

    /**
     * 安全要求配置
     * 
     * @return 安全要求
     */
    private SecurityRequirement getSecurityRequirement() {
        return new SecurityRequirement().addList("apiKey");
    }
}