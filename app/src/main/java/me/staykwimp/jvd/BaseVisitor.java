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

public interface BaseVisitor<R> {
    default public R visit(Downloader visitable) throws ClassCastException {
        if (visitable instanceof YoutubePlaylistDownloader)
            return visit((YoutubePlaylistDownloader) visitable);
        else if (visitable instanceof YoutubeVideoDownloader)
            return visit((YoutubeVideoDownloader) visitable);
        else if (visitable instanceof YoutubeMusicDownloader)
            return visit((YoutubeMusicDownloader) visitable);
        else if (visitable instanceof YoutubeMusicPlaylistDownloader)
            return visit((YoutubeMusicPlaylistDownloader) visitable);
        // add more cases for visitable objects
        
        throw new ClassCastException("Cannot visit class of type " + visitable.getClass().getName());
    }
    public R visit(YoutubeVideoDownloader visitable);
    public R visit(YoutubePlaylistDownloader visitable);
    public R visit(YoutubeMusicDownloader visitable);
    public R visit(YoutubeMusicPlaylistDownloader visitable);
}
