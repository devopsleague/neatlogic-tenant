package codedriver.module.tenant.exception.menu;

import codedriver.framework.exception.core.ApiRuntimeException;

public  class MenuSaveException extends ApiRuntimeException {
	public MenuSaveException(String msg) {
		super(msg);
	}

}