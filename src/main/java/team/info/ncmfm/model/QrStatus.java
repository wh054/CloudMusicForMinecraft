package team.info.ncmfm.model;

/**
 * 二维码扫码登录状态，供登录界面轮询展示。
 */
public enum QrStatus {
    /** 等待扫码（801） */
    WAITING,
    /** 已扫描，等待手机确认（802） */
    SCANNED,
    /** 授权成功并已完成登录（803） */
    CONFIRMED,
    /** 二维码已过期（800），需要刷新 */
    EXPIRED,
    /** 请求出错（网络等） */
    ERROR
}
