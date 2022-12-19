package com.csed.signal;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.HashMap;

public class emotion_data_model {
    @SerializedName("user_id")
    @Expose
    private String userId;

    @SerializedName("emotion_data")
    @Expose
    private ArrayList<HashMap<String, Float>> emotionData;

    @SerializedName("error")
    @Expose
    private String error;

    public emotion_data_model(ArrayList<HashMap<String, Float>> emotionData, String error)
    {
        this.emotionData = emotionData;
        this.error = error;
    }

    public HashMap<String, Float> getEmotionData () {
        HashMap<String, Float> latestEmotionChange;
        if (this.emotionData.isEmpty()) {
            latestEmotionChange = null;
        } else {
            latestEmotionChange = this.emotionData.get(this.emotionData.size() - 1);
        }
        return latestEmotionChange;
    }

    public String getError () {
        return this.error;
    }
}
