package codedriver.module.tenant.service;

import codedriver.module.tenant.dto.MenuVo;

public interface MenuService {
	public int checkIsChildern(Long menuId);
	public int saveMenu(MenuVo menuVo);
	public int deleteMenu(Long menuId);

}
