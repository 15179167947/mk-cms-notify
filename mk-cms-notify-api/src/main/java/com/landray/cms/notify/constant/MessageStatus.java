package com.landray.cms.notify.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.landray.common.core.constant.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 接口调用状态
 * @author lisc
 * @version 2021-12-24
 */
@AllArgsConstructor
public enum MessageStatus implements IEnum<String> {

    /**
     * 待办
     */
    TODO("todo", "cms-notify:enum.messageStatus.todo"),

    /**
     * 已办
     */
    DONE("done", "cms-notify:enum.messageStatus.done"),

    /**
     * 删除
     */
    REMOVE("remove", "cms-notify:enum.messageStatus.remove");

    @EnumValue
    @Getter
    private String value;

    @Getter
    private String messageKey;


    public static class Converter extends IEnum.Converter<String, MessageStatus> {

    }
}
