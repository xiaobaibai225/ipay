package com.pay.framework.db;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.pay.framework.db.BeanOperator.SqlTypes;

@Component
public class JDBCTemplateManager implements DBManager {

    @Autowired
    private JdbcTemplate template;

    public JdbcTemplate getTemplate() {
        return template;
    }

    /**
     * 插入/更新/删除
     *
     * @param sql
     * @param args
     * @return 本次更新影响的行数
     */
    @Override
    public int update(String sql, Object[] args) {
        int result = -1;
        result = template.update(sql, args);
        return result;
    }

    /**
     * 批量插入/更新/删除
     *
     * @param sql
     * @param batchArgs
     * @return 每一条的状态
     */
    @Override
    public int[] batchUpdate(String sql, List<Object[]> batchArgs) {
        int[] result = {};
        result = template.batchUpdate(sql, batchArgs);
        return result;
    }

    /**
     * 查询列表
     *
     * @param sql
     * @param args
     * @return 查询的数据列表
     */
    @Override
    public List<Map<String, Object>> queryForList(String sql, Object[] args) {
        List<Map<String, Object>> result = null;
        result = template.queryForList(sql, args);
        return result;
    }

    @Override
    public <T> int insertObject(T obj) {
        String sql = BeanOperator.getSqlByObject(SqlTypes.INSERT, obj);
        return template.update(sql);
    }

    /**
     * @param sql
     * @return
     * @throws DataAccessException
     */
    @Override
    public List<Map<String, Object>> queryForList(String sql) throws DataAccessException {
        return template.queryForList(sql);
    }

    @Override
    public <T> List<T> queryForListObject(String sql, Class<T> elementType) throws DataAccessException {
        List<Map<String, Object>> resultList = template.queryForList(sql);
        //使用了bean builder
        return BeanOperator.listMap2Object(resultList, elementType);
    }

    @Override
    public <T> List<T> queryForListObject(String sql, Object[] args, Class<T> elementType) throws DataAccessException {
        List<Map<String, Object>> resultList = template.queryForList(sql, args);
        //使用了bean builder
        return BeanOperator.listMap2Object(resultList, elementType);
    }

    @Override
    public <T> List<T> queryForListObject(String sql, Class<T> elementType, int page, int pageSize) throws DataAccessException {
        int startIndex = page * pageSize;
        int endIndex = (page + 1) * pageSize;
        sql = "select * from (select rOraclePageSQL.*,ROWNUM as currentRow from (" +
                sql + ") rOraclePageSQL where rownum <=" + endIndex + ") where currentRow>" + startIndex;

        return queryForListObject(sql, elementType);
    }


    /**
     * 分页查询列表
     *
     * @param sql         原始sql
     * @param args        参数
     * @param elementType 返回对象类型
     * @param rowNum      起始行
     * @param limit       查询的行数
     * @return
     * @throws DataAccessException
     */
    @Override
    public <T> List<T> queryForListObject(String sql, Object[] args, Class<T> elementType, int page, int pageSize) throws DataAccessException {
        int startIndex = page * pageSize;
        int endIndex = (page + 1) * pageSize;
        sql = "select * from (select rOraclePageSQL.*,ROWNUM as currentRow from (" +
                sql + ") rOraclePageSQL where rownum <=" + endIndex + ") where currentRow>" + startIndex;

        return queryForListObject(sql, args, elementType);
    }

    /**
     * 分页查询列表
     *
     * @param sql         原始sql
     * @param args        可变参数
     * @param elementType 返回对象类型
     * @param rowNum      起始行
     * @param limit       查询的行数
     * @return
     * @throws DataAccessException
     */
    @Override
    public <T> List<T> queryForListObject(String sql, Class<T> elementType, int page, int pageSize, Object... args) throws DataAccessException {
        int startIndex = page * pageSize;
        int endIndex = (page + 1) * pageSize;
        sql = "select * from (select rOraclePageSQL.*,ROWNUM as currentRow from (" +
                sql + ") rOraclePageSQL where rownum <=" + endIndex + ") where currentRow>" + startIndex;

        return queryForListObject(sql, elementType, args);
    }

    @Override
    public <T> List<T> queryForListObject(String sql, Class<T> elementType, Object... args) throws DataAccessException {
        List<Map<String, Object>> resultList = template.queryForList(sql, args);
        //使用了bean builder
        return BeanOperator.listMap2Object(resultList, elementType);
    }

    @Override
    public Map<String, Object> queryForMap(String sql) throws DataAccessException {
        try {
            return template.queryForMap(sql);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public Map<String, Object> queryForMap(String sql, Object... args) throws DataAccessException {
        try {
            return template.queryForMap(sql, args);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public Map<String, Object> queryForMap(String sql, Object[] args, int[] argTypes)
            throws DataAccessException {
        try {
            return template.queryForMap(sql, args, argTypes);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public <T> T queryForObject(String sql, Class<T> requiredType) throws DataAccessException {
        try {
            Map<String, Object> resultMap = this.queryForMap(sql);
            //使用了bean builder
            return BeanOperator.map2Object(resultMap, requiredType);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public long queryForLong(String sql) throws DataAccessException {
        try {
            return template.queryForLong(sql);
        } catch (EmptyResultDataAccessException e) {
            return 0;
        }
    }

    @Override
    public int queryForInt(String sql) throws DataAccessException {
        try {
            return template.queryForInt(sql);
        } catch (EmptyResultDataAccessException e) {
            return 0;
        }
    }

    @Override
    public int update(String sql) throws DataAccessException {
        return template.update(sql);
    }

    @Override
    public int[] batchUpdate(String[] sql) throws DataAccessException {
        return template.batchUpdate(sql);
    }

    @Override
    public <T> T queryForObject(String sql, Object[] args, Class<T> requiredType) throws DataAccessException {
        try {
            Map<String, Object> resultMap = this.queryForMap(sql, args);
            //使用了bean builder
            return BeanOperator.map2Object(resultMap, requiredType);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public <T> T queryForObject(String sql, Class<T> requiredType,
                                Object... args) throws DataAccessException {
        try {
            Map<String, Object> resultMap = this.queryForMap(sql, args);
            //使用了bean builder
            return BeanOperator.map2Object(resultMap, requiredType);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public long queryForLong(String sql, Object... args)
            throws DataAccessException {
        try {
            // TODO Auto-generated method stub
            return template.queryForLong(sql, args);
        } catch (EmptyResultDataAccessException e) {
            return 0;
        }
    }

    @Override
    public int queryForInt(String sql, Object... args)
            throws DataAccessException {
        try {
            // TODO Auto-generated method stub
            return template.queryForInt(sql, args);
        } catch (EmptyResultDataAccessException e) {
            return 0;
        }
    }

    @Override
    public int[] batchUpdate(String sql, List<Object[]> batchArgs,
                             int[] argTypes) {
        return template.batchUpdate(sql, batchArgs, argTypes);
    }

    public DataSource getDataSource() {
        return template.getDataSource();
    }

    @Override
    public <T> T queryForBean(String sql, Object[] parameter, Class<T> type) {
        try {
            return template.queryForObject(sql, parameter, BeanPropertyRowMapper.newInstance(type));
        } catch (DataAccessException e) {
            return null;
        }
    }
}
