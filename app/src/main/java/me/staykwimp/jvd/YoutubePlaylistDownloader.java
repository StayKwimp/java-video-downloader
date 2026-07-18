package me.staykwimp.jvd;

/*
    Java Video Downloader
    Copyright (C) 2026  StayKwimp

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

import java.util.ArrayList;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;

import com.github.felipeucelli.javatube.*;

public class YoutubePlaylistDownloader implements Downloader {
    public <R> R accept(BaseVisitor<R> visitor) {
        return visitor.visit(this);
    }

    /*
        Temporary fix for playlists: can only do max 200.
    */
    protected class FixedPlaylist extends Playlist {
        public FixedPlaylist(String InputUrl) throws JSONException, NoSuchFieldException, IllegalAccessException {
            super(InputUrl);
            // Field f = getClass().getSuperclass().getDeclaredField("innerTube");
            // f.setAccessible(true);
            // f.set(this, new InnerTube("WEB"));
        }

        @Override
        protected void setContinuationToken(JSONArray importantContent) throws JSONException {
            JSONObject continuationEndpoint = importantContent.getJSONObject(importantContent.length() - 1)
                    .getJSONObject("continuationItemViewModel");

            if (continuationEndpoint.has("continuationCommand")){
                continuationToken = continuationEndpoint
                        .getJSONObject("continuationCommand")
                        .getJSONObject("innertubeCommand")
                        .getJSONObject("continuationCommand")
                        .getString("token"); // this part is modified
                
            }else if (continuationEndpoint.has("commandExecutorCommand")){
                continuationToken = continuationEndpoint
                        .getJSONObject("commandExecutorCommand")
                        .getJSONArray("commands")
                        .getJSONObject(1)
                        .getJSONObject("continuationCommand")
                        .getString("token");
            }
        }

        @Override
        protected JSONArray extractContinuationItems(JSONArray importantContent) throws Exception {
            JSONArray swap = new JSONArray();

            JSONArray continuationEnd = buildContinuationUrl(continuationToken);

            for(int i = 0; i < importantContent.length(); i++){
                swap.put(importantContent.get(i));
            }

            for(int i = 0; i < continuationEnd.length(); i++){
                swap.put(continuationEnd.get(i));
            }
            return swap;
        }


        // override for debugging
        @Override
        protected JSONArray buildContinuationUrl(String continuation) throws Exception {
            try {
                Field f = getClass().getSuperclass().getDeclaredField("innerTube");
                f.setAccessible(true);
                
                String data = "{" +
                        "\"continuation\": \"" + continuation + "\"" +
                    "}";
                
                JSONObject o = ((InnerTube) f.get(this)).browse(new JSONObject(data));
                // System.err.println(o.toString(4));
                return extractVideos(o);
            }
            catch (Exception e) {
                e.printStackTrace();
                return super.buildContinuationUrl(continuation);
            }
        }

        @Override
        protected JSONArray extractVideos(JSONObject rawJson) {
            JSONArray videosArray = new JSONArray();
            // System.err.println(rawJson.toString(4));
            try {
                
                try {
                    JSONObject tabs = rawJson.getJSONObject("contents")
                        .getJSONObject("twoColumnBrowseResultsRenderer")
                        .getJSONArray("tabs")
                        .getJSONObject(0)
                        .getJSONObject("tabRenderer")
                        .getJSONObject("content")
                        .getJSONObject("sectionListRenderer")
                        .getJSONArray("contents")
                        .getJSONObject(0);

                    videosArray = tabs.getJSONObject("itemSectionRenderer")
                        .getJSONArray("contents");
                    // System.out.println("We have " + videosArray.length() + " videos");
                }
                catch (JSONException ignored) {
                    // System.out.println("ex: " + ignored);
                    System.err.println(rawJson.toString(4));
                    // continuation items here
                    videosArray = rawJson.getJSONArray("onResponseReceivedActions")
                                .getJSONObject(0)
                                .getJSONObject("appendContinuationItemsAction")
                                .getJSONArray("continuationItems");
                    // System.out.println("We have " + videosArray.length() + " videos");
                }

                

                // we may only get not all videos instantly.
                if (videosArray.getJSONObject(videosArray.length() - 1).has("continuationItemViewModel")) {
                    setContinuationToken(videosArray);
                    videosArray = extractContinuationItems(videosArray);
                    // System.out.println("so in total we have " + videosArray.length() + " videos");
                }
            }catch (JSONException e){
                e.printStackTrace();
                System.err.println("JSON exception while extracting videos. See stacktrace above for full details.");
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return videosArray;
        }


        @Override
        public ArrayList<String> getVideos() throws Exception {
            JSONObject json = getJson();
            // System.err.println(json.toString(4));
            JSONArray video = extractVideos(json);
            ArrayList<String> videosId = new ArrayList<>();
            try {
                for(int i = 0; i < video.length(); i++){
                    try{
                        videosId.add("https://www.youtube.com/watch?v=" + video.getJSONObject(i)
                                .getJSONObject("lockupViewModel")
                                .getString("contentId"));
                    }catch (Exception e){
                        e.printStackTrace();
                        System.err.println("Exception thrown on trying to extract video IDs from playlist. See stacktrace above for details.");
                    }
                }
                return unify(videosId);
            } catch (Exception e) {
                throw new Error(e);
            }
        }
    }









    protected FixedPlaylist playlist;
    protected ArrayList<String> videoCache = null;

    public YoutubePlaylistDownloader(String url) throws JSONException, NoSuchFieldException, IllegalAccessException {
        playlist = new FixedPlaylist(url);
    }

    // super() has to be called in constructors for subclasses.
    // use this for full control over the protected variables.
    protected YoutubePlaylistDownloader() {
        playlist = null;
    }

    public YoutubeMusicPlaylistDownloader toYoutubeMusic() {
        return new YoutubeMusicPlaylistDownloader(this);
    }


    // Gets YoutubeVideoDownloaders from a playlist.
    public ArrayList<YoutubeVideoDownloader> getDownloadersFromPlaylist(String saveDirectory) {
        ArrayList<YoutubeVideoDownloader> downloaderArray = new ArrayList<>(getPlaylistSize());
        Random randomGen = new Random();
        for (String url: this.getPlaylistUrls()) {
            try {
                // System.out.println("Found URL in playlist: " + url);
                YoutubeVideoDownloader yvd = new YoutubeVideoDownloader(url, saveDirectory);
                downloaderArray.add(yvd);
                System.out.println("Found video: " + yvd);
                Thread.sleep(randomGen.nextInt(300) + 300); // random delay between adding videos
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return downloaderArray;
    }


    public int getPlaylistSize() {
        return this.getPlaylistUrls().size();
    }

    /*
        Gets the URLs from a playlist.
        Also caches the list such that it only retrieves the URL list from youtube at most once.
    */
    public ArrayList<String> getPlaylistUrls() {
        if (videoCache != null)
            return videoCache;

        System.err.println("Using workaround since playlists cannot be downloaded with JavaTube currently. This method can only download the first 200 videos.");
        try {
            videoCache = playlist.getVideos();
            return videoCache;
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("An error occured while getting the videos from a playlist. See stacktrace above for details.");
            return new ArrayList<String>(); // return empty list upon error
        }
    }

    public String getTitle() {
        try {
            return playlist.getTitle();
        } catch (Exception e) {
            e.printStackTrace();
            return "unknown title";
        }
    }

    public String getDescription() {
        try {
            return playlist.getDescription();
        } catch (Exception e) {
            e.printStackTrace();
            return "unknown description";
        }
    }

    public String getChannelName() {
        try {
            return playlist.getOwner();
        } catch (Exception e) {
            e.printStackTrace();
            return "unknown channel";
        }
    }
}
