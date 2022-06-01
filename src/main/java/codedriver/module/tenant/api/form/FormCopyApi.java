package codedriver.module.tenant.api.form;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.FORM_MODIFY;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.crossover.CrossoverServiceFactory;
import codedriver.framework.dto.FieldValidResultVo;
import codedriver.framework.exception.type.ParamNotExistsException;
import codedriver.framework.form.dao.mapper.FormMapper;
import codedriver.framework.form.dto.FormAttributeVo;
import codedriver.framework.form.dto.FormVersionVo;
import codedriver.framework.form.dto.FormVo;
import codedriver.framework.form.exception.FormNameRepeatException;
import codedriver.framework.form.exception.FormNotFoundException;
import codedriver.framework.form.exception.FormVersionNotFoundException;
import codedriver.framework.form.service.IFormCrossoverService;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.IValid;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.util.RegexUtils;
import codedriver.framework.util.UuidUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Objects;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.CREATE)
@AuthAction(action = FORM_MODIFY.class)
public class FormCopyApi extends PrivateApiComponentBase {

    @Resource
    private FormMapper formMapper;

    @Override
    public String getToken() {
        return "form/copy";
    }

    @Override
    public String getName() {
        return "表单复制接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "uuid", type = ApiParamType.STRING, isRequired = true, desc = "表单uuid"),
            @Param(name = "name", type = ApiParamType.REGEX, rule = RegexUtils.NAME, isRequired = true, maxLength = 50, desc = "表单名称"),
            @Param(name = "currentVersionUuid", type = ApiParamType.STRING, desc = "要复制的版本uuid,空表示复制所有版本")
    })
    @Output({
            @Param(name = "Return", explode = FormVo.class, desc = "新表单信息")
    })
    @Description(desc = "表单复制接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String uuid = jsonObj.getString("uuid");
        String currentVersionUuid = jsonObj.getString("currentVersionUuid");
        if (StringUtils.isNotBlank(currentVersionUuid)) {
            FormVersionVo formVersionVo = formMapper.getFormVersionByUuid(currentVersionUuid);
            if (formVersionVo == null) {
                throw new FormVersionNotFoundException(currentVersionUuid);
            }
            FormVo formVo = formMapper.getFormByUuid(formVersionVo.getFormUuid());
            if (formVo == null) {
                throw new FormNotFoundException(formVersionVo.getFormUuid());
            }
            formVo.setUuid(null);
            String newFormUuid = formVo.getUuid();

            String oldName = formVo.getName();
            String name = jsonObj.getString("name");
            formVo.setName(name);
            //如果表单名称已存在
            if (formMapper.checkFormNameIsRepeat(formVo) > 0) {
                throw new FormNameRepeatException(name);
            }
            formMapper.insertForm(formVo);
            formVersionVo.setVersion(1);
            saveFormVersion(formVersionVo, newFormUuid, oldName, name);
            formVo.setCurrentVersion(formVersionVo.getVersion());
            formVo.setCurrentVersionUuid(formVersionVo.getUuid());
            return formVo;
        } else if(StringUtils.isNotBlank(uuid)) {
            FormVo formVo = formMapper.getFormByUuid(uuid);
            if (formVo == null) {
                throw new FormNotFoundException(uuid);
            }
            formVo.setUuid(null);
            String newFormUuid = formVo.getUuid();
            String oldName = formVo.getName();
            String name = jsonObj.getString("name");
            formVo.setName(name);
            //如果表单名称已存在
            if (formMapper.checkFormNameIsRepeat(formVo) > 0) {
                throw new FormNameRepeatException(name);
            }
            formMapper.insertForm(formVo);
            List<FormVersionVo> formVersionList = formMapper.getFormVersionByFormUuid(uuid);
            for (FormVersionVo formVersionVo : formVersionList) {
                saveFormVersion(formVersionVo, newFormUuid, oldName, name);
                if (formVersionVo.getIsActive().equals(1)) {
                    formVo.setCurrentVersion(formVersionVo.getVersion());
                    formVo.setCurrentVersionUuid(formVersionVo.getUuid());
                }
            }
            return formVo;
        } else {
            throw new ParamNotExistsException("uuid", "currentVersionUuid");
        }
    }

    public IValid name() {
        return value -> {
            FormVo formVo = JSON.toJavaObject(value, FormVo.class);
            if (formMapper.checkFormNameIsRepeat(formVo) > 0) {
                return new FieldValidResultVo(new FormNameRepeatException(formVo.getName()));
            }
            return new FieldValidResultVo();
        };
    }

    private void saveFormVersion(FormVersionVo formVersionVo, String newFormUuid, String oldName, String newName) {
        String content = formVersionVo.getFormConfig();
        content = content.replace(formVersionVo.getFormUuid(), newFormUuid);
        content = content.replace(oldName, newName);
        List<FormAttributeVo> formAttributeList = formVersionVo.getFormAttributeList();
        if (CollectionUtils.isNotEmpty(formAttributeList)) {
            IFormCrossoverService formCrossoverService = CrossoverServiceFactory.getApi(IFormCrossoverService.class);
            for (FormAttributeVo formAttributeVo : formAttributeList) {
                String newFormAttributeUuid = UuidUtil.randomUuid();
                content = content.replace(formAttributeVo.getUuid(), newFormAttributeUuid);
            }
            formVersionVo.setUuid(null);
            formVersionVo.setFormUuid(newFormUuid);
            formVersionVo.setFormConfig(content);
    //        formVersionVo.setEditor(UserContext.get().getUserUuid(true));
            formMapper.insertFormVersion(formVersionVo);
            formVersionVo.setFormAttributeList(null);
            formAttributeList = formVersionVo.getFormAttributeList();
            for (FormAttributeVo formAttributeVo : formAttributeList) {
                //保存激活版本时，插入表单属性信息
                if (Objects.equal(formVersionVo.getIsActive(), 1)) {
                    formMapper.insertFormAttribute(formAttributeVo);
                }
                formCrossoverService.saveDependency(formAttributeVo);
            }
        }
    }
}
