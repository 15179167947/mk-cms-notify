package com.landray.cms.notify.core.entity;

import com.landray.cms.notify.constant.MessageStatus;
import com.landray.common.core.data.field.FdAlterTime;
import com.landray.common.core.data.field.FdCreateTime;
import com.landray.common.core.data.field.FdSubject;
import com.landray.common.core.entity.AbstractEntity;
import com.landray.sys.auth.data.field.FdAlter;
import com.landray.sys.auth.data.field.FdCreator;
import com.landray.sys.org.entity.SysOrgElementSummary;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

/**
 *
 * @author lisc
 * @version 2021-12-24
 */
@Getter
@Setter
@Entity
@Table(indexes = @Index(columnList = "fdStatus"))
public class CmsNotifyPushRecord extends AbstractEntity implements FdSubject, FdCreator, FdCreateTime,
        FdAlter, FdAlterTime {

    /**
     * 来源系统
     */
    @NotBlank
    @Column(length = 100)
    @Length(max = 100)
    private String fdAppName;

    /**
     * 来源模块
     */
    @Column(length = 100)
    @Length(max = 100)
    private String fdModuleName;

    /**
     * 消息所属主域模型全类名
     */
    @NotBlank
    @Column(length = 100)
    @Length(max = 100)
    private String fdEntityName;

    /**
     * 消息所属主域模型ID
     */
    @Column(length = 100)
    @Length(max = 100)
    private String fdEntityId;

    /**
     * 消息在所属主域模型下的关键字
     * <br/>用于区分同一主域模型下的不同类型消息
     * <br/>如某场培训活动, 既要给学员发待办, 也要给讲师发待办, 也要给负责人发待办
     * <br/>当传入了entityId, 强烈建议也传入entityKey
     * <p>
     * <br/>注意!!! 如果需要支持把不同类型消息发送给同一个人, 必须传入不同的entityKey
     * <br/>注意!!! 如果不传入entityKey, 将不能支持把同一主域模型的不同类型消息发送给同一个人
     */
    @Column(length = 100)
    @Length(max = 100)
    private String fdEntityKey;

    /**
     * 原始消息ID
     */
    @Column(length = 100)
    @Length(max = 100)
    private String fdNotifyId;

    /**
     * 消息状态: todo-待办; done-已办; remove-删除
     */
    @Column(length = 10)
    @Convert(converter = MessageStatus.Converter.class)
    private MessageStatus fdStatus;

    /**
     * 第三方 ID
     */
    @Column(length = 100)
    @Length(max = 100)
    private String fdThirdPartyId;

    @Column(length = 200)
    @Length(max = 200)
    private String fdLink;

    /**
     * 当前审批节点
     */
    @Column(length = 100)
    @Length(max = 100)
    private String fdNodeName;

    /**
     * 参数1
     */
    @Column(length = 100)
    @Length(max = 100)
    private String fdParameter1;

    /**
     * 参数2
     */
    @Column(length = 100)
    @Length(max = 100)
    private String fdParameter2;

    /**
     * 发送给 ID
     */
    @Column(length = 36)
    @Length(max = 36)
    private String fdOwnerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fd_start_user_id")
    private SysOrgElementSummary fdStartUserId;

}
