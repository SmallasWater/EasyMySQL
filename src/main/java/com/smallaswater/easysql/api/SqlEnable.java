package com.smallaswater.easysql.api;


import cn.nukkit.plugin.Plugin;
import com.smallaswater.easysql.exceptions.MySqlLoginException;
import com.smallaswater.easysql.mysql.manager.SqlManager;
import com.smallaswater.easysql.mysql.utils.TableType;
import com.smallaswater.easysql.mysql.utils.UserData;
import org.jetbrains.annotations.NotNull;


/**
 * <p>
 * mysql工具类 使用的时候 实例化本类
 *
 * </p>
 *
 * @author 楠木i, 若水
 */
public class SqlEnable {

    private final SqlManager manager;
    private final UserData data;
    private final boolean isEnable;

    public SqlEnable(@NotNull Plugin plugin, @NotNull String tableName, UserData data, TableType... table) throws MySqlLoginException {
        this.data = data;
        this.manager = new SqlManager(plugin, data, tableName, table);
        this.isEnable = this.manager.isEnable();
    }

    public boolean isEnable() {
        return isEnable;
    }

    /**
     * 关闭服务器的时候记得执行这个
     */
    public void disable() {
        manager.shutdown();
    }

    public UserData getData() {
        return data;
    }

    public SqlManager getManager() {
        return manager;
    }
}
