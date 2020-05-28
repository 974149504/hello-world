/**
 * @ClassName AttendanceInfo
 * @Author ShiHaiLin
 * @Date 2020/5/11 14:31
 * @Description
 */
public class AttendanceInfo {
    private String id;

    /**
     * 上班考勤时间
     */
    private String workTime;

    /**
     * 上班打卡时间
     */
    private String clockInTime;

    /**
     * 上班打卡状态：1：正常 2：迟到 3：缺勤
     */
    private Integer clockInStatus;

    /**
     * 上班打卡抓拍图片URL
     */
    private String clockInFileUrl;


    /**
     * 下班考勤时间
     */
    private String closeTime;

    /**
     * 下班打卡时间
     */
    private String clockOutTime;

    /**
     * 下班打卡状态： 1：正常 2：早退 3：缺勤
     */
    private Integer clockOutStatus;

    /**
     * 下班打卡抓拍图片URL
     *
     */
    private String clockOutFileUrl;


    private String clockInDev;

    private String clockOutDev;

    private String clockUserId;

    private Integer clockStatus;

    private String remarks;

    private String createTime;

    private String updateTime;
}
