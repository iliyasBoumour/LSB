package com.inpt.notifications;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAf2werXg:APA91bG1bYjDNe0oB8iGNAQLfhNhZn2dmJWJ28y6Q2IFyoS5eLixMwzYe8GuycxnpK4JS0QbQ7WEADEMObvGw3NzthM7dcJWtjGFKCT0808zCEcQacXDLfxJLdybjukFGBaV3uGwTD99" // Your server key refer to video for finding your server key
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotifcation(@Body NotificationSender body);
}