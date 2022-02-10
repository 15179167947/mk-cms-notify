package com.landray.cms.notify.core.extension;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.landray.cms.notify.constant.MessageStatus;
import com.landray.cms.notify.core.entity.config.CmsNotifyConfig;
import com.landray.cms.notify.core.service.CmsNotifyPushRecordService;
import com.landray.cms.notify.core.service.config.CmsNotifyConfigService;
import com.landray.cms.notify.core.utils.HttpClientUtil;
import com.landray.cms.notify.dto.CmsNotifyPushRecordVO;
import com.landray.common.core.util.TransactionUtil;
import com.landray.common.i18n.ResourceUtil;
import com.landray.framework.plugin.Plugin;
import com.landray.sys.notify.annotation.NotifyProvider;
import com.landray.sys.notify.constant.ProviderConstant;
import com.landray.sys.notify.constant.TodoType;
import com.landray.sys.notify.dto.NotifyProviderContext;
import com.landray.sys.notify.spi.ISysNotifyProvider;
import com.landray.sys.notify.support.util.ReplayException;
import com.landray.sys.org.api.ISysOrgPersonComponentApi;
import com.landray.sys.org.dto.PersonAccountVO;
import com.landray.sys.org.dto.SysOrgElementSummaryVO;
import com.landray.sys.org.entity.SysOrgElementSummary;
import com.landray.tic.base.api.ITicSystemApi;
import com.landray.tic.base.dto.TicSystemDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 消息通知扩展
 * @author lisc
 * @version 2021-12-24
 */
@Slf4j
@Service
@RestController
@RequestMapping("/api/cms-notify/cmsNotifyK2bpm")
@NotifyProvider(id = "k2bpm", messageKey = "cms-notify:notifyType.k2bpm",
        support = ProviderConstant.SUPPORT_SEND | ProviderConstant.SUPPORT_DONE
                | ProviderConstant.SUPPORT_REMOVETODO | ProviderConstant.SUPPORT_REMOVEDONE
                | ProviderConstant.SUPPORT_REMOVEALL, extendBy = "todo",
        informedMethod = true, order = 10)
public class CmsNotifyK2bpmProvider implements ISysNotifyProvider, ApplicationListener<ApplicationReadyEvent> {

    private String SYSTEM_CODE = "K2BPM";

    @Autowired
    private CmsNotifyPushRecordService cmsNotifyPushRecordService;
    @Autowired
    private ISysOrgPersonComponentApi sysOrgPersonComponentApi;
    @Autowired
    private CmsNotifyConfigService cmsNotifyConfigService;

    @Override
    public boolean isEnabled() {
        CmsNotifyConfig cmsNotifyConfig = cmsNotifyConfigService.getCmsNotifyConfig();

        return cmsNotifyConfig.getIsEnabledK2Bpm();
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.debug("开始，激活 K2BPM 扩展点 CmsNotifyK2bpmProvider......");
        Plugin.saveExtensionEnabled(NotifyProvider.class, "k2bpm", true);
        log.debug("结束，激活 K2BPM 扩展点 CmsNotifyK2bpmProvider......");
    }

