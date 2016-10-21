package com.uxerlabs.cabifychallenge.api;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.uxerlabs.cabifychallenge.model.Vehicle;
import com.uxerlabs.cabifychallenge.model.VehicleDeserializer;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Create a service with setting to handle the connection with the Cabify API
 * @author Francisco Cuenca on 18/10/16.
 */

public class CabifyRestService {
    private static String ENDPOINT = "https://test.cabify.com";

    //To show connection log
    private static HttpLoggingInterceptor logging =
            new HttpLoggingInterceptor()
                .setLevel(HttpLoggingInterceptor.Level.BODY);

    //Client http
    private static OkHttpClient.Builder httpClient =
            new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .connectTimeout(30,TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS);

    //To handle JSON
    private static Gson gson =
            new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .registerTypeAdapter(Vehicle.class, new VehicleDeserializer())
                .create();

    //To handle connection
    private static Retrofit.Builder builder =
            new Retrofit.Builder()
                    .baseUrl(ENDPOINT)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create());

    public static <S> S createService(Class<S> serviceClass) {
        return createService(serviceClass, null);
    }

    /*
    Create a Service
    @param serviceClass
    @param authToken authorization token of client application
     */
    public static <S> S createService(Class<S> serviceClass, final String authToken) {
        if (authToken != null) {
            httpClient.addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Interceptor.Chain chain) throws IOException {
                    Request original = chain.request();

                    Request.Builder requestBuilder = original.newBuilder()
                            .header("Authorization", "Bearer "+authToken)
                            .method(original.method(), original.body());

                    Request request = requestBuilder.build();
                    return chain.proceed(request);
                }
            });
        }

        OkHttpClient client = httpClient.build();
        Retrofit retrofit = builder.client(client).build();
        return retrofit.create(serviceClass);
    }

}
