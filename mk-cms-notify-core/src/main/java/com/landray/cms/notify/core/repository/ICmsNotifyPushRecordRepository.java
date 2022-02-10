package com.landray.cms.notify.core.repository;

import com.landray.cms.notify.core.entity.CmsNotifyPushRecord;
import com.landray.common.core.repository.IRepository;
import org.springframework.stereotype.Repository;


/**
 * 消息推送记录
 * @author lisc
 * @version 2021-12-24
 */
@Repository
public interface ICmsNotifyPushRecordRepository extends IRepository<CmsNotifyPushRecord> {

}