    /**
     * 发送待办
     * @param context
     * @throws Exception
     */
    @Override
    public void send(NotifyProviderContext context) throws Exception {

        if (!TodoType.TODO.equals(context.getTodoType())) {
            log.info("当前通知类型为非待办，跳过推送操作！" + JSONObject.toJSONString(context));
            return ;
        } else {
            log.debug("开始发送待办：" + JSONObject.toJSONString(context));
        }

        CmsNotifyConfig cmsNotifyConfig = cmsNotifyConfigService.getCmsNotifyConfig();
        String url = cmsNotifyConfig.getK2bpmUrl() + cmsNotifyConfig.getBatchAddTodoFriendlyPath();

        List<CmsNotifyPushRecordVO> pushRecordVOs = cmsNotifyPushRecordService
                .newInstanceByK2BPM(context, MessageStatus.TODO);

        JSONArray requestParams = new JSONArray();
        for (CmsNotifyPushRecordVO pushRecordVO : pushRecordVOs) {

            String ownerId = pushRecordVO.getFdOwnerId();
            if (ownerId == null)
                continue;

            Map<String, Object> pushData = new HashMap<String, Object>();
            pushData.put("sysId", cmsNotifyConfig.getK2bpmSysId());  // 系统 ID
            pushData.put("token", cmsNotifyConfig.getK2bpmToken());  // 每个系统对应的Token，暂定字段，目前尚未用起来
            pushData.put("sn", pushRecordVO.getFdId());  // 待办唯一 ID
            pushData.put("procName", pushRecordVO.getFdModuleName());  // 流程名称
            pushData.put("actName", pushRecordVO.getFdNodeName());  // 当前审批环节
            pushData.put("title", pushRecordVO.getFdSubject());  // 流程标题

            PersonAccountVO personVo = sysOrgPersonComponentApi.getPersonById(ownerId);
            if (personVo == null)
                continue;

            pushData.put("sendTo", personVo.getFdLoginName());  // 当前处理人

            SysOrgElementSummary startUser = pushRecordVO.getFdStartUser();
            if (startUser != null)
                pushData.put("startUser", startUser.getFdName());  // 流程发起人

            if (startUser.getFdParent() != null)
                pushData.put("startDept", startUser.getFdParent().getFdName());  // 流程发起部门

            pushData.put("url", pushRecordVO.getFdLink());  // 待办跳转URL地址

            requestParams.add(pushData);
        }

        String requestParamsStr = requestParams.toString();

        // 发送待办
        JSONObject resultJson = sendTodoNotify(url, requestParamsStr);
        // 处理返回数据
        addTodoRecord(pushRecordVOs, resultJson);

    }

    /**
     * 置为已办
     * @param context
     * @throws Exception
     */
    @Override
    public void done(NotifyProviderContext context) throws Exception {
        log.debug("开始发送已办：" + JSONObject.toJSONString(context));

        CmsNotifyConfig cmsNotifyConfig = cmsNotifyConfigService.getCmsNotifyConfig();
        String url = cmsNotifyConfig.getK2bpmUrl() + cmsNotifyConfig.getAddDonePath();

        List<CmsNotifyPushRecordVO> pushRecordVOs = cmsNotifyPushRecordService
                .newInstanceByK2BPM(context, MessageStatus.DONE);

        if (CollectionUtils.isEmpty(pushRecordVOs)) {
            log.warn(SYSTEM_CODE + " 未查询到相关推送记录，不执行此次置为已办操作！NotifyProviderContext 参数："
                    + JSONObject.toJSONString(context));
            return ;
        }

        // 获取第三方 ID 集合
        List<String> thirdPartyIdList = pushRecordVOs.stream().map(CmsNotifyPushRecordVO::getFdThirdPartyId)
                .collect(Collectors.toList());

        Map<String, String> requestParam = new HashMap<String, String>();
        requestParam.put("sysId", cmsNotifyConfig.getK2bpmSysId());  // 系统 ID
        requestParam.put("token", cmsNotifyConfig.getK2bpmToken());  // 每个系统对应的Token，暂定字段，目前尚未用起来
        requestParam.put("todoId", StringUtils.join(thirdPartyIdList, ","));  // 待办ID，支持多个，使用“,” 进行分割
        requestParam.put("doneUrl", "");  // 	已办URL

        // 发送待办
        JSONObject resultJson = sendUpdateNotify(url, requestParam);
        // 添加已办推送记录
        updateRecordToResult(pushRecordVOs, resultJson, MessageStatus.DONE);
    }

    /**
     * 将待办中的所有人设置为已办
     * @param context
     * @throws Exception
     */
    @Override
    public void removeTodo(NotifyProviderContext context) throws Exception {
        this.removeNotify(context);
    }

    /**
     * 批量删除
     * @param context
     * @throws Exception
     */
    @Override
    public void removeAll(NotifyProviderContext context) throws Exception {
        this.removeNotify(context);
    }

