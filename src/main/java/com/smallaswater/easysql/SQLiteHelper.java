package com.smallaswater.easysql;

import com.smallaswater.easysql.mysql.data.SqlData;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * @author Sobadfish
 */
public class SQLiteHelper {

    private Connection connection;

    private Statement statement;

    private final String dbFilePath;

    /**
     * 构造函数
     *
     * @param dbFilePath sqlite db 文件路径
     */
    public SQLiteHelper(String dbFilePath) throws ClassNotFoundException, SQLException {
        this.dbFilePath = dbFilePath;
        this.connection = getConnection(dbFilePath);
    }

    /**
     * 获取数据库连接
     *
     * @param dbFilePath db文件路径
     * @return 数据库连接
     */
    private Connection getConnection(String dbFilePath) throws ClassNotFoundException, SQLException {
        Connection conn = null;
        // 1、加载驱动
        Class.forName("org.sqlite.JDBC");
        // 2、建立连接
        // 注意：此处有巨坑，如果后面的 dbFilePath 路径太深或者名称太长，则建立连接会失败
        conn = DriverManager.getConnection("jdbc:sqlite:" + dbFilePath);
        return conn;
    }


    public boolean exists(String table) {
        try {
            getStatement().executeQuery(
                    "select * from " + table
            );
            return true;
        } catch (Exception e) {
            return false;
        }

    }

