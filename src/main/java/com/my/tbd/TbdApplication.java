package com.my.tbd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@ComponentScan
@EnableAutoConfiguration
@EnableCaching
@EnableAspectJAutoProxy(exposeProxy = true)
public class TbdApplication {

	public static void main(String[] args) {
		SpringApplication.run(TbdApplication.class, args);
	}

}