    /**
     * 发送通知（删除）
     * @param context
     * @return
     * @exception ReplayException
     */
    private void removeNotify(NotifyProviderContext context) throws Exception {
        log.debug("开始发送删除待办/已办：" + JSONObject.toJSONString(context));

        CmsNotifyConfig cmsNotifyConfig = cmsNotifyConfigService.getCmsNotifyConfig();
        String url = cmsNotifyConfig.getK2bpmUrl() + cmsNotifyConfig.getDeleteTodoPath();

        List<CmsNotifyPushRecordVO> pushRecordVOs = cmsNotifyPushRecordService
                .newInstanceByK2BPM(context, MessageStatus.REMOVE);

        if (CollectionUtils.isEmpty(pushRecordVOs)) {
            log.warn(SYSTEM_CODE + " 未查询到相关待办/已办记录，不执行此次删除操作！NotifyProviderContext 参数："
                    + JSONObject.toJSONString(context));
            return ;
        }

        List<String> thirdPartyIdList = new ArrayList<String>();
        for (CmsNotifyPushRecordVO pushRecordVO : pushRecordVOs) {
            thirdPartyIdList.add(pushRecordVO.getFdThirdPartyId());
        }

        for (CmsNotifyPushRecordVO pushRecordVO : pushRecordVOs) {
            Map<String, String> requestParam = new HashMap<String, String>();
            requestParam.put("sysId", cmsNotifyConfig.getK2bpmSysId());  // 系统 ID
            requestParam.put("todoId", pushRecordVO.getFdThirdPartyId());  // 待办ID，支持多个，使用“,” 进行分割

            // 发送删除待办操作
            JSONObject resultJson = sendUpdateNotify(url, requestParam);
            // 添加删除推送记录
            updateRecordToResult(pushRecordVO, resultJson, MessageStatus.REMOVE);
        }
    }

    /**
     * 发送通知（待办）
     * @param url
     * @param requestParams
     * @return
     * @exception ReplayException
     */
    private JSONObject sendTodoNotify(String url, String requestParams) {
        StringBuilder pushMsg = new StringBuilder();
        JSONObject resultJson = null;
        try {
            pushMsg.append("接口URL: ").append(url).append("推送参数: ").append(requestParams);

            String response = HttpClientUtil.sendPostRequest(url, requestParams);
            pushMsg.append("返回数据: ").append(response);

            resultJson = JSONObject.parseObject(response);
            if (resultJson == null) {
                pushMsg.insert(0, SYSTEM_CODE + " 待办推送异常! ");

                throw new ReplayException(pushMsg.toString());
            }

            log.debug("待办推送成功！" + pushMsg.toString());
        } catch (Exception e) {
            pushMsg.insert(0, SYSTEM_CODE + " 待办推送异常! ");

            throw new ReplayException(pushMsg.toString(), e);
        }
        return resultJson;
    }

    /**
     * 添加待办推送记录
     * @param pushRecordVOS
     * @param resultJson
     * @throws Exception,ReplayException
     */
    private void addTodoRecord(List<CmsNotifyPushRecordVO> pushRecordVOS, JSONObject resultJson) throws Exception {

        if (resultJson.getIntValue("code") == 1) {  // 成功
            JSONArray data = resultJson.getJSONArray("data");

            TransactionStatus txStatus = null;
            for (int i = 0; i < data.size(); i++) {
                if (txStatus == null) {
                    txStatus = TransactionUtil.beginNewTransaction();
                }

                CmsNotifyPushRecordVO failObj = null;
                try {
                    JSONObject resultData = data.getJSONObject(i);
                    String sn = resultData.getString("sn");

                    for (CmsNotifyPushRecordVO pushRecordVO : pushRecordVOS) {
                        if (pushRecordVO.getFdId().equals(sn)) {
                            failObj = pushRecordVO;

                            pushRecordVO.setFdThirdPartyId(resultData.getString("id"));
                            cmsNotifyPushRecordService.add(pushRecordVO);

                            log.debug("待办推送记录保存成功！保存数据为：" + JSONObject.toJSONString(pushRecordVO));
                            TransactionUtil.commit(txStatus);
                            txStatus = null;

                            break;
                        }
                    }
                } catch (Exception e) {
                    log.error(SYSTEM_CODE + " 待办推送记录保存失败！失败数据 JSON 格式为: " + JSONObject.toJSONString(failObj), e);

                    if (txStatus != null) {
                        TransactionUtil.rollback(txStatus);
                        txStatus = null;
                    }

                }
            }
        } else {
            throw new Exception(resultJson.getString("message"));
        }
    }

