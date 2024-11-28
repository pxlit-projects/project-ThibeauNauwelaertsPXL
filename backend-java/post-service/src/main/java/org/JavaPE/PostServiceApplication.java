package org.JavaPE;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * PostServiceApplication
 *
 */
@SpringBootApplication
@EnableFeignClients
public class PostServiceApplication
{
    public static void main( String[] args )
    {
        SpringApplication.run(PostServiceApplication.class, args);
    }
}
