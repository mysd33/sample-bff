package com.example.bff;

import javax.servlet.Filter;
import javax.sql.DataSource;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.AWSXRayRecorderBuilder;
import com.amazonaws.xray.javax.servlet.AWSXRayServletFilter;
import com.amazonaws.xray.plugins.EC2Plugin;
import com.amazonaws.xray.plugins.ECSPlugin;
import com.amazonaws.xray.plugins.EKSPlugin;
import com.amazonaws.xray.spring.aop.BaseAbstractXRayInterceptor;
import com.amazonaws.xray.sql.TracingDataSource;
import com.example.fw.common.httpclient.WebClientXrayFilter;
import com.zaxxer.hikari.HikariDataSource;

@Profile("xray")
@Aspect
@Configuration
public class XRayConfig extends BaseAbstractXRayInterceptor {
	static {
		// サービスプラグインの設定
		AWSXRayRecorderBuilder builder = AWSXRayRecorderBuilder.standard()
				.withPlugin(new EKSPlugin())
				.withPlugin(new ECSPlugin())
				.withPlugin(new EC2Plugin());
		// TODO: サンプリングルール
		// URL ruleFile = WebConfig.class.getResource("/sampling-rules.json");
		// builder.withSamplingStrategy(new LocalizedSamplingStrategy(ruleFile));

		AWSXRay.setGlobalRecorder(builder.build());
	}

	@Override
	@Pointcut("@within(com.amazonaws.xray.spring.aop.XRayEnabled) " + " && execution(* com.example..*.*(..))")
	protected void xrayEnabledClasses() {
	}

	/**
	 * FilterでのAWS X-RayのHttpトレーシング設定
	 * 
	 */
	@Bean
	public Filter tracingFilter() {
		return new AWSXRayServletFilter("sample-bff");
	}

	/**
	 * WebClientでのAWS X-RayのHttpクライアントトレーシング設定
	 * 
	 */
	@Bean
	public WebClientXrayFilter webClientXrayFilter() {
		return new WebClientXrayFilter();
	}
	
	/**
	 * DataSourceプロパティの取得
	 */
	@Bean
	@ConfigurationProperties(prefix = "spring.datasource")
	public DataSourceProperties dataSourceProperties() {
		return new DataSourceProperties();
	}
		
	/**
	 * DataSourceでのAWS X-RayのJDBCトレーシング設定	 
	 */
	@Bean
	public DataSource dataSource(DataSourceProperties dataSourceProperties) {
		return TracingDataSource.decorate(
				DataSourceBuilder.create()
				.type(HikariDataSource.class)
				.driverClassName(dataSourceProperties.getDriverClassName())
				.url(dataSourceProperties.getUrl())
				.username(dataSourceProperties.getUsername())
				.password(dataSourceProperties.getPassword())				
				.build());
	}
}
