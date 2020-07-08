package com.jit.himalaya.interfaces;

import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.track.Track;

import java.util.List;

public interface IAlbumDetailViewCallback{
    void onDetailListLoaded(List<Track> tracks);

    void onNetworkError(int errorCode, String errorMessage);

    void onAlbumLoaded(Album album);

}
