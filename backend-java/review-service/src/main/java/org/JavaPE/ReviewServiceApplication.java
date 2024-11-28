package org.JavaPE;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Hello world!
 *
 */
@SpringBootApplication
public class ReviewServiceApplication
{
    public static void main( String[] args )
    {
        SpringApplication.run(ReviewServiceApplication.class, args);
    }
}