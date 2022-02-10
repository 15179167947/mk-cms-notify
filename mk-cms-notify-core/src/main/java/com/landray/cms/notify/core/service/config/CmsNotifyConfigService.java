package com.landray.cms.notify.core.service.config;

import com.alibaba.fastjson.JSON;
import com.landray.cms.notify.core.entity.config.CmsNotifyConfig;
import com.landray.common.util.TenantUtil;
import com.landray.framework.config.ApplicationConfigApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * 消息通知扩展设置 Service
 */
@Slf4j
@Service
@RestController
@RequestMapping("/api/cms-notify/cmsNotifyConfigService")
@Transactional(rollbackFor = Exception.class)
public class CmsNotifyConfigService {

    @Autowired
    private ApplicationConfigApi applicationConfigApi;

    public void save(CmsNotifyConfig cmsNotifyConfig) {
        log.info("CmsNotifyConfigService.save.CmsNotifyConfig :: " + JSON.toJSONString(cmsNotifyConfig));

        CmsNotifyConfig notifyConfig = this.applicationConfigApi.get(CmsNotifyConfig.class, TenantUtil.getTenantId());
        if (notifyConfig == null) {
            this.applicationConfigApi.save(cmsNotifyConfig, TenantUtil.getTenantId());
        } else {
            notifyConfig.setIsEnabledK2Bpm(cmsNotifyConfig.getIsEnabledK2Bpm());
            notifyConfig.setK2bpmUrl(cmsNotifyConfig.getK2bpmUrl());
            notifyConfig.setBatchAddTodoFriendlyPath(cmsNotifyConfig.getBatchAddTodoFriendlyPath());
            notifyConfig.setAddDonePath(cmsNotifyConfig.getAddDonePath());
            notifyConfig.setDeleteTodoPath(cmsNotifyConfig.getDeleteTodoPath());
            notifyConfig.setK2bpmSysId(cmsNotifyConfig.getK2bpmSysId());
            notifyConfig.setK2bpmToken(cmsNotifyConfig.getK2bpmToken());
            this.applicationConfigApi.save(notifyConfig, TenantUtil.getTenantId());
        }
    }

    public CmsNotifyConfig getCmsNotifyConfig() {
        return this.applicationConfigApi.get(CmsNotifyConfig.class, TenantUtil.getTenantId());
    }

}
