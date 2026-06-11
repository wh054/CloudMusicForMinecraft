package team.info.ncmfm.entity;

public class LoginStatus {
    private Data data;

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public static class Data {
        private int code;
        private LoginInfo.AccountBean account;

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public LoginInfo.AccountBean getAccount() {
            return account;
        }

        public void setAccount(LoginInfo.AccountBean account) {
            this.account = account;
        }
    }
}
