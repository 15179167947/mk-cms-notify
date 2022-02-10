package com.landray.cms.notify.core.entity.config;

import lombok.Getter;
import lombok.Setter;

/**
 * 消息通知扩展 Config
 */
@Getter
@Setter
public class CmsNotifyConfig {

    /**
     * 是否开启 K2BPM 待办扩展
     */
    private Boolean isEnabledK2Bpm;

    /**
     * K2BPM Url
     * 例：http://172.253.34.205:8081
     */
    private String k2bpmUrl;

    /**
     * 待办批量提交 Path
     * 例：/psc-backend/operate/batchAddTodoFriendly
     */
    private String batchAddTodoFriendlyPath;

    /**
     * 待办转已办 Path
     * 例：/psc-backend/operate/addDone
     */
    private String addDonePath;

    /**
     * 删除待办 Path
     * 例：/psc-backend/operate/deleteTodo
     */
    private String deleteTodoPath;

    /**
     * K2BPM SysId
     */
    private String k2bpmSysId;

    /**
     * K2BPM Token
     */
    private String k2bpmToken;

}
