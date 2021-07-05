package com.smallaswater.easysql.mysql;


import cn.nukkit.plugin.Plugin;
import com.smallaswater.easysql.EasySql;
import com.smallaswater.easysql.exceptions.MySqlLoginException;
import com.smallaswater.easysql.mysql.data.SqlDataManager;
import com.smallaswater.easysql.mysql.manager.PluginManager;
import com.smallaswater.easysql.mysql.utils.*;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 数据库基类文件
 *
 * @author 楠木i，若水
 */
public abstract class BaseMySql {

    private final UserData data;

    private final Plugin plugin;

    protected LoginPool pool;

    public BaseMySql(@NotNull Plugin plugin, @NotNull UserData data) {
        this.data = data;
        this.plugin = plugin;
    }

    public static String getDefaultConfig() {
        return getDefaultTable(new TableType("name", Types.VARCHAR), new TableType("config", Types.TEXT));
    }

    public static String getDefaultTable(TableType... type) {
        StringBuilder builder = new StringBuilder();
        int i = 1;
        for (TableType type1 : type) {
            builder.append(type1.toTable());
            if (i != type.length) {
                builder.append(",");
            }
            i++;
        }
        return builder.toString();
    }

    /**
     * 连接数据库
     *
     * @return 是否链接成功
     * @throws MySqlLoginException 连接错误
     */
    protected boolean connect() throws MySqlLoginException {
        Connection connection = null;

        try {
            this.pool = EasySql.getLoginPool(data);

            Class.forName("com.mysql.cj.jdbc.Driver");
            this.pool.dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
            this.pool.dataSource.setUrl("jdbc:mysql://" + this.data.getHost() + ':' + this.data.getPort() + '/' + this.data.getDatabase() + "?&autoReconnect=true&failOverReadOnly=false&serverTimezone=GMT&characterEncoding=utf8&useSSL=false");
            this.pool.dataSource.setUsername(this.data.getUser());
            this.pool.dataSource.setPassword(this.data.getPassWorld());
            this.pool.dataSource.setInitialSize(3);
            this.pool.dataSource.setMinIdle(1);
            this.pool.dataSource.setMaxActive(30);
            this.pool.dataSource.setValidationQuery("SELECT 1");
            this.pool.dataSource.setTimeBetweenEvictionRunsMillis(1800);

            connection = this.getConnection();
            if (connection != null) {
                plugin.getLogger().info("已连接数据库");
                PluginManager.connect(plugin, this);
                return true;
            } else {
                plugin.getLogger().info("无法连接数据库");
            }
        } catch (Exception e) {
            e.printStackTrace();
            plugin.getLogger().info("连接数据库出现异常...");
        } finally {

            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        throw new MySqlLoginException();

    }

    public Connection getConnection() {
        try {
            return pool.dataSource.getConnection();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取 manager
     */
    @Deprecated
    public SqlDataManager getSqlManager(String form) {
        return this.getSqlDataManager(form);
    }

    public SqlDataManager getSqlDataManager(String form) {
        return new SqlDataManager(this.data.getDatabase(), form, this.getConnection());
    }


    /**
     * 关闭数据库连接
     */
    public void shutdown() {
        try {
            this.pool.dataSource.getConnection().close();
            this.plugin.getLogger().info(" 已断开数据库连接");
        } catch (SQLException var2) {
            var2.printStackTrace();
        }
    }

    /**
     * 是否存在表
     *
     * @param tableName 表名称
     * @return 是否存在
     */
    public boolean isExistForm(String tableName) {
        try {
            ResultSet resultSet = this.getConnection().getMetaData().getTables(null, null, tableName, null);
            return resultSet.next();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * 创建表单 SQL指令 每个args代表指令中的 ?
     *
     * @param args       参数
     * @return 是否创建成功
     */
    public boolean createTable(String... args) {
        String command = "CREATE TABLE " + args[0] + "(?)engine=InnoDB default charset=utf8";
        try {
            ResultSet resultSet = this.getConnection().getMetaData().getTables(null, null, args[0], null);
            if (!resultSet.next()) {
                return this.getSqlDataManager(args[0]).runSql(command, new ChunkSqlType(1, args[1]));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Deprecated
    public boolean createTable(Connection connection, String... args) {
        String command = "CREATE TABLE " + args[0] + "(?)engine=InnoDB default charset=utf8";
        try {
            ResultSet resultSet = connection.getMetaData().getTables(null, null, args[0], null);
            if (!resultSet.next()) {
                return getSqlManager(args[0]).runSql(command, new ChunkSqlType(1, args[1]));
            }
        } catch (SQLException var5) {
            var5.printStackTrace();
        }

        return false;
    }

    /**
     * 删除表
     *
     * @param tableName 表名称
     */
    public void deleteTable(String tableName) {
        String sql = "DROP TABLE " + tableName;
        this.getSqlDataManager("").runSql(sql);
    }

    @Deprecated
    public void deleteField(String tableName) {
        this.deleteTable(tableName);
    }

    /**
     * 是否存在字段
     *
     * @param table  表名
     * @param column 字段名
     * @return 是否存在
     */
    public boolean isExistColumn(String table, String column) {
        try {
            ResultSet resultSet = this.getConnection().getMetaData().getColumns(null, null, table, column);
            return resultSet.next();
        } catch (SQLException e) {
            return false;
        }
    }

    @Deprecated
    public boolean isColumn(String table, String column) {
        return this.isExistColumn(table, column);
    }

    /**
     * 给表增加字段
     *
     * @param form  表单名
     * @param types 字段参数
     * @param args  字段名
     * @return 增加一个字段
     */
    public boolean createColumn(Types types, String form, String args) {
        String command = "ALTER TABLE " + form + " ADD " + args + " " + types.toString();
        return this.getSqlDataManager(form).runSql(command);
    }

    /**
     * 给表删除字段
     *
     * @param args 字段名
     * @param form 表单名称
     * @return 删除一个字段
     */
    public boolean deleteColumn(String args, String form) {
        String command = "ALTER TABLE " + form + " DROP ?";
        return this.getSqlDataManager(form).runSql(command, new ChunkSqlType(1, args));
    }

    /**
     * 获取数据条数
     */
    public int getDataSize(String sql, String form) {
        int i = 0;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = this.getConnection().prepareStatement("SELECT COUNT(*) FROM " + form + " " + sql);
            resultSet = preparedStatement.executeQuery();
            if (resultSet != null) {
                if (resultSet.next()) {
                    i = resultSet.getInt(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return i;
    }

}
