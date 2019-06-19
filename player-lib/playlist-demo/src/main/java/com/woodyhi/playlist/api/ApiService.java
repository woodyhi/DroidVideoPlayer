package com.woodyhi.playlist.api;

import com.woodyhi.playlist.model.TrailerListData;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Headers;

/**
 * @auth June
 * @date 2019/06/18
 */
public interface ApiService {

    @GET("PageSubArea/TrailerList.api")
    Observable<TrailerListData> getTrailersData();
}
