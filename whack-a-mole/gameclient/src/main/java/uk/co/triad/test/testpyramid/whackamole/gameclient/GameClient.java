package uk.co.triad.test.testpyramid.whackamole.gameclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;

@SpringBootApplication
public class GameClient
{
  private Logger logger = LoggerFactory.getLogger(GameClient.class);

  public static void main(String[] args)
  {
    SpringApplication.run(GameClient.class, args);
  }

  @Bean
  public CommandLineRunner commandLineRunner(ApplicationContext ctx)
  {
    return args -> {
      logger.info("Let's inspect the beans provided by Spring Boot:");
      var beanNames = ctx.getBeanDefinitionNames();
      Arrays.sort(beanNames);
      for (String beanName : beanNames)
      {
        logger.info(beanName);
      }
    };
  }
}
