/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.tenant.schedule.plugin;

import neatlogic.framework.dao.mapper.TeamMapper;
import neatlogic.framework.dto.TeamVo;
import neatlogic.framework.lrcode.LRCodeManager;
import neatlogic.framework.scheduler.annotation.Param;
import neatlogic.framework.scheduler.annotation.Prop;
import neatlogic.framework.scheduler.core.PublicJobBase;
import neatlogic.framework.scheduler.dto.JobObject;
import neatlogic.framework.transaction.util.TransactionUtil;
import org.apache.commons.lang3.StringUtils;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import java.util.*;

@Component
@DisallowConcurrentExecution
public class SyncLdapTeamSchedule extends PublicJobBase {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private TeamMapper teamMapper;

    @Override
    public String getName() {
        return "同步LDAP的组织架构";
    }

    @Override
    public void reloadJob(JobObject jobObject) {

    }

    @Override
    public void initJob(String tenantUuid) {

    }

    @Prop({
            @Param(name = "ldapUrl", controlType = "text", description = "ldap地址", required = true,sort=0),
            @Param(name = "userDn", controlType = "text", description = "同步账号dn", required = true,sort=1),
            @Param(name = "userSecret", controlType = "text", description = "登录密码", required = true,sort=2),
            @Param(name = "searchBase", controlType = "text", description = "从指定目录开始查找", required = true,sort=3),
            @Param(name = "searchFilter", controlType = "text", description = "过滤类型，默认：objectclass=organizationalUnit",required=false,sort=4),
            @Param(name = "rootParentUUid", controlType = "text", description = "根节点，默认：0",required=false,sort=5)
    })
    @Override
    public void executeInternal(JobExecutionContext context, JobObject jobObject) throws Exception {

        String batchSize = "20000"; //分页数量，默认是1000
        String ldapUrl = getPropValue(jobObject, "ldapUrl");
        String userDn = getPropValue(jobObject, "userDn");
        String userSecret = getPropValue(jobObject, "userSecret");
        String searchBase = getPropValue(jobObject, "searchBase"); //从xx顶层目录快速查找
        String searchFilter = getPropValue(jobObject, "searchFilter"); //LDAP搜索过滤器类
        String rootParentUUid = getPropValue(jobObject, "rootParentUUid");
        if(StringUtils.isBlank(rootParentUUid)){
            rootParentUUid = TeamVo.ROOT_UUID;
        }

        if (StringUtils.isBlank(searchFilter)) {
            searchFilter = "objectclass=organizationalUnit";
        }

        //parent不存在
        if(teamMapper.getTeamByUuid(rootParentUUid) == null ){
            rootParentUUid = TeamVo.ROOT_UUID;
        }

        if (StringUtils.isNotBlank(ldapUrl) && StringUtils.isNotBlank(userDn) && StringUtils.isNotBlank(userSecret)) {
            TransactionStatus transactionStatus = TransactionUtil.openTx();
            Hashtable<String, String> HashEnv = new Hashtable<String, String>();
            HashEnv.put(Context.SECURITY_AUTHENTICATION, "simple"); // LDAP访问安全级别
            HashEnv.put(Context.SECURITY_PRINCIPAL, userDn);
            HashEnv.put(Context.SECURITY_CREDENTIALS, userSecret);
            HashEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            HashEnv.put(Context.PROVIDER_URL, ldapUrl);
            HashEnv.put(Context.BATCHSIZE, batchSize);
            try {
                LdapContext ctx = new InitialLdapContext(HashEnv, null);
                // 搜索控制器
                SearchControls searchCtls = new SearchControls();
                // 创建搜索控制器
                searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
                // 对象的每个属性名
                String[] returnedAtts = { "dn","entryUUID","description" };
                searchCtls.setReturningAttributes(returnedAtts);

                // 根据设置的域节点、过滤器类和搜索控制器搜索LDAP得到结果
                NamingEnumeration answer = ctx.search(searchBase, searchFilter, searchCtls);
                String rootDn = null ;
                Map<String , String> uuidMap = new HashMap<>();
                List<TeamVo> teamVoList = new ArrayList<>();
                while (answer.hasMoreElements()) {
                    SearchResult sr = (SearchResult) answer.next();
                    String name = sr.getName();
                    String teamId = null, teamName = null ;
                    String fullName = sr.getNameInNamespace();
                    String parentName = getParentName(fullName);
                    if (StringUtils.isNotBlank(name)) {
                        teamName = getTeamName(name);
                    }else{
                        teamName = fullName.split(",")[0].split("ou=")[1];
                    }
                    if(StringUtils.isBlank(name)){
                        rootDn = fullName;
                    }
                    Attributes Attrs = sr.getAttributes();
                    if (Attrs != null) {
                        try {
                            for (NamingEnumeration ne = Attrs.getAll(); ne.hasMore(); ) {
                                Attribute Attr = (Attribute) ne.next();
                                String attrName = Attr.getID().toString();
                                String attrValue = null;
                                for (NamingEnumeration e = Attr.getAll(); e.hasMore(); ) {
                                    attrValue = e.next().toString();
                                }
                                if (attrName.equals("entryUUID")) {
                                    teamId = attrValue.replace("-", "");
                                }
                            }
                        } catch (NamingException e) {
                            logger.error("[Sync Ldap Team Error]:" + e.getMessage());
                        }
                    }
                    uuidMap.put(teamName,teamId);
                    TeamVo teamVo = new TeamVo();
                    teamVo.setSource("ldap");
                    teamVo.setUuid(teamId);
                    teamVo.setName(teamName);
                    teamVo.setParentName(parentName);
                    setUpwardTeamName(teamVo , fullName , rootDn);
                    teamVoList.add(teamVo);
                }
                ctx.close();

                for (TeamVo teamVo: teamVoList) {
                    setUpwardUuidPath(uuidMap ,teamVo);
                    String parentUuid = uuidMap.get(teamVo.getParentName());
                    teamVo.setParentUuid(parentUuid == null ? rootParentUUid : parentUuid);
                }

                this.teamMapper.batchSaveTeam(teamVoList);
                //重算左右编码
                LRCodeManager.rebuildLeftRightCode("team", "uuid" , "parent_uuid" );
                //数据清理
                this.teamMapper.updateTeamIsDeleteBySource("ldap");
                TransactionUtil.commitTx(transactionStatus);
                teamVoList = null ;
            } catch (NamingException e) {
                logger.error("[Sync Ldap Team Error]:" + e.getMessage());
                TransactionUtil.rollbackTx(transactionStatus);
            }
        }else{
            logger.error("[Sync Ldap Team Error]: Incomplete Plugin parameters.");
        }
    }

