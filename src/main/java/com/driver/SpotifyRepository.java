package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class SpotifyRepository {
    public HashMap<Artist, List<Album>> artistAlbumMap;
    public HashMap<Album, List<Song>> albumSongMap;
    public HashMap<Playlist, List<Song>> playlistSongMap;
    public HashMap<Playlist, List<User>> playlistListenerMap;
    public HashMap<User, Playlist> creatorPlaylistMap;
    public HashMap<User, List<Playlist>> userPlaylistMap;
    public HashMap<Song, List<User>> songLikeMap;

    public List<User> users;
    public List<Song> songs;
    public List<Playlist> playlists;
    public List<Album> albums;
    public List<Artist> artists;

    public SpotifyRepository(){
        //To avoid hitting apis multiple times, initialize all the hashmaps here with some dummy data
        artistAlbumMap = new HashMap<>();
        albumSongMap = new HashMap<>();
        playlistSongMap = new HashMap<>();
        playlistListenerMap = new HashMap<>();
        creatorPlaylistMap = new HashMap<>();
        userPlaylistMap = new HashMap<>();
        songLikeMap = new HashMap<>();

        users = new ArrayList<>();
        songs = new ArrayList<>();
        playlists = new ArrayList<>();
        albums = new ArrayList<>();
        artists = new ArrayList<>();
    }

    public User createUser(String name, String mobile) {
        User newUser = new User(name, mobile);
        users.add(newUser);
        return newUser;
    }

    public Artist createArtist(String name) {
        Artist newArtist = new Artist(name.trim());
        artists.add(newArtist);
        return newArtist;
    }

    public Album createAlbum(String title, String artistName) {
        Artist artist = findOrCreateArtist(artistName.trim());
        Album newAlbum = new Album(title.trim());
        artistAlbumMap.computeIfAbsent(artist, k -> new ArrayList<>()).add(newAlbum);
        albums.add(newAlbum);
        return newAlbum;
    }

    private Artist findOrCreateArtist(String name) {
        for (Artist artist : artists) {
            if (artist.getName().equalsIgnoreCase(name)) {
                return artist;
            }
        }
        return createArtist(name);
    }

    public Song createSong(String title, String albumName, int length) throws Exception{
        Album album = findAlbum(albumName.trim());
        if (album == null) {
            throw new Exception("Album does not exist");
        }
        Song newSong = new Song(title.trim(), length);
        albumSongMap.computeIfAbsent(album, k -> new ArrayList<>()).add(newSong);
        songs.add(newSong);
        return newSong;
    }

    private Album findAlbum(String albumName) {
        for (Album album : albums) {
            if (album.getTitle().equalsIgnoreCase(albumName.trim())) {
                return album;
            }
        }
        return null;
    }

    public Playlist createPlaylistOnLength(String mobile, String title, int length) throws Exception {
        User user = findUserByMobile(mobile.trim());
        if (user == null) {
            throw new Exception("User does not exist");
        }
        Playlist playlist = new Playlist(title.trim());
        List<Song> playlistSongs = new ArrayList<>();
        for (Song song : songs) {
            if (song.getLength() == length) {
                playlistSongs.add(song);
            }
        }
        playlistSongMap.put(playlist, playlistSongs);
        playlistListenerMap.put(playlist, Collections.singletonList(user));
        creatorPlaylistMap.put(user, playlist);
        userPlaylistMap.computeIfAbsent(user, k -> new ArrayList<>()).add(playlist);
        playlists.add(playlist);
        return playlist;
    }
    private User findUserByMobile(String mobile) {
        for (User user : users) {
            if (user.getMobile().equalsIgnoreCase(mobile)) {
                return user;
            }
        }
        return null;
    }

    public Playlist createPlaylistOnName(String mobile, String title, List<String> songTitles) throws Exception {
        User user = findUserByMobile(mobile.trim());
        if (user == null) {
            throw new Exception("User does not exist");
        }
        Playlist playlist = new Playlist(title.trim());
        List<Song> playlistSongs = new ArrayList<>();
        for (String songTitle : songTitles) {
            for (Song song : songs) {
                if (song.getTitle().equalsIgnoreCase(songTitle.trim())) {
                    playlistSongs.add(song);
                }
            }
        }
        playlistSongMap.put(playlist, playlistSongs);
        playlistListenerMap.put(playlist, Collections.singletonList(user));
        creatorPlaylistMap.put(user, playlist);
        userPlaylistMap.computeIfAbsent(user, k -> new ArrayList<>()).add(playlist);
        playlists.add(playlist);
        return playlist;
    }

    public Playlist findPlaylist(String mobile, String playlistTitle) throws Exception {
        User user = findUserByMobile(mobile.trim());
        if (user == null) {
            throw new Exception("User does not exist");
        }
        for (Playlist playlist : playlists) {
            if (playlist.getTitle().equalsIgnoreCase(playlistTitle.trim())) {
                if (!playlistListenerMap.containsKey(playlist) || !playlistListenerMap.get(playlist).contains(user)) {
                    playlistListenerMap.computeIfAbsent(playlist, k -> new ArrayList<>()).add(user);
                    userPlaylistMap.computeIfAbsent(user, k -> new ArrayList<>()).add(playlist);
                }
                return playlist;
            }
        }
        throw new Exception("Playlist does not exist");
    }

    public Song likeSong(String mobile, String songTitle) throws Exception {
        User user = findUserByMobile(mobile.trim());
        if (user == null) {
            throw new Exception("User does not exist");
        }
        Song likedSong = null;
        for (Song song : songs) {
            if (song.getTitle().equalsIgnoreCase(songTitle.trim())) {
                if (!songLikeMap.containsKey(song) || !songLikeMap.get(song).contains(user)) {
                    song.setLikes(song.getLikes() + 1);
                    songLikeMap.computeIfAbsent(song, k -> new ArrayList<>()).add(user);
                    Artist artist = findArtistOfSong(song);
                    if (artist != null) {
                        artist.setLikes(artist.getLikes() + 1);
                    }
                    likedSong = song;
                }
                break;
            }
        }
        if (likedSong == null) {
            throw new Exception("Song does not exist");
        }
        return likedSong;
    }
    private Artist findArtistOfSong(Song song) {
        for (Map.Entry<Artist, List<Album>> entry : artistAlbumMap.entrySet()) {
            for (Album album : entry.getValue()) {
                if (albumSongMap.containsKey(album) && albumSongMap.get(album).contains(song)) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }
    public String mostPopularArtist() {
        Artist mostPopular = null;
        for (Artist artist : artists) {
            if (mostPopular == null || artist.getLikes() > mostPopular.getLikes()) {
                mostPopular = artist;
            }
        }
        return mostPopular != null ? mostPopular.getName() : null;
    }

    public String mostPopularSong() {
        Song mostPopular = null;
        for (Song song : songs) {
            if (mostPopular == null || song.getLikes() > mostPopular.getLikes()) {
                mostPopular = song;
            }
        }
        return mostPopular != null ? mostPopular.getTitle() : null;
    }
}
