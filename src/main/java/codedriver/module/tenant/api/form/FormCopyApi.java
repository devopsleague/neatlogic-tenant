package codedriver.module.tenant.api.form;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.FORM_MODIFY;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dto.FieldValidResultVo;
import codedriver.framework.form.dao.mapper.FormMapper;
import codedriver.framework.form.dto.FormAttributeVo;
import codedriver.framework.form.dto.FormVersionVo;
import codedriver.framework.form.dto.FormVo;
import codedriver.framework.form.exception.FormIllegalParameterException;
import codedriver.framework.form.exception.FormNameRepeatException;
import codedriver.framework.form.exception.FormNotFoundException;
import codedriver.framework.form.exception.FormVersionNotFoundException;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.IValid;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

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
            @Param(name = "name", type = ApiParamType.REGEX, rule = "^[A-Za-z_\\d\\u4e00-\\u9fa5]+$", isRequired = true, maxLength = 50, desc = "表单名称"),
            @Param(name = "currentVersionUuid", type = ApiParamType.STRING, desc = "要复制的版本uuid,空表示复制所有版本")
    })
    @Output({
            @Param(name = "Return", explode = FormVo.class, desc = "新表单信息")
    })
    @Description(desc = "表单复制接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String uuid = jsonObj.getString("uuid");
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
        if (jsonObj.containsKey("currentVersionUuid")) {
            String currentVersionUuid = jsonObj.getString("currentVersionUuid");
            FormVersionVo formVersionVo = formMapper.getFormVersionByUuid(currentVersionUuid);
            if (formVersionVo == null) {
                throw new FormVersionNotFoundException(currentVersionUuid);
            }
            if (!uuid.equals(formVersionVo.getFormUuid())) {
                throw new FormIllegalParameterException("表单版本：'" + currentVersionUuid + "'不属于表单：'" + uuid + "'的版本");
            }
            formVersionVo.setVersion(1);
            saveFormVersion(formVersionVo, newFormUuid, oldName, name);
            formVo.setCurrentVersion(formVersionVo.getVersion());
            formVo.setCurrentVersionUuid(formVersionVo.getUuid());
        } else {
            List<FormVersionVo> formVersionList = formMapper.getFormVersionByFormUuid(uuid);
            if (formVersionList == null || formVersionList.isEmpty()) {
                throw new FormIllegalParameterException("表单：'" + uuid + "'没有版本");
            }
            for (FormVersionVo formVersionVo : formVersionList) {
                saveFormVersion(formVersionVo, newFormUuid, oldName, name);
                if (formVersionVo.getIsActive().equals(1)) {
                    formVo.setCurrentVersion(formVersionVo.getVersion());
                    formVo.setCurrentVersionUuid(formVersionVo.getUuid());
                }
            }
        }
        return formVo;
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
        List<FormAttributeVo> oldFormAttributeList = formMapper.getFormAttributeList(new FormAttributeVo(newFormUuid, formVersionVo.getUuid()));
        for (FormAttributeVo formAttributeVo : oldFormAttributeList) {
            String newFormAttributeUuid = UUID.randomUUID().toString().replace("-", "");
            content = content.replace(formAttributeVo.getUuid(), newFormAttributeUuid);
        }
        formVersionVo.setUuid(null);
        formVersionVo.setFormUuid(newFormUuid);
        formVersionVo.setFormConfig(content);
        formVersionVo.setEditor(UserContext.get().getUserUuid(true));
        formMapper.insertFormVersion(formVersionVo);
        List<FormAttributeVo> formAttributeList = formVersionVo.getFormAttributeList();
        if (formAttributeList != null && formAttributeList.size() > 0) {
            for (FormAttributeVo formAttributeVo : formAttributeList) {
                formMapper.insertFormAttribute(formAttributeVo);
            }
        }
    }
}
