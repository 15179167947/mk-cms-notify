package com.landray.cms.notify.core.config;


import com.landray.sys.auth.role.annotation.AuthRole;
import com.landray.sys.auth.role.annotation.AuthRoles;

/**
 * 权限角色定义
 * @author lisc
 * @version 2021-12-24
 */
@AuthRoles(roles = {
        @AuthRole(name = "ROLE_CMSNOTIFY_DEFAULT", messageKey = "cms-notify:role.ROLE_CMSNOTIFY_DEFAULT.name",
                desc = "cms-notify:role.ROLE_CMSNOTIFY_DEFAULT.desc"),
        @AuthRole(name = "ROLE_CMSNOTIFY_BACKSTAGE_MANAGER", messageKey = "cms-notify:role.ROLE_CMSNOTIFY_BACKSTAGE_MANAGER.name",
                desc = "cms-notify:role.ROLE_CMSNOTIFY_BACKSTAGE_MANAGER.desc"),
        @AuthRole(name = "ROLE_CMSNOTIFY_ADMIN", messageKey = "cms-notify:role.ROLE_CMSNOTIFY_ADMIN.name",
                desc = "cms-notify:role.ROLE_CMSNOTIFY_ADMIN.desc"),
        @AuthRole(name = "ROLE_CMSNOTIFY_RECORD_READER", messageKey = "cms-notify:role.ROLE_CMSNOTIFY_RECORD_READER.name",
                desc = "cms-notify:role.ROLE_CMSNOTIFY_RECORD_READER.desc"),
        @AuthRole(name = "ROLE_CMSNOTIFY_RECORD_DELETE", messageKey = "cms-notify:role.ROLE_CMSNOTIFY_RECORD_DELETE.name",
                desc = "cms-notify:role.ROLE_CMSNOTIFY_RECORD_DELETE.desc"),
})
public class CmsNotifyRole {

    /** 消息通知_默认权限 */
    public static final String ROLE_CMSNOTIFY_DEFAULT = "ROLE_CMSNOTIFY_DEFAULT";
    public static final String ROLE_CMSNOTIFY_BACKSTAGE_MANAGER = "ROLE_CMSNOTIFY_BACKSTAGE_MANAGER";
    /** 消息通知_管理员 */
    public static final String ROLE_CMSNOTIFY_ADMIN = "ROLE_CMSNOTIFY_ADMIN";
    /** 消息通知_阅读文档 */
    public static final String ROLE_CMSNOTIFY_RECORD_READER = "ROLE_CMSNOTIFY_RECORD_READER";
    /** 消息通知_删除文档 */
    public static final String ROLE_CMSNOTIFY_RECORD_DELETE = "ROLE_CMSNOTIFY_RECORD_DELETE";

}
