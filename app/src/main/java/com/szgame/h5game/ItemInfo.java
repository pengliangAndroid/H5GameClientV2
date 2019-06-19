package com.szgame.h5game;

/**
 * @author pengl
 */
public class ItemInfo {

    /**
     * extends_info_data :
     * game_area : 1
     * game_level : 7
     * game_price : 6
     * game_role_id : 524429
     * game_role_name : 呼延迎曼
     * notify_id : -1
     * subject : 元宝
     * game_guid : 9181617
     * game_sign :
     * game_orderid : 13-7-524429-1-xiaoqi-1551863750
     */

    private String amount;
    private String paytime;
    private int goodId;
    private String goodName;
    private int roleId;
    private String roleName;
    private int serverId;
    private String serverName;
    private String notifyUrl;
    private String extension;
    private String outTradeNo;
    private int rolelv;

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getPaytime() {
        return paytime;
    }

    public void setPaytime(String paytime) {
        this.paytime = paytime;
    }

    public int getGoodId() {
        return goodId;
    }

    public void setGoodId(int goodId) {
        this.goodId = goodId;
    }

    public String getGoodName() {
        return goodName;
    }

    public void setGoodName(String goodName) {
        this.goodName = goodName;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getOutTradeNo() {
        return outTradeNo;
    }

    public void setOutTradeNo(String outTradeNo) {
        this.outTradeNo = outTradeNo;
    }

    public int getRolelv() {
        return rolelv;
    }

    public void setRolelv(int rolelv) {
        this.rolelv = rolelv;
    }
}
