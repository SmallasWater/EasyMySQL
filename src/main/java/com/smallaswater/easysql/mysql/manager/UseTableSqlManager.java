package com.smallaswater.easysql.mysql.manager;

import cn.nukkit.plugin.Plugin;
import com.smallaswater.easysql.exceptions.MySqlLoginException;
import com.smallaswater.easysql.mysql.BaseMySql;
import com.smallaswater.easysql.mysql.data.SqlDataManager;
import com.smallaswater.easysql.mysql.utils.Types;
import com.smallaswater.easysql.mysql.utils.UserData;
import lombok.Getter;

/**
 * 根据表名使用数据库
 *
 * @author SmallasWater
 */
public class UseTableSqlManager extends BaseMySql {

    @Getter
    protected String tableName;

    public UseTableSqlManager(Plugin plugin, UserData data, String tableName) throws MySqlLoginException {
        super(plugin, data);
        connect();
        this.tableName = tableName;
    }

    public SqlDataManager getSqlManager() {
        return super.getSqlManager(this.tableName);
    }

    /**
     * 给表增加字段
     *
     * @param types 字段参数
     * @param args  字段名
     * @return 增加一个字段
     */
    public boolean createColumn(Types types, String args) {
        return this.createColumn(types, this.tableName, args);
    }

    /**
     * 给表删除字段
     *
     * @param args 字段名
     * @return 删除一个字段
     */
    public boolean deleteColumn(String args) {
        return this.deleteColumn(args, this.tableName);
    }

    public void deleteTable() {
        this.deleteTable(this.tableName);
    }

}
