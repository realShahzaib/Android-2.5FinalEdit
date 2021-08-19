package com.hybridproduct.apps.nearbystores.parser.api_parser;


import com.hybridproduct.apps.nearbystores.appconfig.AppContext;
import com.hybridproduct.apps.nearbystores.classes.Images;
import com.hybridproduct.apps.nearbystores.classes.User;
import com.hybridproduct.apps.nearbystores.parser.Parser;
import com.hybridproduct.apps.nearbystores.parser.tags.Tags;

import org.json.JSONException;
import org.json.JSONObject;

import io.realm.RealmList;

public class UserParser extends Parser {

    public UserParser(JSONObject json) {
        super(json);
    }

    public RealmList<User> getUser() {

        RealmList<User> list = new RealmList<User>();

        try {

            JSONObject json_array = json.getJSONObject(Tags.RESULT);

            for (int i = 0; i < json_array.length(); i++) {

                JSONObject json_user = json_array.getJSONObject(i + "");
                User user = new User();

                user.setId(json_user.getInt("id_user"));


                try {
                    user.setName(json_user.getString("name"));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                try {
                    user.setStatus(json_user.getString("status"));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }


                try {
                    user.setDistance(json_user.getDouble("distance"));
                } catch (Exception e) {
                    e.printStackTrace();
                }


                user.setUsername(json_user.getString("username"));
                user.setEmail(json_user.getString("email"));
                user.setPhone(json_user.getString("telephone"));
                user.setType(User.TYPE_LOGGED);

                try {
                    user.setAuth(json_user.getString("typeAuth"));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                try {
                    user.setCountry(json_user.getString("country_name"));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (json_user.has("token")) {
                    user.setToken(json_user.getString("token"));
                }

                try {
                    user.setBlocked(json_user.getBoolean("blocked"));
                } catch (Exception e) {
                    e.printStackTrace();
                }


                try {
                    user.setJob(json_user.getString("job"));
                } catch (Exception e) {
                    e.printStackTrace();
                }


                try {
                    user.setOnline(json_user.getBoolean("is_online"));
                } catch (Exception e) {
                    user.setOnline(false);
                }


                try {

                    //Log.e("imageParser",json_user.toString());
                    if (json_user.has("images") && json_user.isNull("images")) {
                        if (json_user.getJSONObject("images") != null) {
                            ImagesParser imgp = new ImagesParser(json_user.getJSONObject("images"));
                            if (imgp.getImagesList().size() > 0) {

                                RealmList<Images> listImages = imgp.getImagesList();
                                for (int j = 0; j < listImages.size(); j++) {
                                    listImages.get(j).setType(Images.USER);
                                }
                                user.setImages(imgp.getImagesList().get(0));
                            }
                        }
                    }

                } catch (Exception e) {
                    if (AppContext.DEBUG)
                        e.printStackTrace();
                }

                list.add(user);

            }

        } catch (JSONException e) {
            if (AppContext.DEBUG)
                e.printStackTrace();
        }


        return list;
    }


}
