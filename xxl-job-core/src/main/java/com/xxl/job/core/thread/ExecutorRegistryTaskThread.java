package com.xxl.job.core.thread;

import com.xxl.job.core.anno.JobTrigger;
import com.xxl.job.core.biz.AdminBiz;
import com.xxl.job.core.biz.model.RegistryParam;
import com.xxl.job.core.biz.model.RegistryTaskParam;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.enums.RegistryConfig;
import com.xxl.job.core.executor.XxlJobExecutor;
import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import com.xxl.job.core.util.GsonTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by xuxueli on 17/3/2.
 */
public class ExecutorRegistryTaskThread {
    private static Logger logger = LoggerFactory.getLogger(ExecutorRegistryTaskThread.class);

    private static ExecutorRegistryTaskThread instance = new ExecutorRegistryTaskThread();

    public static ExecutorRegistryTaskThread getInstance() {
        return instance;
    }

    private Thread registryThread;

    private AtomicBoolean registered = new AtomicBoolean(false);

    public void start(final String appname) {

        // valid
        if (appname == null || appname.trim().length() == 0) {
            logger.warn(">>>>>>>>>>> xxl-job, executor registry config fail, appname is null.");
            return;
        }
        if (XxlJobExecutor.getAdminBizList() == null) {
            logger.warn(">>>>>>>>>>> xxl-job, executor registry config fail, adminAddresses is null.");
            return;
        }

        if (CollectionUtils.isEmpty(XxlJobSpringExecutor.jobTriggers)) {
            logger.warn(">>>>>>>>>>> xxl-job, to be registered task is empty");
            return;
        }

        registryThread = new Thread(() -> {
            // registry
            if (!registered.get()) {
                try {
                    for (AdminBiz adminBiz : XxlJobExecutor.getAdminBizList()) {
                        // todo 测试连通性
                        Iterator<JobTrigger> iterator = XxlJobSpringExecutor.jobTriggers.iterator();
                        while (iterator.hasNext()) {
                            JobTrigger jobTrigger = iterator.next();
                            RegistryTaskParam registryParam = createRegistryTaskParam(jobTrigger, appname);
                            try {
                                ReturnT<String> registryResult = adminBiz.registryTask(registryParam);
                                if (registryResult != null && ReturnT.SUCCESS_CODE == registryResult.getCode()) {
                                    registryResult = ReturnT.SUCCESS;
                                    logger.debug(">>>>>>>>>>> xxl-job registry task success, registryTaskParam:{}, registryResult:{}", GsonTool.toJson(registryParam), registryResult);
                                } else {
                                    logger.info(">>>>>>>>>>> xxl-job registry task fail, registryTaskParam:{}, registryResult:{}", GsonTool.toJson(registryParam), registryResult);
                                }
                            } catch (Exception e) {
                                logger.info(">>>>>>>>>>> xxl-job registry task error, registryTaskParam:{}", GsonTool.toJson(registryParam), e);
                            }
                            iterator.remove();
                        }
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                } finally {
                    registered.set(false);
                }

            }
            logger.info(">>>>>>>>>>> xxl-job, executor registry tasks success.");
        });
        registryThread.setDaemon(true);
        registryThread.setName("xxl-job, executor ExecutorRegistryThread");
        registryThread.start();
    }

    private RegistryTaskParam createRegistryTaskParam(JobTrigger jobTrigger, String jobGroupName) {
        RegistryTaskParam registryTaskParam = new RegistryTaskParam();
        registryTaskParam.setEnabled(jobTrigger.start());
        registryTaskParam.setJobGroupName(jobGroupName);
        registryTaskParam.setJobDesc(jobTrigger.jobDesc());
        registryTaskParam.setAuthor(jobTrigger.author());
        registryTaskParam.setScheduleType(jobTrigger.scheduleType().name());
        registryTaskParam.setScheduleConf(jobTrigger.scheduleConf());
        registryTaskParam.setGlueType(jobTrigger.mode().name());
        registryTaskParam.setExecutorHandler(jobTrigger.jobHandler());
        registryTaskParam.setExecutorParam(jobTrigger.jobParam());
        registryTaskParam.setExecutorRouteStrategy(jobTrigger.routeStrategy().name());
        registryTaskParam.setMisfireStrategy(jobTrigger.misFireStrategy().name());
        registryTaskParam.setExecutorBlockStrategy(jobTrigger.blockStrategy().name());
        registryTaskParam.setExecutorTimeout(jobTrigger.executorTimeout());
        registryTaskParam.setExecutorFailRetryCount(jobTrigger.retryCount());

        return registryTaskParam;
    }
}