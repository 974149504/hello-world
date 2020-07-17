package udpandObserve;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @ClassName SpringUtil
 * @Author ShiHaiLin
 * @Date 2020/7/14 10:28
 * @Description 根据上下文环境获取实例bean
 */

@Slf4j
@Component
public class SpringUtil implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringUtil.applicationContext = applicationContext;
    }

    //获取上下文
    public ApplicationContext getApplicationContext(){
        return applicationContext;
    }

    public static <T> T getBean(Class<T> name) throws BeansException{
        return (T) applicationContext.getBean(name);
    }
}



package com.allcam.adapter.gws;


        import com.allcam.adapter.gws.service.udp.UdpReceive;
        import com.allcam.adapter.gws.service.util.SpringUtil;
        import com.allcam.adapter.gws.system.AdapterProperties;
        import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
        import org.slf4j.Logger;
        import org.slf4j.LoggerFactory;
        import org.springframework.boot.CommandLineRunner;
        import org.springframework.boot.SpringApplication;
        import org.springframework.boot.autoconfigure.SpringBootApplication;
        import org.springframework.boot.context.properties.EnableConfigurationProperties;
        import org.springframework.scheduling.annotation.EnableScheduling;
        import org.springframework.transaction.annotation.EnableTransactionManagement;


public class GwsServiceApplication implements CommandLineRunner
{
    private static final Logger LOG = LoggerFactory.getLogger(GwsServiceApplication.class);

    @Override
    public void run(String... args)
    {
        LOG.info(">>>>>>>> Application Start Complete! <<<<<<<<");
    }

    public static void main(String[] args)
    {
        SpringApplication.run(GwsServiceApplication.class, args);
        //线程跟随Spring主项目启动
        new Thread(SpringUtil.getBean(UdpReceive.class)).start();
    }

}