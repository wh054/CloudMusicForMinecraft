package team.info.ncmfm.entity;

/**
 * /login/qr/check 返回，轮询扫码状态。
 * code: 800 二维码过期 / 801 等待扫码 / 802 待确认 / 803 授权成功（带 cookie）
 */
public class QrCheck {
    private int code;
    private String message;
    private String cookie;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCookie() {
        return cookie;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }
}
