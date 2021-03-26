package codedriver.module.tenant.api.form;

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
import codedriver.framework.form.exception.FormVersionNotFoundException;
import codedriver.framework.matrix.dao.mapper.MatrixMapper;
import codedriver.framework.matrix.dto.ProcessMatrixFormComponentVo;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.IValid;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.google.common.base.Objects;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional
@AuthAction(action = FORM_MODIFY.class)
@OperationType(type = OperationTypeEnum.CREATE)
public class FormSaveApi extends PrivateApiComponentBase {

    @Resource
    private FormMapper formMapper;

    @Resource
    private MatrixMapper matrixMapper;

    @Override
    public String getToken() {
        return "form/save";
    }

    @Override
    public String getName() {
        return "表单保存接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Override
    @Input({
            @Param(name = "uuid", type = ApiParamType.STRING, desc = "表单uuid", isRequired = true),
            @Param(name = "name", type = ApiParamType.REGEX, rule = "^[A-Za-z_\\d\\u4e00-\\u9fa5]+$", isRequired = true, maxLength = 50, desc = "表单名称"),
            //@Param(name = "isActive", type = ApiParamType.ENUM, rule = "0,1", desc = "是否激活", isRequired = true),
            @Param(name = "currentVersionUuid", type = ApiParamType.STRING, desc = "当前版本的uuid，为空代表创建一个新版本", isRequired = false),
            @Param(name = "formConfig", type = ApiParamType.JSONOBJECT, desc = "表单控件生成的json内容", isRequired = true)
    })
    @Output({
            @Param(name = "uuid", type = ApiParamType.STRING, desc = "表单uuid"),
            @Param(name = "formVersionUuid", type = ApiParamType.STRING, desc = "表单版本uuid")
    })
    @Description(desc = "表单保存接口")
    public Object myDoService(JSONObject jsonObj) throws Exception {
        FormVo formVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<FormVo>() {
        });
        if (formMapper.checkFormNameIsRepeat(formVo) > 0) {
            throw new FormNameRepeatException(formVo.getName());
        }
        //判断表单是否存在
        if (formMapper.checkFormIsExists(formVo.getUuid()) == 0) {
            //插入表单信息
            formMapper.insertForm(formVo);
        } else {
            //更新表单信息
            formMapper.updateForm(formVo);
        }

        //插入表单版本信息
        FormVersionVo formVersionVo = new FormVersionVo();
        formVersionVo.setFormConfig(formVo.getFormConfig());
        formVersionVo.setFormUuid(formVo.getUuid());
        //formMapper.resetFormVersionIsActiveByFormUuid(formVo.getUuid());
        //formVersionVo.setIsActive(1);
        if (StringUtils.isBlank(formVo.getCurrentVersionUuid())) {
            Integer version = formMapper.getMaxVersionByFormUuid(formVo.getUuid());
            if (version == null) {//如果表单没有激活版本时，设置当前版本号为1，且为激活版本
                version = 1;
                formVersionVo.setIsActive(1);
            } else {
                version += 1;
                formVersionVo.setIsActive(0);
            }
            formVersionVo.setVersion(version);
            formMapper.insertFormVersion(formVersionVo);
        } else {
            FormVersionVo formVersion = formMapper.getFormVersionByUuid(formVo.getCurrentVersionUuid());
            if (formVersion == null) {
                throw new FormVersionNotFoundException(formVo.getCurrentVersionUuid());
            }
            if (!formVo.getUuid().equals(formVersion.getFormUuid())) {
                throw new FormIllegalParameterException("表单版本：'" + formVo.getCurrentVersionUuid() + "'不属于表单：'" + formVo.getUuid() + "'的版本");
            }
            formVersionVo.setUuid(formVo.getCurrentVersionUuid());
            formVersionVo.setIsActive(formVersion.getIsActive());
            formMapper.updateFormVersion(formVersionVo);
        }
        //保存激活版本时，更新表单属性信息
        if (Objects.equal(formVersionVo.getIsActive(), 1)) {
            formMapper.deleteFormAttributeByFormUuid(formVo.getUuid());
            List<FormAttributeVo> formAttributeList = formVersionVo.getFormAttributeList();
            if (CollectionUtils.isNotEmpty(formAttributeList)) {
                for (FormAttributeVo formAttributeVo : formAttributeList) {
                    formMapper.insertFormAttribute(formAttributeVo);
                }
            }
        }

        List<ProcessMatrixFormComponentVo> processMatrixFormComponentList = formVersionVo.getProcessMatrixFormComponentList();
        formMapper.deleteProcessMatrixFormComponentByFormVersionUuid(formVersionVo.getUuid());
        if (CollectionUtils.isNotEmpty(processMatrixFormComponentList)) {
            for (ProcessMatrixFormComponentVo processMatrixFormComponentVo : processMatrixFormComponentList) {
                matrixMapper.insertMatrixFormComponent(processMatrixFormComponentVo);
            }
        }
        JSONObject resultObj = new JSONObject();
        resultObj.put("uuid", formVo.getUuid());
        resultObj.put("currentVersionUuid", formVersionVo.getUuid());
        resultObj.put("currentVersion", formVersionVo.getVersion());
        return resultObj;
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

}
