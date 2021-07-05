package com.smallaswater.easysql.mysql.manager;

import cn.nukkit.plugin.Plugin;
import com.smallaswater.easysql.exceptions.MySqlLoginException;
import com.smallaswater.easysql.mysql.data.SqlDataManager;
import com.smallaswater.easysql.mysql.utils.Types;
import com.smallaswater.easysql.mysql.utils.UserData;
import lombok.Getter;

/**
 * 根据表名使用数据库
 *
 * @author SmallasWater
 */
public class UseTableSqlManager extends SqlManager {

    @Getter
    protected String tableName;

    public UseTableSqlManager(Plugin plugin, UserData data, String tableName) throws MySqlLoginException {
        super(plugin, data);
        this.tableName = tableName;
    }

    @Deprecated
    public SqlDataManager getSqlManager() {
        return super.getSqlDataManager(this.tableName);
    }

    public SqlDataManager getSqlDataManager() {
        return super.getSqlDataManager(this.tableName);
    }

    /**
     * 给表增加字段
     *
     * @param types 字段参数
     * @param args  字段名
     * @return 增加一个字段
     */
    public boolean createColumn(Types types, String args) {
        return super.createColumn(types, this.tableName, args);
    }

    @Deprecated //对于UseTableSqlManager来说不推荐使用此方法
    @Override
    public boolean createColumn(Types types, String form, String args) {
        return super.createColumn(types, form, args);
    }

    /**
     * 给表删除字段
     *
     * @param args 字段名
     * @return 删除一个字段
     */
    public boolean deleteColumn(String args) {
        return super.deleteColumn(args, this.tableName);
    }

    @Deprecated //对于UseTableSqlManager来说不推荐使用此方法
    @Override
    public boolean deleteColumn(String args, String form) {
        return super.deleteColumn(args, form);
    }

    public void deleteTable() {
        super.deleteTable(this.tableName);
    }

    @Deprecated //对于UseTableSqlManager来说不推荐使用此方法
    @Override
    public void deleteTable(String tableName) {
        super.deleteTable(tableName);
    }
}
