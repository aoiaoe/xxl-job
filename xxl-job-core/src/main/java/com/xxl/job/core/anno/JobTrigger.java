package com.xxl.job.core.anno;

import com.xxl.job.core.enums.ExecutorBlockStrategyEnum;
import com.xxl.job.core.enums.MisfireStrategy;
import com.xxl.job.core.enums.RouteStrategy;
import com.xxl.job.core.enums.ScheduleType;
import com.xxl.job.core.glue.GlueTypeEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface JobTrigger {

    /**
     * 注册后是否启动， 默认true
     */
    boolean start() default true;
    /**
     * 负责人, 必填
     */
    String author();
    /**
     * 任务描述，必填
     */
    String jobDesc();

    /**
     * 调度类型, 默认cron表达式
     */
    ScheduleType scheduleType() default ScheduleType.CRON;

    /**
     * 调度配置，根据scheduleType配置
     * CRON就配置cron表达式，
     * FIX_RATE则配置速率，整数,单位为秒
     */
    String scheduleConf() default "";

    /**
     * 暂时只支持BEAN
     */
    GlueTypeEnum mode() default GlueTypeEnum.BEAN;

    /**
     * 处理器名称
     */
    String jobHandler();

    /**
     * 任务执行参数
     */
    String jobParam() default "";

    /**
     * 路由策略, 默认FIRST
     */
    RouteStrategy routeStrategy() default RouteStrategy.FIRST;

    /**
     * 调度过期策略， 默认忽略
     */
    MisfireStrategy misFireStrategy() default MisfireStrategy.DO_NOTHING;

    /**
     * 阻塞处理策略, 默认单机串行
     */
    ExecutorBlockStrategyEnum blockStrategy() default ExecutorBlockStrategyEnum.SERIAL_EXECUTION;

    /**
     * 任务超时时间
     */
    int executorTimeout() default 0;

    /**
     * 失败重试次数
     */
    int retryCount() default 0;
}
