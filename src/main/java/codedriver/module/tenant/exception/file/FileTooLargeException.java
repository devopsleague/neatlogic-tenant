package codedriver.module.tenant.exception.file;

import codedriver.framework.exception.core.ApiRuntimeException;

public class FileTooLargeException extends ApiRuntimeException {
	public FileTooLargeException(Long fileSize, Long maxSize) {
		super("附件最大支持：" + maxSize + "字节，当前文件：" + fileSize + "字节");
	}

}
