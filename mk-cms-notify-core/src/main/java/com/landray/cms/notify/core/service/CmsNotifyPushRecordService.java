package com.landray.cms.notify.core.service;

import com.alibaba.fastjson.JSONObject;
import com.landray.cms.notify.api.ICmsNotifyPushRecordApi;
import com.landray.cms.notify.constant.MessageStatus;
import com.landray.cms.notify.core.entity.CmsNotifyPushRecord;
import com.landray.cms.notify.core.repository.ICmsNotifyPushRecordRepository;
import com.landray.cms.notify.dto.CmsNotifyPushRecordVO;
import com.landray.common.core.constant.QueryConstant;
import com.landray.common.core.dto.QueryRequest;
import com.landray.common.core.service.AbstractServiceImpl;
import com.landray.common.util.IDGenerator;
import com.landray.sys.notify.api.ISysNotifyDoneApi;
import com.landray.sys.notify.api.ISysNotifyTodoApi;
import com.landray.sys.notify.dto.NotifyProviderContext;
import com.landray.sys.notify.support.util.NotifyContextRequestHelper;
import com.landray.sys.org.api.ISysOrgElementSummaryEntityApi;
import com.landray.sys.org.dto.SysOrgElementSummaryVO;
import com.landray.sys.org.dto.SysOrgPersonVO;
import com.landray.sys.org.entity.SysOrgElementSummary;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * 消息推送记录
 * @author lisc
 * @version 2021-12-24
 */
@Slf4j
@Service
@RestController
@RequestMapping("/api/cms-notify/cmsNotifyPushRecord")
@Transactional(readOnly = true, rollbackFor = {Exception.class})
public class CmsNotifyPushRecordService extends
        AbstractServiceImpl<ICmsNotifyPushRecordRepository, CmsNotifyPushRecord, CmsNotifyPushRecordVO> implements
        ICmsNotifyPushRecordApi {

    @Autowired
    private CmsNotifyPushRecordService cmsNotifyPushRecordService;
    @Autowired
    private ISysOrgElementSummaryEntityApi sysOrgElementSummaryEntityApi;

    /**
     * 根据 K2BPM 创建推送实例
     * @param context
     * @param messageStatus
     */
    public List<CmsNotifyPushRecordVO> newInstanceByK2BPM(NotifyProviderContext context, MessageStatus messageStatus) {
        List<CmsNotifyPushRecordVO> pushRecordVOs = new ArrayList<CmsNotifyPushRecordVO>();

        if (MessageStatus.TODO.equals(messageStatus)) {
            List<SysOrgPersonVO> targets = context.getTargets();

            for (SysOrgPersonVO target : targets) {
                CmsNotifyPushRecordVO pushRecord = new CmsNotifyPushRecordVO();
                pushRecord.setFdId(IDGenerator.generateID());
                pushRecord.setFdSubject(context.getSubject());
                pushRecord.setFdStatus(MessageStatus.TODO);
                pushRecord.setFdAppName(context.getAppName());
                pushRecord.setFdModuleName(context.getModuleName());
                pushRecord.setFdNodeName(context.getNodeName());
                pushRecord.setFdEntityId(context.getEntityId());
                pushRecord.setFdEntityName(context.getEntityName());
                pushRecord.setFdEntityKey(context.getEntityKey());
                pushRecord.setFdNotifyId(context.getNotifyId());
                pushRecord.setFdLink(context.getLink());
                pushRecord.setFdParameter1(context.getParameter1());
                pushRecord.setFdParameter2(context.getParameter2());
                pushRecord.setFdOwnerId(target.getFdId());

                if (context.getCreator() != null) {
                    pushRecord.setFdStartUser(
                            sysOrgElementSummaryEntityApi.getByFdId(context.getCreator().getFdId()));
                }

                pushRecordVOs.add(pushRecord);
            }
            log.info("创建待办推送记录数据：" + JSONObject.toJSONString(pushRecordVOs));
        } else if (MessageStatus.DONE.equals(messageStatus) || MessageStatus.REMOVE.equals(messageStatus)) {
            QueryRequest request = NotifyContextRequestHelper.buildQueryRequest(context);
            request.addCondition("fdStatus", QueryConstant.Operator.neq, MessageStatus.REMOVE);
            List<CmsNotifyPushRecordVO> pushRecords = cmsNotifyPushRecordService.findAll(request).getContent();
            log.info("根据条件查询待办记录数，查询参数：" + JSONObject.toJSONString(request.getConditions()) + "; " +
                    "返回记录：" + JSONObject.toJSONString(pushRecords) + "; 总记录数：" + pushRecords.size());

            for (CmsNotifyPushRecordVO pushRecord : pushRecords) {
                pushRecord.setFdStatus(messageStatus);
                pushRecord.setFdAlterTime(new Date());
                pushRecordVOs.add(pushRecord);
            }
        }

        return pushRecordVOs;
    }
}
