package team.info.ncmfm.entity;

/**
 * /login/qr/key 返回，用于获取二维码 unikey
 */
public class QrKey {
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
        private int code;
        private String unikey;

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getUnikey() {
            return unikey;
        }

        public void setUnikey(String unikey) {
            this.unikey = unikey;
        }
    }
}
