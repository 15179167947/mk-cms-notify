package com.landray.cms.notify.core.controller;

import com.landray.cms.notify.api.ICmsNotifyPushRecordApi;
import com.landray.cms.notify.core.config.CmsNotifyRole;
import com.landray.cms.notify.dto.CmsNotifyPushRecordVO;
import com.landray.common.core.controller.AbstractController;
import com.landray.common.core.controller.CombineController;
import com.landray.common.core.controller.DeleteAllController;
import com.landray.common.core.controller.ListController;
import com.landray.common.core.dto.IdVO;
import com.landray.common.core.dto.IdsDTO;
import com.landray.common.dto.Response;
import com.landray.common.exception.NoRecordException;
import com.landray.sys.auth.constant.ValidatorOperator;
import com.landray.sys.auth.validator.annotation.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * 消息推送记录 Controller
 * @author lisc
 * @version 2021-12-24
 */
@Api(tags = {"已办消息推送接口"}, protocols = "http")
@Slf4j
@RestController
@RequestMapping(path = "/data/cms-notify/cmsNotifyPushRecord")
@ModuleValidator(opt = ValidatorOperator.OPT_OR, validators = {
    @AuthValidator(value = "roleValidator", params = {
        @ValidateParam(CmsNotifyRole.ROLE_CMSNOTIFY_DEFAULT)}),
    @AuthValidator(value = "roleValidator", params = {
        @ValidateParam(CmsNotifyRole.ROLE_CMSNOTIFY_ADMIN)})
})
public class CmsNotifyPushRecordController
    extends AbstractController<ICmsNotifyPushRecordApi, CmsNotifyPushRecordVO>
    implements ListController<ICmsNotifyPushRecordApi, CmsNotifyPushRecordVO> {

    @AuthValidators(opt = ValidatorOperator.OPT_OR, value = {
            @AuthValidator(value = "roleValidator", params = {
                    @ValidateParam(CmsNotifyRole.ROLE_CMSNOTIFY_RECORD_READER)}),
            @AuthValidator(value = "roleValidator", params = {
                    @ValidateParam(CmsNotifyRole.ROLE_CMSNOTIFY_ADMIN)})
    })
    @PostMapping({"get"})
    @ApiOperation("查看接口")
    public Response<CmsNotifyPushRecordVO> get(@RequestBody IdVO vo) {
        Optional<CmsNotifyPushRecordVO> result = this.getApi().loadById(vo);
        if (!result.isPresent()) {
            throw new NoRecordException();
        } else {
            return Response.ok(result.get());
        }
    }

    @AuthValidators(opt = ValidatorOperator.OPT_OR, value = {
            @AuthValidator(value = "roleValidator", params = {
                    @ValidateParam(CmsNotifyRole.ROLE_CMSNOTIFY_RECORD_DELETE)}),
            @AuthValidator(value = "roleValidator", params = {
                    @ValidateParam(CmsNotifyRole.ROLE_CMSNOTIFY_ADMIN)})
    })
    @PostMapping({"delete"})
    @ApiOperation("删除接口")
    public Response<?> delete(@RequestBody IdVO vo) {
        this.getApi().delete(vo);
        return Response.ok();
    }

    @AuthValidators(opt = ValidatorOperator.OPT_OR, value = {
            @AuthValidator(value = "roleValidator", params = {
                    @ValidateParam(CmsNotifyRole.ROLE_CMSNOTIFY_RECORD_DELETE)}),
            @AuthValidator(value = "roleValidator", params = {
                    @ValidateParam(CmsNotifyRole.ROLE_CMSNOTIFY_ADMIN)})
    })
    @PostMapping({"deleteAll"})
    @ApiOperation("批量删除接口")
    public Response<?> deleteAll(@RequestBody IdsDTO ids) {
        this.getApi().deleteAll(ids);
        return Response.ok();
    }

}
