package com.landray.cms.notify.core.controller.config;


import com.landray.cms.notify.core.config.CmsNotifyRole;
import com.landray.cms.notify.core.entity.config.CmsNotifyConfig;
import com.landray.cms.notify.core.service.config.CmsNotifyConfigService;
import com.landray.common.dto.Response;
import com.landray.sys.auth.constant.ValidatorOperator;
import com.landray.sys.auth.validator.annotation.AnonymousValidator;
import com.landray.sys.auth.validator.annotation.AuthValidator;
import com.landray.sys.auth.validator.annotation.AuthValidators;
import com.landray.sys.auth.validator.annotation.ValidateParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * 消息通知扩展设置 Controller
 * @date 2021-07-20
 */
@RestController
@RequestMapping({"/data/cms-notify/cmsNotifyConfig"})
@AuthValidators(opt = ValidatorOperator.OPT_OR, value = {
        @AuthValidator(value = "roleValidator", params = {
                @ValidateParam(CmsNotifyRole.ROLE_CMSNOTIFY_BACKSTAGE_MANAGER)}),
        @AuthValidator(value = "roleValidator", params = {
                @ValidateParam(CmsNotifyRole.ROLE_CMSNOTIFY_ADMIN)})
})
@AnonymousValidator
public class CmsNotifyConfigController {

    @Autowired
    private CmsNotifyConfigService cmsNotifyConfigService;

    @PostMapping({"/save"})
    @AuthValidators(opt = ValidatorOperator.OPT_OR, value = {
            @AuthValidator(value = "roleValidator", params = {
                    @ValidateParam(CmsNotifyRole.ROLE_CMSNOTIFY_BACKSTAGE_MANAGER)}),
            @AuthValidator(value = "roleValidator", params = {
                    @ValidateParam(CmsNotifyRole.ROLE_CMSNOTIFY_ADMIN)})
    })
    public Response save(@RequestBody CmsNotifyConfig config) {
        this.cmsNotifyConfigService.save(config);
        return Response.ok();
    }

    @AuthValidators({@AuthValidator("notAnonymousValidator")})
    @PostMapping({"/getCmsNotifyConfig"})
    public Response<CmsNotifyConfig> getCmsNotifyConfig() {
        CmsNotifyConfig notifyConfig = this.cmsNotifyConfigService.getCmsNotifyConfig();
        if (Objects.isNull(notifyConfig)) {
            notifyConfig = new CmsNotifyConfig();
            notifyConfig.setIsEnabledK2Bpm(true);
        }
        return Response.ok(notifyConfig);
    }
}