    public void addTable(String tableName, DBTable tables) {
        if (!exists(tableName)) {
            String sql = "create table " + tableName + "(" + tables.asSql() + ")";
            try {
                getStatement().executeQuery(sql);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String getDbFilePath() {
        return dbFilePath;
    }

    /**
     * 增加数据
     */
    public <T> void add(String tableName, T values) {
        try {
            if (statement != null) {
                SqlData sqlData = SqlData.classToSqlData(values);
                add(tableName, sqlData);
            }
        } catch (Exception ignore) {

        }
    }


    /**
     * 增加数据
     */
    public SQLiteHelper add(String tableName, SqlData values) {
        try {
            if (statement != null) {
                String sql = "insert into " + tableName + "(" + values.getColumnToString() + ") values (" + values.getObjectToString() + ")";
                statement.execute(sql);
            }
        } catch (Exception ignore) {
        }
        return this;
    }


    /**
     * 删除数据
     */
    public SQLiteHelper remove(String tableName, int id) {
        try {
            if (statement != null) {
                String sql = "delete from " + tableName + " where id = " + id;
                statement.execute(sql);
            }
        } catch (Exception ignore) {
        }
        return this;
    }

    public SQLiteHelper remove(String tableName, String key, String value) {
        try {
            if (statement != null) {
                String sql = "delete from " + tableName + " where " + key + " = '" + value + "'";
                statement.execute(sql);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    public SQLiteHelper removeAll(String tableName) {
        try {
            if (statement != null) {
                String sql = "delete from " + tableName;
                statement.execute(sql);
            }
        } catch (Exception ignore) {
        }
        return this;
    }


    public <T> SQLiteHelper set(String tableName, T values) {
        SqlData contentValues = SqlData.classToSqlDataAsId(values);
        if (contentValues.getInt("id") == -1) {
            throw new NullPointerException("无 id 信息");
        }
        return set(tableName, contentValues.getInt("id"), contentValues);
    }

    public <T> SQLiteHelper set(String tableName, String key, String value, T values) {
        SqlData sqlData = SqlData.classToSqlData(values);
        return set(tableName, key, value, sqlData);
    }


    /**
     * 更新数据
     */
    public SQLiteHelper set(String tableName, int id, SqlData values) {
        try {
            if (statement != null) {
                statement.execute("update " + tableName + " set " + values.toUpdateValue() + " where id = " + id);
            }

        } catch (Exception ignore) {
        }

        return this;
    }

    /**
     * 更新数据
     */
    public SQLiteHelper set(String tableName, String key, String value, SqlData values) {

        try {
            if (statement != null) {
                statement.execute("update " + tableName + " set " + values.toUpdateValue() + " where " + key + " = " + value);
            }

        } catch (Exception ignore) {
        }

        return this;

    }

    public <T> SQLiteHelper set(String tableName, SqlData key, T values) {
        SqlData sqlData = SqlData.classToSqlData(values);
        try {
            if (statement != null) {

                String sql = "update " + tableName + " set " + sqlData.toUpdateValue() + " where " + getUpDataWhere(key);
                PreparedStatement statement = connection.prepareStatement(sql);

                int i = 1;
                for (Object type : key.getObjects()) {
                    statement.setString(i, type.toString());
                    i++;
                }
                statement.execute();

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return this;

//        return  set(tableName,key,value,sqlData);
    }


    public boolean hasData(String tableName, String key, String value) {
        try {
            if (statement != null) {
                ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM " + tableName + " WHERE " + key + " = '" + value + "'");
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    resultSet.close();
                    return count > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private String getUpDataWhere(SqlData data) {
        StringBuilder builder = new StringBuilder();
        for (String column : data.getData().keySet()) {
            builder.append(column).append(" = ? and");
        }
        String str = builder.toString();
        return str.substring(0, str.length() - 3);
    }

    public <T> T get(String tableName, int id, Class<T> clazz) {
        T instance = null;
        try {
            String query = "SELECT * FROM " + tableName + " WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();

            instance = explainClass(resultSet, clazz, clazz.newInstance());
            resultSet.close();
        } catch (SQLException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return instance;

    }

    public <T> T get(String tableName, String key, String value, Class<T> clazz) {
        T instance = null;
        try {
            // 准备 SQL 查询语句
            String query = "SELECT * FROM " + tableName + " WHERE " + key + " = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, value);

            ResultSet resultSet = statement.executeQuery();
            T t = clazz.newInstance();

            explainClass(resultSet, clazz, t);
            resultSet.close();

        } catch (SQLException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return instance;

    }

    public <T> LinkedList<T> getDataByString(String tableName, String selection, String[] key, Class<T> clazz) {
        LinkedList<T> datas = new LinkedList<>();
        try {
            // 准备 SQL 查询语句
            String query = "SELECT * FROM " + tableName + " WHERE " + selection;
            PreparedStatement statement = connection.prepareStatement(query);

            // 设置查询条件
            for (int i = 0; i < key.length; i++) {
                statement.setString(i + 1, key[i]);
            }

            // 执行查询
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                T t = clazz.newInstance();
                datas.add(explainClass(resultSet, clazz, t));
            }
            resultSet.close();

        } catch (SQLException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return datas;
    }


    public <T> LinkedList<T> getAll(String tableName, Class<T> clazz) {
        LinkedList<T> datas = new LinkedList<>();
        try {
            // 准备 SQL 查询语句
            String query = "SELECT * FROM " + tableName;
            PreparedStatement statement = connection.prepareStatement(query);

            // 执行查询
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                T t = clazz.newInstance();
                datas.add(explainClass(resultSet, clazz, t));
            }

            resultSet.close();

        } catch (SQLException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return datas;
    }

    private <T> T explainClass(ResultSet cursor, Class<?> tc, T t) {
        try {
            ResultSetMetaData rsmd = cursor.getMetaData();
            for (int i = 0; i < rsmd.getColumnCount(); i++) {
                String name = rsmd.getColumnName(i + 1);
                Field field = tc.getField(name);
                if (field.getType() == int.class) {
                    field.set(t, cursor.getInt(name));
                } else if (field.getType() == float.class || field.getType() == double.class) {
                    field.set(t, cursor.getFloat(name));
                } else if (field.getType() == boolean.class) {
                    field.set(t, Boolean.valueOf(cursor.getString(name)));

                } else if (field.getType() == long.class) {
                    field.set(t, cursor.getLong(name));

                } else {
                    field.set(t, cursor.getString(name));
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return t;
    }


    public static class DBTable {
        LinkedHashMap<String, String> tables = new LinkedHashMap<>();

        public DBTable(String key, String value) {
            tables.put(key, value);
        }

        public DBTable(Map<String, String> m) {
            System.out.println(m);
            tables.putAll(m);
        }

        public DBTable put(String key, String value) {
            tables.put(key, value);
            return this;
        }

        public String asSql() {
            StringBuilder s = new StringBuilder();
            for (Map.Entry<String, String> e : tables.entrySet()) {
                s.append(e.getKey()).append(" ").append(e.getValue()).append(",");
            }
            return s.substring(0, s.length() - 1);

        }

        public static DBTable asDbTable(Class<?> t) {
            Field[] fields = t.getFields();
            LinkedHashMap<String, String> stringStringLinkedHashMap = new LinkedHashMap<>();
            boolean isId = false;
            // 先找自增id
            for (Field field : fields) {
                if ("id".equalsIgnoreCase(field.getName()) && field.getType() == long.class) {
                    //找到了
                    isId = true;
                    break;
                }
            }
            if (!isId) {
                throw new NullPointerException("数据库类需要一个id");
            }
            stringStringLinkedHashMap.put("id", "integer primary key autoincrement");
            for (Field field : fields) {
                if ("id".equalsIgnoreCase(field.getName()) && field.getType() == int.class) {
                    //找到了
                    continue;
                }
                if (field.getType() == float.class || field.getType() == double.class) {
                    stringStringLinkedHashMap.put(field.getName().toLowerCase(), field.getType().getName());
                } else {
                    stringStringLinkedHashMap.put(field.getName().toLowerCase(), "varchar(20)");
                }
            }
            return new DBTable(stringStringLinkedHashMap);

        }

    }


    private Connection getConnection() throws ClassNotFoundException, SQLException {
        if (null == connection) {
            connection = getConnection(dbFilePath);
        }
        return connection;
    }

    private Statement getStatement() throws SQLException, ClassNotFoundException {
        if (null == statement) {
            statement = getConnection().createStatement();
        }
        return statement;
    }

    public void destroyed() {
        this.close();
    }

    /**
     * 数据库资源关闭和释放
     */
    public void close() {
        try {
            if (null != connection) {
                connection.close();
                connection = null;
            }

            if (null != statement) {
                statement.close();
                statement = null;
            }

        } catch (SQLException e) {
            System.out.println("Sqlite数据库关闭时异常 " + e);
        }
    }

}
