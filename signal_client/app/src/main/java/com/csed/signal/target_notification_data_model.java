package com.csed.signal;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class target_notification_data_model {
    @SerializedName("user_id")
    @Expose
    private String userId;

    @SerializedName("timestamp")
    @Expose
    private String timestamp;

    @SerializedName("q1_answer")
    @Expose
    private String Q1Answer;

    @SerializedName("q2_answer")
    @Expose
    private String Q2Answer;

    @SerializedName("click_more_cnt")
    @Expose
    private Integer clickMoreCnt;

    public target_notification_data_model(String userId, String timestamp, String Q1Answer, String Q2Answer, Integer clickMoreCnt)
    {
        this.userId = userId;
        this.timestamp = timestamp;
        this.Q1Answer = Q1Answer;
        this.Q2Answer = Q2Answer;
        this.clickMoreCnt = clickMoreCnt;
    }

}
