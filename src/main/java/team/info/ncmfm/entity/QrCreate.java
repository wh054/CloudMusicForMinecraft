package team.info.ncmfm.entity;

/**
 * /login/qr/create?qrimg=true 返回，包含二维码图片（base64 PNG）与跳转地址
 */
public class QrCreate {
    private int code;
    private Data data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public static class Data {
        private String qrurl;
        private String qrimg;

        public String getQrurl() {
            return qrurl;
        }

        public void setQrurl(String qrurl) {
            this.qrurl = qrurl;
        }

        public String getQrimg() {
            return qrimg;
        }

        public void setQrimg(String qrimg) {
            this.qrimg = qrimg;
        }
    }
}
