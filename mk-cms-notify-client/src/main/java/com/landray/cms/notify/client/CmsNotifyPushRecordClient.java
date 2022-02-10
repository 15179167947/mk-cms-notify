package com.landray.cms.notify.client;

import com.landray.cms.notify.api.ICmsNotifyPushRecordApi;
import org.springframework.cloud.openfeign.FeignClient;


/**
 * 消息推送记录 Client 接口
 * @author lisc
 * @version 2021-12-24
 */
@FeignClient(name = "module/cms-notify", path = "/api/cms-notify/cmsNotifyPushRecord")
public interface CmsNotifyPushRecordClient extends ICmsNotifyPushRecordApi {

}
