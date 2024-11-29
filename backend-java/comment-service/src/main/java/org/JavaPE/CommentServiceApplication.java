package org.JavaPE;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Hello world!
 *
 */
@SpringBootApplication
public class CommentServiceApplication
{
    public static void main( String[] args )
    {
        SpringApplication.run(CommentServiceApplication.class, args);
    }
}