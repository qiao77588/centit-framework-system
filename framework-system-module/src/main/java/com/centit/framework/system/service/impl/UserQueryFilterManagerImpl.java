package com.centit.framework.system.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.centit.framework.core.dao.DictionaryMapUtils;
import com.centit.framework.core.dao.QueryParameterPrepare;
import com.centit.framework.system.dao.UserQueryFilterDao;
import com.centit.framework.system.po.UserQueryFilter;
import com.centit.framework.system.service.UserQueryFilterManager;
import com.centit.support.algorithm.DatetimeOpt;
import com.centit.support.algorithm.StringBaseOpt;
import com.centit.support.database.utils.PageDesc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * UserQueryFilter  Service.
 * create by scaffold 2016-02-29
 * @author codefan@sina.com
 * 用户自定义过滤条件表null
*/
@Service
public class UserQueryFilterManagerImpl implements UserQueryFilterManager{

    public static final Logger logger = LoggerFactory.getLogger(UserQueryFilterManager.class);

    private UserQueryFilterDao userQueryFilterDao ;

    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public JSONArray listUserQueryFiltersAsJson(
            String[] fields,
            Map<String, Object> filterMap, PageDesc pageDesc){

        return DictionaryMapUtils.objectsToJSONArray(
                listObjects(filterMap,pageDesc), fields);
    }

    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public List<UserQueryFilter> listUserQueryFilterByModle(String userCode,String modelCode){
        return userQueryFilterDao.listUserQueryFilterByModle(userCode, modelCode);
    }

    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public Long getNextFilterKey(){
        return userQueryFilterDao.getNextKey();
    }

    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public Long saveUserDefaultFilter(UserQueryFilter userQueryFilter){
        if(StringBaseOpt.isNvl(userQueryFilter.getUserCode())||
                StringBaseOpt.isNvl(userQueryFilter.getModleCode()))
            return null;

        List<UserQueryFilter> filters = userQueryFilterDao.listUserDefaultFilterByModle(userQueryFilter.getUserCode(),
                                                                                        userQueryFilter.getModleCode());
        if(filters==null || filters.size()<1){
            userQueryFilter.setFilterNo(getNextFilterKey());
            userQueryFilter.setIsDefault("T");
            userQueryFilter.setCreateDate(DatetimeOpt.currentUtilDate());
            userQueryFilterDao.saveNewObject(userQueryFilter);
            return userQueryFilter.getFilterNo();
        }else{
            UserQueryFilter dbFilter;
            for(int i=1;i<filters.size();i++){
                 dbFilter = filters.get(i);
                 dbFilter.setIsDefault("F");
                 userQueryFilterDao.mergeUserFilter(dbFilter);
            }
            dbFilter = filters.get(0);
            dbFilter.setFilterName( userQueryFilter.getFilterName());
            dbFilter.setFilterValue(userQueryFilter.getFilterValue());
            userQueryFilter.setCreateDate(DatetimeOpt.currentUtilDate());
            userQueryFilterDao.mergeUserFilter(dbFilter);
            return dbFilter.getFilterNo();
        }

    }

    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public UserQueryFilter getUserDefaultFilter(String userCode,String modelCode){
        if(StringBaseOpt.isNvl(userCode)||
                StringBaseOpt.isNvl(modelCode))
            return null;

        Map<String,String> map=new HashMap<String,String>();
        map.put("userCode", userCode);
        map.put("modelCode", modelCode);
        List<UserQueryFilter> filters = userQueryFilterDao.listUserDefaultFilterByModle(userCode, modelCode);
        if(filters==null || filters.size()<1)
            return null;
        return filters.get(0);
    }

    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public UserQueryFilter getUserQueryFilter(Long filterNo){
        return userQueryFilterDao.getObjectById(filterNo);
    }

    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public boolean deleteUserQueryFilter(Long filterNo){
        UserQueryFilter uqf = userQueryFilterDao.getObjectById(filterNo);
        if(uqf==null)
            return false;
        if("T".equals(uqf.getIsDefault()))
            return false;
        userQueryFilterDao.deleteObject(uqf);
        return true;
    }

    @Override
    public void mergeObject(UserQueryFilter userQueryFilter) {
        userQueryFilterDao.mergeUserFilter(userQueryFilter);
    }

    @Override
    public void saveNewObject(UserQueryFilter userQueryFilter) {
        /*return*/ userQueryFilterDao.saveNewObject(userQueryFilter);
    }

    @Override
    public List<UserQueryFilter> listObjects(Map<String, Object> filterMap, PageDesc pageDesc) {
        return userQueryFilterDao.pageQuery(
            QueryParameterPrepare.makeMybatisOrderByParam(
                QueryParameterPrepare.prepPageParams(
                    filterMap,pageDesc,userQueryFilterDao.pageCount(filterMap)),UserQueryFilter.class));
    }
}