    /**
     * 发送通知（置为已办/删除）
     * @param url
     * @param requestParam
     * @return
     * @exception ReplayException
     */
    private JSONObject sendUpdateNotify(String url, Map<String, String> requestParam) {
        StringBuilder pushMsg = new StringBuilder();
        JSONObject resultJson = null;
        try {
            pushMsg.append("接口URL: ").append(url).append("推送参数: ").append(JSONObject.toJSONString(requestParam));
            String response = HttpClientUtil.sendPostRequest(url, requestParam);
            pushMsg.append("返回数据: ").append(response);

            resultJson = JSONObject.parseObject(response);
            if (resultJson == null) {
                pushMsg.insert(0, SYSTEM_CODE + " 置为已办接口调用失败! ");

                throw new ReplayException(pushMsg.toString());
            }
            log.debug("置为已办/删除操作成功！" + pushMsg.toString());
        } catch (Exception e) {
            pushMsg.insert(0, SYSTEM_CODE + " 置为已办接口调用失败! ");

            throw new ReplayException(pushMsg.toString(), e);
        }
        return resultJson;
    }

    /**
     * 添加待办推送记录
     * @param pushRecordVO
     * @param resultJson
     * @throws Exception,ReplayException
     */
    private void updateRecordToResult(CmsNotifyPushRecordVO pushRecordVO,
          JSONObject resultJson, MessageStatus status) throws Exception {

        if (resultJson.getIntValue("code") == 1) {  // 成功
            TransactionStatus txStatus = null;
            CmsNotifyPushRecordVO failObj = null;

            try {
                if (txStatus == null)
                    txStatus = TransactionUtil.beginNewTransaction();

                failObj = pushRecordVO;

                pushRecordVO.setFdStatus(status);
                cmsNotifyPushRecordService.update(pushRecordVO);

                log.debug("置为已办/删除推送记录保存成功！保存数据为：" + JSONObject.toJSONString(pushRecordVO));
                TransactionUtil.commit(txStatus);
                txStatus = null;
            } catch (Exception e) {
                if (txStatus != null) {
                    TransactionUtil.rollback(txStatus);
                    txStatus = null;
                }

                log.error(SYSTEM_CODE + "（" + status + "）推送记录保存失败！失败数据 JSON 格式为: " + JSONObject.toJSONString(failObj), e);
            }
        } else {
            throw new Exception(resultJson.getString("message"));
        }
    }

    /**
     * 更新推送记录已办/删除状态
     * @param pushRecordVOs
     * @param resultJson
     * @throws Exception,ReplayException
     */
    private void updateRecordToResult(List<CmsNotifyPushRecordVO> pushRecordVOs,
          JSONObject resultJson, MessageStatus status) throws Exception {

        if (resultJson.getIntValue("code") == 1) {  // 成功
            TransactionStatus txStatus = null;
            CmsNotifyPushRecordVO failObj = null;

            for (CmsNotifyPushRecordVO pushRecordVO : pushRecordVOs) {
                try {
                    if (txStatus == null)
                        txStatus = TransactionUtil.beginNewTransaction();

                    failObj = pushRecordVO;

                    pushRecordVO.setFdStatus(status);
                    cmsNotifyPushRecordService.update(pushRecordVO);

                    log.debug("置为已办/删除推送记录保存成功！保存数据为：" + JSONObject.toJSONString(pushRecordVO));
                    TransactionUtil.commit(txStatus);
                    txStatus = null;
                } catch (Exception e) {
                    if (txStatus != null) {
                        TransactionUtil.rollback(txStatus);
                        txStatus = null;
                    }

                    log.error(SYSTEM_CODE + "（" + status + "）推送记录保存失败！失败数据 JSON 格式为: " + JSONObject.toJSONString(failObj), e);
                }
            }
        } else {
            throw new Exception(resultJson.getString("message"));
        }
    }

}
