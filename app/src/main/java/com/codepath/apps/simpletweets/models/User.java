package com.codepath.apps.simpletweets.models;

import com.codepath.apps.simpletweets.utils.MyDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

@Table(database = MyDatabase.class)
public class User extends BaseModel implements Serializable {
    @PrimaryKey
    @Column
    long uid;
    @Column
    String name;
    @Column
    String screenName;
    @Column
    public String profileImageUrl;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getScreenName() {
        return screenName;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public static User fromArray(JSONObject jsonObject) {
        User user = new User();
        try {
            user.name = jsonObject.getString("name");
            user.uid = jsonObject.getLong("id");
            user.screenName = jsonObject.getString("screen_name");
            user.profileImageUrl = jsonObject.getString("profile_image_url");
            user.save();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return user;
    }

}
