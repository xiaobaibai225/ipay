package com.pay.framework.db;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;


/**
 * DB 操作类，封装了所有db的操作
 *
 * @author houzhaowei
 */
public interface DBManager {

    public JdbcTemplate getTemplate();

    /**
     * 插入/更新/删除
     *
     * @param sql
     * @param args
     * @return 本次更新影响的行数
     */
    public int update(String sql, Object[] args);

    /**
     * 批量插入/更新/删除
     *
     * @param sql
     * @param batchArgs
     * @return 每一条的状态
     */
    public int[] batchUpdate(String sql, List<Object[]> batchArgs);

    /**
     * 查询列表
     *
     * @param sql
     * @param args
     * @return 查询的数据列表
     */
    public List<Map<String, Object>> queryForList(String sql, Object[] args);

    /**
     * 插入对象到db相应的表
     *
     * @param obj
     * @param clz
     * @return
     */
    public <T> int insertObject(T obj);

    /**
     * @param sql
     * @return
     * @throws DataAccessException
     */
    public List<Map<String, Object>> queryForList(String sql) throws DataAccessException;

    public <T> List<T> queryForListObject(String sql, Class<T> elementType) throws DataAccessException;

    public <T> List<T> queryForListObject(String sql, Object[] args, Class<T> elementType) throws DataAccessException;

    public <T> List<T> queryForListObject(String sql, Class<T> elementType, int rowNum, int limit) throws DataAccessException;


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
    public <T> List<T> queryForListObject(String sql, Object[] args, Class<T> elementType, int rowNum, int limit) throws DataAccessException;

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
    public <T> List<T> queryForListObject(String sql, Class<T> elementType, int rowNum, int limit, Object... args) throws DataAccessException;

    public <T> List<T> queryForListObject(String sql, Class<T> elementType, Object... args) throws DataAccessException;

    public Map<String, Object> queryForMap(String sql) throws DataAccessException;

    public Map<String, Object> queryForMap(String sql, Object... args) throws DataAccessException;

    public Map<String, Object> queryForMap(String sql, Object[] args, int[] argTypes) throws DataAccessException;

    public <T> T queryForObject(String sql, Class<T> requiredType) throws DataAccessException;

    public long queryForLong(String sql) throws DataAccessException;

    public int queryForInt(String sql) throws DataAccessException;

    public int update(String sql) throws DataAccessException;

    public int[] batchUpdate(String[] sql) throws DataAccessException;

    public <T> T queryForObject(String sql, Object[] args, Class<T> requiredType) throws DataAccessException;

    public <T> T queryForObject(String sql, Class<T> requiredType, Object... args) throws DataAccessException;

    public long queryForLong(String sql, Object... args) throws DataAccessException;

    public int queryForInt(String sql, Object... args) throws DataAccessException;

    public int[] batchUpdate(String sql, List<Object[]> batchArgs, int[] argTypes);

    public DataSource getDataSource();

    public <T> T queryForBean(String sql, Object[] parameter, Class<T> type);
}
