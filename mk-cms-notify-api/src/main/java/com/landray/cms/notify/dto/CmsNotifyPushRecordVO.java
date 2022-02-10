package com.landray.cms.notify.dto;

import com.landray.cms.notify.constant.MessageStatus;
import com.landray.common.core.data.field.FdAlterTime;
import com.landray.common.core.data.field.FdCreateTime;
import com.landray.common.core.data.field.FdSubject;
import com.landray.common.core.dto.AbstractVO;
import com.landray.sys.auth.data.field.FdAlter;
import com.landray.sys.auth.data.field.FdCreator;
import com.landray.sys.org.dto.SysOrgElementSummaryVO;
import com.landray.sys.org.entity.SysOrgElementSummary;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


/**
 * 消息推送记录 VO
 * @author lisc
 * @version 2021-12-24
 */
@Getter
@Setter
@ToString
@ApiModel(value = "cmsNotifyPushRecordVO", description = "消息推送记录DTO类")
public class CmsNotifyPushRecordVO extends AbstractVO implements FdSubject, FdCreator, FdCreateTime,
        FdAlter, FdAlterTime {

    @ApiModelProperty(value = "来源系统", dataType = "String")
    private String fdAppName;

    @ApiModelProperty(value = "来源模块", dataType = "String")
    private String fdModuleName;

    @ApiModelProperty(value = "消息所属主域模型全类名", dataType = "String")
    private String fdEntityName;

    @ApiModelProperty(value = "消息所属主域模型ID", dataType = "String")
    private String fdEntityId;

    @ApiModelProperty(value = "消息在所属主域模型下的关键字", dataType = "String")
    private String fdEntityKey;

    @ApiModelProperty(value = "原始消息ID", dataType = "String")
    private String fdNotifyId;

    @ApiModelProperty(value = "推送状态: todo-待办; done-已办; remove-删除", dataType = "String")
    private MessageStatus fdStatus;

    @ApiModelProperty(value = "第三方 ID", dataType = "String")
    private String fdThirdPartyId;

    @ApiModelProperty(value = "待办链接", dataType = "String")
    private String fdLink;

    @ApiModelProperty(value = "当前审批节点", dataType = "String")
    private String fdNodeName;

    @ApiModelProperty(value = "参数1", dataType = "String")
    private String fdParameter1;

    @ApiModelProperty(value = "参数2", dataType = "String")
    private String fdParameter2;

    @ApiModelProperty(value = "发送给 ID", dataType = "String")
    private String fdOwnerId;

    @ApiModelProperty(value = "发送给", dataType = "SysOrgElementSummary")
    private SysOrgElementSummary fdOwner;

    @ApiModelProperty(value = "流程发起人", dataType = "SysOrgElementSummary")
    private SysOrgElementSummary fdStartUser;

}
