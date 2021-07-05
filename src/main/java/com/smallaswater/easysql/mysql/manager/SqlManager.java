package com.smallaswater.easysql.mysql.manager;


import cn.nukkit.plugin.Plugin;
import com.smallaswater.easysql.exceptions.MySqlLoginException;
import com.smallaswater.easysql.mysql.BaseMySql;
import com.smallaswater.easysql.mysql.utils.TableType;
import com.smallaswater.easysql.mysql.utils.UserData;
import org.jetbrains.annotations.NotNull;


/**
 * BaseMySql的实现类
 *
 * @author SmallasWater
 */
public class SqlManager extends BaseMySql {

    private boolean isEnable = false;

    public SqlManager(@NotNull Plugin plugin, @NotNull UserData data) throws MySqlLoginException {
        super(plugin, data);
        if (connect()) {
            this.isEnable = true;
        }
    }

    @Deprecated
    public SqlManager(@NotNull Plugin plugin, @NotNull UserData data, String configTableName, TableType... table) throws MySqlLoginException {
        this(plugin, data);
        if (this.isEnable) {
            if (configTableName != null && !configTableName.trim().equals("") && table.length > 0) {
                if (this.createTable(configTableName, BaseMySql.getDefaultTable(table))) {
                    plugin.getLogger().info("创建数据表" + configTableName + "成功");
                }
            }
        }
    }

    /**
     * 配置专用
     *
     * @param plugin 插件
     * @param data 用户配置
     * @param configTableName 数据库表名
     */
    @Deprecated
    public SqlManager(Plugin plugin, UserData data, String configTableName) throws MySqlLoginException {
        super(plugin, data);
        if (connect()) {
            if (createTable(configTableName, BaseMySql.getDefaultConfig())) {
                plugin.getLogger().info("创建数据表成功");
            }
        }
    }

    public boolean isEnable() {
        return isEnable;
    }

    public void disable() {
        PluginManager.getList().remove(this);
        this.shutdown();
        this.isEnable = false;
    }


}
