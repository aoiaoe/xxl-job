package com.xxl.job.admin.service.impl;

import com.fasterxml.jackson.databind.util.BeanUtil;
import com.xxl.job.admin.core.model.XxlJobGroup;
import com.xxl.job.admin.core.model.XxlJobInfo;
import com.xxl.job.admin.core.thread.JobCompleteHelper;
import com.xxl.job.admin.core.thread.JobRegistryHelper;
import com.xxl.job.admin.core.util.I18nUtil;
import com.xxl.job.admin.dao.XxlJobGroupDao;
import com.xxl.job.admin.dao.XxlJobInfoDao;
import com.xxl.job.admin.service.XxlJobService;
import com.xxl.job.core.biz.AdminBiz;
import com.xxl.job.core.biz.model.HandleCallbackParam;
import com.xxl.job.core.biz.model.RegistryParam;
import com.xxl.job.core.biz.model.RegistryTaskParam;
import com.xxl.job.core.biz.model.ReturnT;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author xuxueli 2017-07-27 21:54:20
 */
@Service
public class AdminBizImpl implements AdminBiz {

    @Resource
    private XxlJobService xxlJobService;
    @Resource
    private XxlJobGroupDao xxlJobGroupDao;
    @Resource
    private XxlJobInfoDao xxlJobInfoDao;

    @Override
    public ReturnT<String> callback(List<HandleCallbackParam> callbackParamList) {
        return JobCompleteHelper.getInstance().callback(callbackParamList);
    }

    @Override
    public ReturnT<String> registry(RegistryParam registryParam) {
        return JobRegistryHelper.getInstance().registry(registryParam);
    }

    @Override
    public ReturnT<String> registryRemove(RegistryParam registryParam) {
        return JobRegistryHelper.getInstance().registryRemove(registryParam);
    }

    @Override
    public ReturnT<String> registryTask(RegistryTaskParam param) {
        XxlJobGroup group = xxlJobGroupDao.findByAppName(param.getJobGroupName());
        if (group == null) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "执行器不存在，请先在控制台新增执行器" );
        }
        int count = xxlJobInfoDao.countExist(group.getId(), param.getJobDesc());
        if (count > 0) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "执行器：" + param.getJobGroupName() + "中已经存在该任务！" );
        }
        XxlJobInfo jobInfo = new XxlJobInfo();
        BeanUtils.copyProperties(param, jobInfo);
        jobInfo.setJobGroup(group.getId());
        jobInfo.setTriggerStatus(param.isEnabled() ? 1 : 0);
        return xxlJobService.add(jobInfo);
    }

}
