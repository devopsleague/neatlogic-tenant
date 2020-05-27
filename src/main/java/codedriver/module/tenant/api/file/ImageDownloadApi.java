package codedriver.module.tenant.api.file;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.exception.user.NoTenantException;
import codedriver.framework.file.dao.mapper.FileMapper;
import codedriver.framework.file.dto.FileVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.IsActived;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.BinaryStreamApiComponentBase;
import codedriver.module.tenant.exception.file.FileNotFoundException;

@Service
@IsActived
public class ImageDownloadApi extends BinaryStreamApiComponentBase {

	@Autowired
	private FileMapper fileMapper;

	@Autowired
	private FileSystem fileSystem;

	@Override
	public String getToken() {
		return "image/download";
	}

	@Override
	public String getName() {
		return "图片下载接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ @Param(name = "uuid", type = ApiParamType.STRING, desc = "图片uuid", isRequired = true) })
	@Description(desc = "图片下载接口")
	@Override
	public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String uuid = paramObj.getString("uuid");
		FileVo fileVo = fileMapper.getFileByUuid(uuid);
		String tenantUuid = TenantContext.get().getTenantUuid();
		if (StringUtils.isBlank(tenantUuid)) {
			throw new NoTenantException();
		}
		if (fileVo != null) {
			ServletOutputStream os = null;
			FSDataInputStream in = fileSystem.open(new Path("/" + tenantUuid + "/images/" + fileVo.getUuid()));
			String fileNameEncode = "";
			Boolean flag = request.getHeader("User-Agent").indexOf("Gecko") > 0;
			if (request.getHeader("User-Agent").toLowerCase().indexOf("msie") > 0 || flag) {
				fileNameEncode = URLEncoder.encode(fileVo.getName(), "UTF-8");// IE浏览器
			} else {
				fileNameEncode = new String(fileVo.getName().replace(" ", "").getBytes(StandardCharsets.UTF_8), "ISO8859-1");
			}
			response.setContentType(fileVo.getContentType());
			os = response.getOutputStream();
			IOUtils.copyLarge(in, os);
			if (os != null) {
				os.flush();
				os.close();
			}
			if (in != null) {
				in.close();
			}
		} else {
			throw new FileNotFoundException(uuid);
		}
		return null;
	}
}