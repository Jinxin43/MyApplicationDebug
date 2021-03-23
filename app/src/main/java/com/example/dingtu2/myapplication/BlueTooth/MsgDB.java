package com.example.dingtu2.myapplication.BlueTooth;


import com.DingTu.Base.PubVar;
import com.DingTu.Base.Tools;
import com.DingTu.Dataset.ASQLiteDatabase;
import com.DingTu.Dataset.SQLiteDataReader;

import java.util.ArrayList;
import java.util.List;


public class MsgDB {
    private android.database.sqlite.SQLiteDatabase SQLiteDatabase;
    String configPath = PubVar.m_SysAbsolutePath + "/SysFile/Config.dbx";
    private ASQLiteDatabase m_SQLiteDatabase;
//    //用户参数配置表
    private v1_UserConfigDB_UserParam m_UserParam = null;

    public MsgDB() {
        if (this.CheckAndCreateTable("BeidouMessage")) {
            this.m_UserParam = new v1_UserConfigDB_UserParam();
            this.m_UserParam.SetBindDB(this.GetSQLiteDatabase());
        }
        SQLiteDatabase = SQLiteDatabase.openOrCreateDatabase(configPath, null);
    }

    /**
     * 创建指定名称的表
     *
     * @param TableName
     * @return
     */
    private boolean CheckAndCreateTable(String TableName) {
        if (this.IsExistTable(TableName)) return true;
        else {
            //创建表
            List<String> createSQL = new ArrayList<String>();
            createSQL.add("CREATE TABLE " + TableName + " (");
            createSQL.add("ID integer primary key autoincrement  not null default (0),");
            createSQL.add("FromId" + " text,");
            createSQL.add("ToId" + " text,");
            createSQL.add("Msg" + " text,");
            createSQL.add("MsgType" + " text,");
            createSQL.add("Time" + " text");
            createSQL.add(")");
            String SQL = Tools.JoinT("\r\n", createSQL);
            return this.GetSQLiteDatabase().ExcuteSQL(SQL);
        }
    }

    /**
     * 检查指定的表是否在存在
     *
     * @param TableName
     * @return
     */
    private boolean IsExistTable(String TableName) {
        try {
            String SQL = "SELECT COUNT(*) as count FROM sqlite_master WHERE type='table' and name= '" + TableName + "'";
            SQLiteDataReader DR = this.GetSQLiteDatabase().Query(SQL);
            if (DR == null) return false;
            int Count = 0;
            if (DR.Read()) Count = Integer.parseInt(DR.GetString("count"));
            DR.Close();
            if (Count > 0) return true;
            else return false;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return false;


    }

    /**
     * 得到指定的数据库操作类
     *
     * @return
     */
    private ASQLiteDatabase GetSQLiteDatabase() {
        if (this.m_SQLiteDatabase == null) this.OpenDatabase();
        return this.m_SQLiteDatabase;
    }

    //打开配置数据库
    private void OpenDatabase() {
        if (Tools.ExistFile(configPath)) {
            this.m_SQLiteDatabase = new ASQLiteDatabase();
            this.m_SQLiteDatabase.setDatabaseName(configPath);
        }
    }

    public List<Msg> readReDB() {
        List<Msg> msgList = new ArrayList<Msg>();
        if (msgList != null && msgList.size() > 0) {
            msgList.clear();
        }
        String result = "";
        try {
            String readSql = "select * from BeidouMessage where MsgType='RE' ";
            SQLiteDataReader reader = new SQLiteDataReader(SQLiteDatabase.rawQuery(readSql, null));
            while (reader.Read()) {
                if (reader.GetCount() > 0) {
                    Msg msg = new Msg();
                    msg.setFromId(reader.GetString("FromId"));
                    msg.setMsg(reader.GetString("Msg"));
                    msg.setMsgType(reader.GetString("MsgType"));
                    msg.setTime(reader.GetString("Time"));
                    msgList.add(msg);

                }
            }
            reader.Close();
        } catch (Exception ex) {
            Tools.ShowMessageBox(PubVar.m_DoEvent.m_Context, ex.getMessage());

        }

        return msgList;
    }


    public List<Msg> readSeDB() {
        List<Msg> msgList = new ArrayList<Msg>();
        if (msgList != null && msgList.size() > 0) {
            msgList.clear();
        }
        String result = "";
        try {
            String readSql = "select * from BeidouMessage where MsgType='SE' ";
            SQLiteDataReader reader = new SQLiteDataReader(SQLiteDatabase.rawQuery(readSql, null));
            while (reader.Read()) {

                if (reader.GetCount() > 0) {
                    Msg msg = new Msg();
                    msg.setToId(reader.GetString("ToId"));
                    msg.setMsg(reader.GetString("Msg"));
                    msg.setMsgType(reader.GetString("MsgType"));
                    msg.setTime(reader.GetString("Time"));
                    msgList.add(msg);
                }
            }
            reader.Close();
        } catch (Exception ex) {
            Tools.ShowMessageBox(PubVar.m_DoEvent.m_Context, ex.getMessage());

        }

        return msgList;
    }

    public boolean SaveMsgDB(String Id, String msg, String msgtype, String time) {
        try {
            String sql = "insert into BeidouMessage(FromId,ToId,Msg,MsgType,Time) values " + "('%1$s','%2$s','%3$s','%4$s','%5$s')";
            sql = String.format(sql, Id, Id, msg, msgtype, time);
            SQLiteDatabase.execSQL(sql);
            SQLiteDatabase.close();
        } catch (Exception ex) {
            Tools.ShowMessageBox(PubVar.m_DoEvent.m_Context, ex.getMessage());
            return false;
        }

        return true;
    }

}
