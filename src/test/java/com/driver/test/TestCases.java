package com.driver.test;

import com.driver.*;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.InjectMocks;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = Application.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestCases {

    SpotifyRepository repository = new SpotifyRepository();

    @Test
    void createUser_shouldCreateUserWithGivenNameAndMobile() {
        String name = "John";
        String mobile = "1234567890";

        User user = repository.createUser(name, mobile);

        assertEquals(name, user.getName());
        assertEquals(mobile, user.getMobile());
    }

    @Test
    void createArtist_shouldCreateArtistWithGivenName() {
        String name = "Ed Sheeran";

        Artist artist = repository.createArtist(name);

        assertEquals(name, artist.getName());
    }

    @Test
    void createAlbum_shouldCreateAlbumWithGivenTitleAndArtistName(){
        String title = "Divide";
        String artistName = "Ed Sheeran";

        Album album = repository.createAlbum(title, artistName);

        Artist artist = null;
        for(Artist a: repository.artists){
            if(a.getName().equals(artistName)){
                artist = a;
                break;
            }
        }

        assertEquals(title, album.getTitle());
        assertTrue(repository.albums.contains(album));
        assertEquals(1, repository.artistAlbumMap.get(artist).size());
        assertTrue(repository.artistAlbumMap.get(artist).contains(album));
    }

    @Test
    void createSong_shouldCreateSongWithGivenTitleAlbumNameAndLength() throws Exception {
        String title = "Shape of You";
        String albumName = "Divide";
        int length = 233;

        Album album = repository.createAlbum(albumName, "Ed Sheeran");
        Song song = repository.createSong(title, albumName, length);

        assertEquals(title, song.getTitle());
        assertEquals(length, song.getLength());
        assertTrue(repository.songs.contains(song));
        assertEquals(1, repository.albumSongMap.get(album).size());
        assertTrue(repository.albumSongMap.get(album).contains(song));
    }

    @Test
    void createSong_shouldNotCreateSongWithGivenTitleAlbumNameAndLength() throws Exception {
        String title = "Shape of You";
        String albumName = "Divide";
        int length = 233;

        Album album = repository.createAlbum(albumName, "Ed Sheeran");
        try {
            Song song = repository.createSong(title, "TEMP", length);
        } catch(Exception e){
            assert(e.getMessage().equals("Album does not exist"));
        }
    }

    @Test
    void createPlaylistOnLength_shouldCreatePlaylistWithGivenTitleAndLength() throws Exception {
        String mobile = "1234567890";
        String title = "Top Songs";
        int length = 233;

        User user = repository.createUser("John", mobile);
        Album album = repository.createAlbum("Divide", "Ed Sheeran");
        Album album1 = repository.createAlbum("X", "Taylor Swift");
        Song song1 = repository.createSong("Shape of You", "Divide", length);
        Song song2 = repository.createSong("Thinking Out Loud", "X", length + 1);
        Playlist playlist = repository.createPlaylistOnLength(mobile, title, length);

        assertEquals(title, playlist.getTitle());
        assertTrue(repository.playlists.contains(playlist));
        assertEquals(1, repository.playlistSongMap.get(playlist).size());
        assertTrue(repository.playlistSongMap.get(playlist).contains(song1));
        assertNotNull(repository.playlistListenerMap.get(playlist));
        assertTrue(repository.playlistListenerMap.get(playlist).contains(user));
        assertEquals(1, repository.userPlaylistMap.get(user).size());
        assertTrue(repository.userPlaylistMap.get(user).contains(playlist));
    }

    @Test
    void createPlaylistOnLength_shouldNotCreatePlaylistWithGivenTitleAndLength() throws Exception {
        String mobile = "1234567890";
        String title = "Top Songs";
        int length = 233;

        User user = repository.createUser("John", mobile);
        Album album = repository.createAlbum("Divide", "Ed Sheeran");
        Album album1 = repository.createAlbum("X", "Taylor Swift");
        Song song1 = repository.createSong("Shape of You", "Divide", length);
        Song song2 = repository.createSong("Thinking Out Loud", "X", length + 1);

        try {
            Playlist playlist = repository.createPlaylistOnLength("123456789", title, length);
        } catch(Exception e){
            assert (e.getMessage().equals("User does not exist"));
        }
    }

    @Test
    public void createPlaylistOnName_shouldCreatePlaylistWithGivenTitleAndSongTitles() throws Exception {
        User user = repository.createUser("John", "555-1234");
        Album album1 = repository.createAlbum("Album 1", "Ed Shereen");
        Album album2 = repository.createAlbum("Album 2", "Taylor Swift");
        Song song1 = repository.createSong("Song 1", "Album 1", 300);
        Song song2 = repository.createSong("Song 2", "Album 2", 240);
        Song song3 = repository.createSong("Song 3", "Album 1", 180);
        List<String> songTitles = new ArrayList<>();
        songTitles.add("Song 1");
        songTitles.add("Song 3");
        Playlist playlist = repository.createPlaylistOnName("555-1234", "My Playlist", songTitles);
        List<Song> songsInPlaylist = repository.playlistSongMap.get(playlist);
        assertTrue(repository.playlists.contains(playlist));
        assertEquals(2, repository.playlistSongMap.get(playlist).size());
        assertTrue(repository.playlistSongMap.get(playlist).contains(song1));
        assertNotNull(repository.playlistListenerMap.get(playlist));
        assertTrue(repository.playlistListenerMap.get(playlist).contains(user));
        assertEquals(1, repository.userPlaylistMap.get(user).size());
        assertTrue(repository.userPlaylistMap.get(user).contains(playlist));
    }

    @Test
    public void createPlaylistOnName_shouldNotCreatePlaylistWithGivenTitleAndSongTitles() throws Exception {
        User user = repository.createUser("John", "555-1234");
        Album album1 = repository.createAlbum("Album 1", "Ed Shereen");
        Album album2 = repository.createAlbum("Album 2", "Taylor Swift");
        Song song1 = repository.createSong("Song 1", "Album 1", 300);
        Song song2 = repository.createSong("Song 2", "Album 2", 240);
        Song song3 = repository.createSong("Song 3", "Album 1", 180);
        List<String> songTitles = new ArrayList<>();
        songTitles.add("Song 1");
        songTitles.add("Song 3");
        try {
            Playlist playlist = repository.createPlaylistOnName("555-123", "My Playlist", songTitles);
        } catch(Exception e){
            assert(e.getMessage().equals("User does not exist"));
        }
    }

    @Test
    public void findPlaylist_shouldFindPlaylistWithGivenTitleAndMobile() throws Exception {
        User user = repository.createUser("John", "555-1234");
        Album album1 = repository.createAlbum("Album 1", "Ed Shereen");
        Album album2 = repository.createAlbum("Album 2", "Taylor Swift");
        Song song1 = repository.createSong("Song 1", "Album 1", 300);
        Song song2 = repository.createSong("Song 2", "Album 2", 240);
        Song song3 = repository.createSong("Song 3", "Album 1", 180);
        List<String> songTitles = new ArrayList<>();
        songTitles.add("Song 1");
        songTitles.add("Song 3");
        Playlist playlist = repository.createPlaylistOnName("555-1234", "My Playlist", songTitles);
        User user1 = repository.createUser("Doe", "444-1234");
        Playlist playlist1 = repository.findPlaylist(user1.getMobile(), playlist.getTitle());
        assertEquals(2, repository.playlistListenerMap.get(playlist).size());
        assertTrue(repository.playlistListenerMap.get(playlist).contains(user));
        assertTrue(repository.playlistListenerMap.get(playlist).contains(user1));
        assertEquals(1, repository.userPlaylistMap.get(user1).size());
        assertTrue(repository.userPlaylistMap.get(user1).contains(playlist));
    }

    @Test
    public void findPlaylist_shouldFindPlaylistWithGivenTitleAndMobile1() throws Exception {
        User user = repository.createUser("John", "555-1234");
        Album album1 = repository.createAlbum("Album 1", "Ed Shereen");
        Album album2 = repository.createAlbum("Album 2", "Taylor Swift");
        Song song1 = repository.createSong("Song 1", "Album 1", 300);
        Song song2 = repository.createSong("Song 2", "Album 2", 240);
        Song song3 = repository.createSong("Song 3", "Album 1", 180);
        List<String> songTitles = new ArrayList<>();
        songTitles.add("Song 1");
        songTitles.add("Song 3");
        Playlist playlist = repository.createPlaylistOnName("555-1234", "My Playlist", songTitles);
        User user1 = repository.createUser("Doe", "444-1234");
        Playlist playlist1 = repository.findPlaylist(user1.getMobile(), playlist.getTitle());
        assertEquals(2, repository.playlistListenerMap.get(playlist).size());
        assertTrue(repository.playlistListenerMap.get(playlist).contains(user));
        assertTrue(repository.playlistListenerMap.get(playlist).contains(user1));
        assertEquals(1, repository.userPlaylistMap.get(user1).size());
        assertTrue(repository.userPlaylistMap.get(user1).contains(playlist));
    }

    @Test
    public void findPlaylist_shouldNotFindPlaylistWithGivenTitleAndMobile1() throws Exception {
        User user = repository.createUser("John", "555-1234");
        Album album1 = repository.createAlbum("Album 1", "Ed Shereen");
        Album album2 = repository.createAlbum("Album 2", "Taylor Swift");
        Song song1 = repository.createSong("Song 1", "Album 1", 300);
        Song song2 = repository.createSong("Song 2", "Album 2", 240);
        Song song3 = repository.createSong("Song 3", "Album 1", 180);
        List<String> songTitles = new ArrayList<>();
        songTitles.add("Song 1");
        songTitles.add("Song 3");
        Playlist playlist = repository.createPlaylistOnName("555-1234", "My Playlist", songTitles);
        User user1 = repository.createUser("Doe", "444-1234");
        try {
            Playlist playlist1 = repository.findPlaylist("555", playlist.getTitle());
        } catch(Exception e){
            assert (e.getMessage().equals("User does not exist"));
        }
    }

    @Test
    public void findPlaylist_shouldNotFindPlaylistWithGivenTitleAndMobile2() throws Exception {
        User user = repository.createUser("John", "555-1234");
        Album album1 = repository.createAlbum("Album 1", "Ed Shereen");
        Album album2 = repository.createAlbum("Album 2", "Taylor Swift");
        Song song1 = repository.createSong("Song 1", "Album 1", 300);
        Song song2 = repository.createSong("Song 2", "Album 2", 240);
        Song song3 = repository.createSong("Song 3", "Album 1", 180);
        List<String> songTitles = new ArrayList<>();
        songTitles.add("Song 1");
        songTitles.add("Song 3");
        Playlist playlist = repository.createPlaylistOnName("555-1234", "My Playlist", songTitles);
        User user1 = repository.createUser("Doe", "444-1234");
        try {
            Playlist playlist1 = repository.findPlaylist("555-1234", "Temp");
        } catch(Exception e){
            assert (e.getMessage().equals("Playlist does not exist"));
        }
    }

    @Test
    public void likeSong_shouldLikeSongWithGivenTitleAndMobile() throws Exception {
        User user = repository.createUser("John", "123456789");
        User user1 = repository.createUser("Doe", "1234567890");
        Artist artist = repository.createArtist("Ed Shereen");
        Album album1 = repository.createAlbum("Album 1", "Ed Shereen");
        Album album2 = repository.createAlbum("Album 2", "Taylor Swift");
        Song song1 = repository.createSong("Song 1", "Album 1", 300);
        Song song2 = repository.createSong("Song 2", "Album 2", 240);
        Song song3 = repository.createSong("Song 3", "Album 1", 180);
        Song song = repository.likeSong("123456789", "Song 1");
        assertEquals(1, song.getLikes());
        assertEquals(1, repository.songLikeMap.get(song).size());
        assertTrue(repository.songLikeMap.get(song).contains(user));
        assertEquals(1, artist.getLikes());

        song = repository.likeSong("123456789", "Song 1");
        assertEquals(1, song.getLikes());
        assertEquals(1, repository.songLikeMap.get(song).size());
        assertEquals(1, artist.getLikes());

        song = repository.likeSong("1234567890", "Song 1");
        assertEquals(2, song.getLikes());
        assertEquals(2, repository.songLikeMap.get(song).size());
        assertTrue(repository.songLikeMap.get(song).contains(user));
        assertTrue(repository.songLikeMap.get(song).contains(user1));
        assertEquals(2, artist.getLikes());

        song = repository.likeSong("1234567890", "Song 3");
        assertEquals(1, song.getLikes());
        assertEquals(1, repository.songLikeMap.get(song).size());
        assertTrue(repository.songLikeMap.get(song).contains(user1));
        assertEquals(3, artist.getLikes());
    }

    @Test
    public void likeSong_shouldLikeSongWithGivenTitleAndMobile1() throws Exception {
        User user = repository.createUser("John", "123456789");
        User user1 = repository.createUser("Doe", "1234567890");
        Artist artist = repository.createArtist("Ed Shereen");
        Album album1 = repository.createAlbum("Album 1", "Ed Shereen");
        Album album2 = repository.createAlbum("Album 2", "Taylor Swift");
        Song song1 = repository.createSong("Song 1", "Album 1", 300);
        Song song2 = repository.createSong("Song 2", "Album 2", 240);
        Song song3 = repository.createSong("Song 3", "Album 1", 180);
        Song song = repository.likeSong("123456789", "Song 1");
        assertEquals(1, song.getLikes());
        assertEquals(1, repository.songLikeMap.get(song).size());
        assertTrue(repository.songLikeMap.get(song).contains(user));
        assertEquals(1, artist.getLikes());

        song = repository.likeSong("123456789", "Song 1");
        assertEquals(1, song.getLikes());
        assertEquals(1, repository.songLikeMap.get(song).size());
        assertEquals(1, artist.getLikes());

        song = repository.likeSong("1234567890", "Song 1");
        assertEquals(2, song.getLikes());
        assertEquals(2, repository.songLikeMap.get(song).size());
        assertTrue(repository.songLikeMap.get(song).contains(user));
        assertTrue(repository.songLikeMap.get(song).contains(user1));
        assertEquals(2, artist.getLikes());

        song = repository.likeSong("1234567890", "Song 3");
        assertEquals(1, song.getLikes());
        assertEquals(1, repository.songLikeMap.get(song).size());
        assertTrue(repository.songLikeMap.get(song).contains(user1));
        assertEquals(3, artist.getLikes());
    }


    @Test
    public void likeSong_shouldNotLikeSongWithGivenTitleAndMobile() throws Exception {
        User user = repository.createUser("John", "123456789");
        User user1 = repository.createUser("Doe", "1234567890");
        Artist artist = repository.createArtist("Ed Shereen");
        Album album1 = repository.createAlbum("Album 1", "Ed Shereen");
        Album album2 = repository.createAlbum("Album 2", "Taylor Swift");
        Song song1 = repository.createSong("Song 1", "Album 1", 300);
        Song song2 = repository.createSong("Song 2", "Album 2", 240);
        Song song3 = repository.createSong("Song 3", "Album 1", 180);

        try{
            Song song = repository.likeSong("12345678", "Song 1");
        } catch (Exception e){
            assert (e.getMessage().equals("User does not exist"));
        }
        try{
            Song song = repository.likeSong("123456789", "Song 4");
        } catch (Exception e){
            assert (e.getMessage().equals("Song does not exist"));
        }
    }

    @Test
    public void mostPopularSong() throws Exception {
        User user = repository.createUser("John", "123456789");
        User user1 = repository.createUser("Doe", "1234567890");
        Artist artist = repository.createArtist("Ed Shereen");
        Album album1 = repository.createAlbum("Album 1", "Ed Shereen");
        Album album2 = repository.createAlbum("Album 2", "Taylor Swift");
        Song song1 = repository.createSong("Song 1", "Album 1", 300);
        Song song2 = repository.createSong("Song 2", "Album 2", 240);
        Song song3 = repository.createSong("Song 3", "Album 1", 180);
        Song song = repository.likeSong("123456789", "Song 1");
        song = repository.likeSong("123456789", "Song 1");
        song = repository.likeSong("1234567890", "Song 1");
        song = repository.likeSong("1234567890", "Song 3");
        assertEquals("Song 1", repository.mostPopularSong());
    }

    @Test
    public void mostPopularSong1() throws Exception {
        User user = repository.createUser("John", "123456789");
        User user1 = repository.createUser("Doe", "1234567890");
        Artist artist = repository.createArtist("Ed Shereen");
        Album album1 = repository.createAlbum("Album 1", "Ed Shereen");
        Album album2 = repository.createAlbum("Album 2", "Taylor Swift");
        Song song1 = repository.createSong("Song 1", "Album 1", 300);
        Song song2 = repository.createSong("Song 2", "Album 2", 240);
        Song song3 = repository.createSong("Song 3", "Album 1", 180);
        Song song = repository.likeSong("123456789", "Song 1");
        song = repository.likeSong("123456789", "Song 1");
        song = repository.likeSong("1234567890", "Song 1");
        song = repository.likeSong("1234567890", "Song 3");
        assertEquals("Song 1", repository.mostPopularSong());
    }

    @Test
    public void mostPopularArtist() throws Exception {
        User user = repository.createUser("John", "123456789");
        User user1 = repository.createUser("Doe", "1234567890");
        Artist artist = repository.createArtist("Ed Shereen");
        Artist artist1 = repository.createArtist("Taylor Swift");
        Album album1 = repository.createAlbum("Album 1", "Ed Shereen");
        Album album2 = repository.createAlbum("Album 2", "Taylor Swift");
        Song song1 = repository.createSong("Song 1", "Album 1", 300);
        Song song2 = repository.createSong("Song 2", "Album 2", 240);
        Song song3 = repository.createSong("Song 3", "Album 2", 180);
        Song song = repository.likeSong("123456789", "Song 1");
        song = repository.likeSong("123456789", "Song 1");
        song = repository.likeSong("1234567890", "Song 1");
        song = repository.likeSong("1234567890", "Song 2");
        song = repository.likeSong("123456789", "Song 1");
        song = repository.likeSong("1234567890", "Song 2");
        song = repository.likeSong("1234567890", "Song 1");
        song = repository.likeSong("123456789", "Song 1");
        song = repository.likeSong("1234567890", "Song 1");
        song = repository.likeSong("1234567890", "Song 1");
        assertEquals("Ed Shereen", repository.mostPopularArtist());
    }

    @Test
    public void mostPopularArtist1() throws Exception {
        User user = repository.createUser("John", "123456789");
        User user1 = repository.createUser("Doe", "1234567890");
        Artist artist = repository.createArtist("Ed Shereen");
        Artist artist1 = repository.createArtist("Taylor Swift");
        Album album1 = repository.createAlbum("Album 1", "Ed Shereen");
        Album album2 = repository.createAlbum("Album 2", "Taylor Swift");
        Song song1 = repository.createSong("Song 1", "Album 1", 300);
        Song song2 = repository.createSong("Song 2", "Album 2", 240);
        Song song3 = repository.createSong("Song 3", "Album 1", 180);
        Song song = repository.likeSong("123456789", "Song 1");
        song = repository.likeSong("123456789", "Song 1");
        song = repository.likeSong("1234567890", "Song 1");
        song = repository.likeSong("1234567890", "Song 3");
        song = repository.likeSong("123456789", "Song 2");
        song = repository.likeSong("1234567890", "Song 2");
        song = repository.likeSong("1234567890", "Song 2");
        song = repository.likeSong("123456789", "Song 3");
        song = repository.likeSong("1234567890", "Song 3");
        song = repository.likeSong("1234567890", "Song 3");
        assertEquals("Ed Shereen", repository.mostPopularArtist());
    }
}