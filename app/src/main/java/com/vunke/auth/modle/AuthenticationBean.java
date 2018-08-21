package com.vunke.auth.modle;

/**
 * Created by zhuxi on 2017/10/20.
 */
public class AuthenticationBean {
    private String user_id;
    private int Auth_code;
    private String create_time;
    private String Error_code;
    private String Error_Info;

    public String getError_Info() {
        return Error_Info;
    }

    public void setError_Info(String error_Info) {
        Error_Info = error_Info;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public int getAuth_code() {
        return Auth_code;
    }

    public void setAuth_code(int auth_code) {
        Auth_code = auth_code;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public String getError_code() {
        return Error_code;
    }

    public void setError_code(String error_code) {
        Error_code = error_code;
    }

    @Override
    public String toString() {
        return "AuthenticationBean{" +
                "user_id='" + user_id + '\'' +
                ", Auth_code=" + Auth_code +
                ", create_time='" + create_time + '\'' +
                ", Error_code='" + Error_code + '\'' +
                ", Error_Info='" + Error_Info + '\'' +
                '}';
    }
}
