package tythor.herakia.configuration;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskDecorator;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
import org.springframework.scheduling.concurrent.SimpleAsyncTaskScheduler;
import tythor.herakia.utility.SignatureUtil;
import tythor.herakia.utility.thread.VirtualThreadUtil;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

@Configuration
class ExecutorConfiguration {
    @Bean
    @ConditionalOnMissingBean(name = "bootstrapExecutor")
    public Executor bootstrapExecutor() {
        return VirtualThreadUtil.getVirtualExecutor(SignatureUtil.getMethodName());
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean(name = "taskExecutor")
    public TaskExecutor taskExecutor(ObjectProvider<TaskDecorator> taskDecorator) {
        ExecutorService executorService = VirtualThreadUtil.getVirtualExecutor("task");

        ConcurrentTaskExecutor concurrentTaskExecutor = new ConcurrentTaskExecutor(executorService);
        concurrentTaskExecutor.setTaskDecorator(taskDecorator.getIfUnique());
        return concurrentTaskExecutor;
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean(name = "taskScheduler")
    public TaskScheduler taskScheduler(ObjectProvider<TaskDecorator> taskDecorator) {
        ExecutorService executorService = VirtualThreadUtil.getVirtualExecutor("scheduling");

        SimpleAsyncTaskScheduler simpleAsyncTaskScheduler = new SimpleAsyncTaskScheduler();
        simpleAsyncTaskScheduler.setTargetTaskExecutor(executorService);
        simpleAsyncTaskScheduler.setTaskDecorator(taskDecorator.getIfUnique());
        return simpleAsyncTaskScheduler;
    }
}