    /**
     * 计算检索的组 name层级
     * @param teamVo
     * @param fullName
     * @param rootDn
     */
    private static void setUpwardTeamName(TeamVo teamVo ,String fullName , String rootDn){
        String handleName = fullName.replace(rootDn,"");
        if(StringUtils.isNotBlank(handleName)){
            List<String> upwardTeamNameList = new ArrayList<>();
            String[] dnNames = handleName.split(",");
            for (String dnName : dnNames) {
                if(StringUtils.isNotBlank(dnName)){
                    upwardTeamNameList.add(getTeamName(dnName));
                }
            }
            //补充根
            String rootName = getTeamName(rootDn);
            upwardTeamNameList.add(rootName);
            Collections.reverse(upwardTeamNameList);
            teamVo.setUpwardNamePath(String.join("/", upwardTeamNameList));
        }
    }

    /**
     * 计算检索的组uuid 层级 path
     * @param uuidMap
     * @param teamVo
     */
    private static void setUpwardUuidPath(Map<String,String> uuidMap , TeamVo teamVo){
        String namePath = teamVo.getUpwardNamePath();
        if(StringUtils.isNotBlank(namePath)){
            String[] namePaths = namePath.split("/");
            List<String> upwardTeamUuidList = new ArrayList<>();
            for (String name : namePaths) {
                if(StringUtils.isNotBlank(name) && uuidMap.containsKey(name)){
                    upwardTeamUuidList.add(uuidMap.get(name));
                }
            }
            teamVo.setUpwardUuidPath(String.join(",", upwardTeamUuidList));
        }
    }

    /**
     * 获取参数
     * @param jobObject
     * @param key
     * @return
     */
    private static String getPropValue(JobObject jobObject, String key) {
        Object value = jobObject.getProp(key);
        if (value == null) {
            return "";
        } else {
            return value.toString();
        }
    }

    private static String getTeamName(String name){
        if(StringUtils.isBlank(name)){
            return "";
        }
        String groupName = name.split("ou=")[1].split(",")[0];
        //去掉顶层结构
        if("groups".equals(groupName)){
            groupName = "";
        }
        return groupName;
    }

    private static String getParentName(String fullName) {
        if (StringUtils.isBlank(fullName)) {
            return "";
        }
        String parentFullName = fullName.substring(fullName.indexOf(",") + 1, fullName.length());
        return getTeamName(parentFullName);
    }

}