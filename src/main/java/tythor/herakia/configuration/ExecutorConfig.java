package tythor.herakia.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.SimpleAsyncTaskScheduler;
import tythor.herakia.utility.VirtualThreadUtil;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

@Slf4j
@Configuration
@AutoConfiguration
@AutoConfigureBefore(TaskExecutionAutoConfiguration.class)
@RequiredArgsConstructor
public class ExecutorConfig implements AsyncConfigurer {
    @Bean
    @ConditionalOnMissingBean(name = "bootstrapExecutor")
    public Executor bootstrapExecutor() {
        return VirtualThreadUtil.getVirtualExecutor(Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean(name = "taskExecutor")
    public ExecutorService taskExecutor() {
        return VirtualThreadUtil.getVirtualExecutor("task");
    }

    @Bean
    @ConditionalOnMissingBean(name = "taskScheduler")
    public TaskScheduler taskScheduler() {
        SimpleAsyncTaskScheduler simpleAsyncTaskScheduler = new SimpleAsyncTaskScheduler();
        simpleAsyncTaskScheduler.setTargetTaskExecutor(VirtualThreadUtil.getVirtualExecutor("scheduling"));
        return simpleAsyncTaskScheduler;
    }

    @Override
    public Executor getAsyncExecutor() {
        return taskExecutor();
    }
}
