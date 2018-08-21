package com.vunke.auth.modle;



import java.util.List;

/**
 * Created by zhuxi on 2017/1/17.
 */
public class GroupStrategy {


    /**
     * ApkPath :
     * EPGcode : 1
     * EPGpackage : com.xxxx.xxxx
     * GroupAddress : 湖南
     * GroupName : 联创分组
     * GroupStatus : 0
     * GroupType :
     * GrpupNumber : 2a000025
     */

    private List<GroupStrategyBean> json;

    public List<GroupStrategyBean> getJson() {
        return json;
    }

    @Override
    public String toString() {
        return "GroupStrategy{" +
                "json=" + json +
                '}';
    }

    public void setJson(List<GroupStrategyBean> json) {
        this.json = json;
    }

    public static class GroupStrategyBean {
        private String ApkPath;
        private String EPGcode;
        private String EPGpackage;
        private String GroupAddress;
        private String GroupName;
        private String GroupStatus;
        private String GroupType;
        private String GrpupNumber;
        private String UserId;
        @Override
        public String toString() {
            return "JsonBean{" +
                    "ApkPath='" + ApkPath + '\'' +
                    ", EPGcode='" + EPGcode + '\'' +
                    ", EPGpackage='" + EPGpackage + '\'' +
                    ", GroupAddress='" + GroupAddress + '\'' +
                    ", GroupName='" + GroupName + '\'' +
                    ", GroupStatus='" + GroupStatus + '\'' +
                    ", GroupType='" + GroupType + '\'' +
                    ", GrpupNumber='" + GrpupNumber + '\'' +
                    '}';
        }
        public String getUserId() {
            return UserId;
        }

        public void setUserId(String userId) {
            UserId = userId;
        }
        public String getApkPath() {
            return ApkPath;
        }

        public void setApkPath(String ApkPath) {
            this.ApkPath = ApkPath;
        }

        public String getEPGcode() {
            return EPGcode;
        }

        public void setEPGcode(String EPGcode) {
            this.EPGcode = EPGcode;
        }

        public String getEPGpackage() {
            return EPGpackage;
        }

        public void setEPGpackage(String EPGpackage) {
            this.EPGpackage = EPGpackage;
        }

        public String getGroupAddress() {
            return GroupAddress;
        }

        public void setGroupAddress(String GroupAddress) {
            this.GroupAddress = GroupAddress;
        }

        public String getGroupName() {
            return GroupName;
        }

        public void setGroupName(String GroupName) {
            this.GroupName = GroupName;
        }

        public String getGroupStatus() {
            return GroupStatus;
        }

        public void setGroupStatus(String GroupStatus) {
            this.GroupStatus = GroupStatus;
        }

        public String getGroupType() {
            return GroupType;
        }

        public void setGroupType(String GroupType) {
            this.GroupType = GroupType;
        }

        public String getGrpupNumber() {
            return GrpupNumber;
        }

        public void setGrpupNumber(String GrpupNumber) {
            this.GrpupNumber = GrpupNumber;
        }
    }
}
